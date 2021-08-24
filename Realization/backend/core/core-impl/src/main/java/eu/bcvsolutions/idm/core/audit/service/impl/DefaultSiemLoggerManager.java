package eu.bcvsolutions.idm.core.audit.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * 
 * Default implementation {@link SiemLoggerManager}.
 * 
 * @author Ondrej Husnik
 * @since 11.2.0
 *
 */
@Service("siemLoggerManager")
public class DefaultSiemLoggerManager implements SiemLoggerManager {
	
	@Autowired
	@Lazy
	private SecurityService securityService;
	@Autowired
	@Lazy
	private LookupService lookupService;
	@Autowired
	@Lazy
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	@Lazy
	private ConfigurationService configurationService;

	@Override
	public <E extends Serializable> void log(EntityEvent<E> event, String status, String detail) {
		if (event == null) {
			return;
		}
		Serializable content = event.getContent();
		String operationType = event.getType().name();
		String action = null;
		String transactionUuid = null;
		AbstractDto targetDto = null;
		AbstractDto subjectDto = null;
		
		if (content instanceof AbstractDto) {
			transactionUuid = Objects.toString(((AbstractDto)content).getTransactionId(),"");
		}
		
		// entity specific logging approach
		if(content instanceof IdmIdentityDto) {
			IdmIdentityDto dto = (IdmIdentityDto)content;
			IdmIdentityDto oldDto = (IdmIdentityDto)event.getOriginalSource();
			identitySpecificLog(dto, oldDto, operationType, transactionUuid, status, detail);
			return;
		} else if (content instanceof IdmRoleDto) {
			action = buildAction(ROLE_LEVEL_KEY, operationType);
			if(skipLogging(action)) {
				return;
			}
			targetDto = (AbstractDto)content;
		} else if (content instanceof IdmAuthorizationPolicyDto) {
			action = buildAction(ROLE_PERMISSION_LEVEL_KEY, operationType);
			if(skipLogging(action)) {
				return;
			}
			subjectDto = (AbstractDto)content;
			targetDto = lookupService.lookupEmbeddedDto(subjectDto, IdmAuthorizationPolicy_.role.getName());
		} else if (content instanceof IdmIdentityRoleDto) {
			action = buildAction(ROLE_ASSIGNMENT_LEVEL_KEY, operationType);
			if(skipLogging(action)) {
				return;
			}
			AbstractDto dto = (AbstractDto)content;
			AbstractDto contractDto = lookupService.lookupEmbeddedDto(dto, IdmIdentityRole_.identityContract.getName());
			subjectDto = lookupService.lookupEmbeddedDto(dto, IdmIdentityRole_.role.getName());
			targetDto = lookupService.lookupEmbeddedDto(contractDto, IdmIdentityContract_.identity.getName());
		} else if (content instanceof IdmRoleRequestDto) {
			roleRequestSpecificLog((IdmRoleRequestDto)content, operationType,  transactionUuid, status, detail);
			return;
		} else if(content instanceof IdmConfigurationDto) {
			action = buildAction(CONFIGURATION_LEVEL_KEY, operationType);
			if(skipLogging(action)) {
				return;
			}
			targetDto = (AbstractDto)content;
		} else if (content instanceof IdmLongRunningTaskDto) {
			IdmLongRunningTaskDto dto = (IdmLongRunningTaskDto)content;
			lrtSpecificLog(dto, operationType, transactionUuid, status, detail);
			return;
		} else {
			return;
		}
		log(action, status, targetDto, subjectDto, transactionUuid, detail);
	}


	@Override
	public <DTO extends BaseDto> void log(String action, String result, DTO targetDto, DTO subjectDto, String transactionUuid, String detail) {
		if(skipLogging(action)) {
			return;
		}
		String targetName = null;
		String targetUuid = null;
		String subjectName = null;
		String subjectUuid = null;
		if (targetDto instanceof Codeable) {
			targetName = ((Codeable)targetDto).getCode();
		}
		
		if (targetDto instanceof BaseDto) {
			targetUuid = Objects.toString(((BaseDto)targetDto).getId(),"");
		}
		
		if (subjectDto instanceof Codeable) {
			subjectName = ((Codeable)subjectDto).getCode();
		}
		
		if (subjectDto instanceof BaseDto) {
			subjectUuid = Objects.toString(((BaseDto)subjectDto).getId(),"");
		}
		
		log(action, result, targetName, targetUuid, subjectName, subjectUuid, transactionUuid, detail);
	}


	@Override
	public void log(String action, String result, String targetName, String targetUuid, String subjectName, String subjectUuid, String transactionUuid, String detail) {
		if(skipLogging(action)) {
			return;
		}
		String performedByName = securityService.getCurrentUsername();
		String performedByUuid = Objects.toString(securityService.getCurrentId(),"");
		String fullAction = buildAction(ROOT_LEVEL_KEY, action);
		
		org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(fullAction);
		LOG.info(formatLog(fullAction, result, targetName, targetUuid, subjectName, subjectUuid, performedByName, performedByUuid, transactionUuid, detail));
	}


	@Override
	public String buildAction(String... levels) {
		StringBuilder sb = new StringBuilder();
		List<String> toPrint = new ArrayList<String>();
		for(int i = 0; i < levels.length; ++i) {
			if(StringUtils.isBlank(levels[i])) {
				continue;
			}
			toPrint.add(levels[i]);
		}
				
		for(int i = 0; i < toPrint.size(); ++i) {
			sb.append(toPrint.get(i));
			if(i < toPrint.size()-1) {
				sb.append(LEVEL_DELIMITER_ATTRIBUTE);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Internal log formatter.
	 * 
	 * @param action
	 * @param status
	 * @param targetName
	 * @param targetUuid
	 * @param subjectName
	 * @param subjectUuid
	 * @param performedByName
	 * @param performedByUuid
	 * @param reason
	 * @return
	 */
	private String formatLog(String action, String result, String targetName, String targetUuid, String subjectName, String subjectUuid, 
			String performedByName, String performedByUuid, String transactionUuid, String detail) {
		StringBuilder sb = new StringBuilder();
		sb
		.append(String.format("%s%s[%s] ", RESULT_ATTRIBUTE, PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(result,"")))
		.append(String.format("%s%s[%s] ", TARGET_NAME_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(targetName,"")))
		.append(String.format("%s%s[%s] ", TARGET_UUID_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(targetUuid,"")))
		.append(String.format("%s%s[%s] ", SUBJECT_NAME_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(subjectName,"")))
		.append(String.format("%s%s[%s] ", SUBJECT_UUID_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(subjectUuid,"")))
		.append(String.format("%s%s[%s] ", PERFORMED_BY_NAME_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(performedByName,"")))
		.append(String.format("%s%s[%s] ", PERFORMED_BY_UUID_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(performedByUuid,"")))
		.append(String.format("%s%s[%s] ", TRANSACTION_UUID_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(transactionUuid,"")))
		.append(String.format("%s%s[%s]", DETAIL_ATTRIBUTE , PARAMETER_DELIMITER_ATTRIBUTE, Objects.toString(detail,"")));
		return sb.toString();
	}
	
	/**
	 * Method provides specific logic for identity logging.
	 * 
	 * @param newDto
	 * @param oldDto
	 * @param operationType
	 * @param transactionUuid
	 * @param status
	 * @param reason
	 */
	private void identitySpecificLog(IdmIdentityDto newDto, IdmIdentityDto oldDto, String operationType, String transactionUuid, String status, String reason) {
		String action = buildAction(IDENTITY_LEVEL_KEY, operationType);
		if(skipLogging(action)) {
			return;
		}
		if(oldDto != null && StringUtils.isEmpty(reason)) {
			IdentityState newState = newDto.getState();
			IdentityState oldState = oldDto.getState();
			if (newState != null && oldState != null && oldState.isDisabled() != newState.isDisabled()) {
				reason =  newState.isDisabled() ? "DISABLED" : "ENABLED";
			}
		}			
		log(action, status, newDto, null, transactionUuid, reason);
	}

	/**
	 * Method provides specific logic for LRT logging.
	 * 
	 * @param dto
	 * @param operationType
	 * @param transactionUuid
	 * @param status
	 * @param reason
	 */
	private void lrtSpecificLog(IdmLongRunningTaskDto dto, String operationType, String transactionUuid, String status, String detail) {
		String action = buildAction(LRT_LEVEL_KEY, operationType);
		if(skipLogging(action)) {
			return;
		}
		OperationState state = dto.getResult().getState();
		String result = Objects.toString(state,"");
		status = OperationState.EXCEPTION == state ? FAILED_ACTION_STATUS : status;
		detail = StringUtils.isEmpty(detail) ? result : detail;
		log(action, status, dto, null, transactionUuid, detail);
	}

	/**
	 * Method provides specific logic for role request logging.
	 * 
	 * @param dto
	 * @param operationType
	 * @param transactionUuid
	 * @param status
	 * @param reason
	 */
	private void roleRequestSpecificLog(IdmRoleRequestDto dto, String operationType, String transactionUuid, String status, String detail) {
		String result = null;
		String action = buildAction(ROLE_REQUEST_LEVEL_KEY, operationType);
		if(skipLogging(action)) {
			return;
		}
		IdmConceptRoleRequestFilter filter = new IdmConceptRoleRequestFilter();
		filter.setRoleRequestId(dto.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.find(filter, null).getContent();
		for (IdmConceptRoleRequestDto concept : concepts) {
			IdmRoleDto roleDto = lookupService.lookupEmbeddedDto(concept, IdmConceptRoleRequest_.role.getName());
			if(StringUtils.isEmpty(detail)) {
				RoleRequestState state = concept.getState();
				result = Objects.toString(state,"");
				status = RoleRequestState.EXCEPTION == state ? FAILED_ACTION_STATUS : status; 
			} else {
				result = detail;
			}
			log(action, status, concept, roleDto, transactionUuid, result);
			result = null;
		}
	}
	
	
	/**
	 * Optimization method skipping the log process according to the configuration.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean skipLogging() {
		StringBuilder sb = new StringBuilder();
		sb.append(CONFIGURATION_PROPERTY_PREFIX);
		sb.append('.');
		sb.append(DefaultSiemLoggerManager.class.getCanonicalName());
		String value = configurationService.getValue(sb.toString());
		return  !isLoggingOnByLevel(value);
	}
	
	/**
	 * Optimization method skipping the log process according to the configuration.
	 *
	 * @param action
	 * @return
	 */
	private boolean skipLogging(String action) {
		boolean result = true;
		String levels[] = {};
		if(action!=null) {
			levels = action.split(Pattern.quote(String.valueOf(LEVEL_DELIMITER_ATTRIBUTE))); 
		}
		StringBuilder sb = new StringBuilder();
		sb.append(CONFIGURATION_PROPERTY_PREFIX);
		sb.append(LEVEL_DELIMITER_ATTRIBUTE);
		sb.append(ROOT_LEVEL_KEY);
		String value = configurationService.getValue(sb.toString());
		result = !isLoggingOnByLevel(value);
		for(int i=0; i < levels.length; ++i) {
			sb.append(LEVEL_DELIMITER_ATTRIBUTE);
			sb.append(levels[i]);
			value = configurationService.getValue(sb.toString());
			result = value == null ? result : !isLoggingOnByLevel(value);  
		}
		return result;
	}
	
	/**
	 * Test if logging is on according to the set level.
	 * 
	 * @param level
	 * @return
	 */
	private boolean isLoggingOnByLevel(String level) {
		if (level == null) {
			return false;
		}
		for (String allowed : LOG_ON_LEVELS_PROPERTY) {
			if (StringUtils.equalsIgnoreCase(level, allowed)) {
				return true;
			}
		}
		return false;
	}
	
}

package eu.bcvsolutions.idm.core.audit.service.impl;

import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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
	private ConfigurationService configurationService;

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
		
		if (targetDto != null) {
			targetUuid = Objects.toString(((BaseDto)targetDto).getId(),"");
		}
		
		if (subjectDto instanceof Codeable) {
			subjectName = ((Codeable)subjectDto).getCode();
		}
		
		if (subjectDto != null) {
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
		for (String level : levels) {
			if (StringUtils.isBlank(level)) {
				continue;
			}
			toPrint.add(level);
		}
				
		for(int i = 0; i < toPrint.size(); ++i) {
			sb.append(toPrint.get(i));
			if(i < toPrint.size()-1) {
				sb.append(LEVEL_DELIMITER_ATTRIBUTE);
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean skipLogging(String action) {
		boolean result = true;
		String[] levels = {};
		if(action!=null) {
			levels = action.split(Pattern.quote(String.valueOf(LEVEL_DELIMITER_ATTRIBUTE))); 
		}
		StringBuilder sb = new StringBuilder();
		sb.append(CONFIGURATION_PROPERTY_PREFIX);
		sb.append(LEVEL_DELIMITER_ATTRIBUTE);
		sb.append(ROOT_LEVEL_KEY);
		String value = configurationService.getValue(sb.toString());
		result = !isLoggingOnByLevel(value);
		for (String level : levels) {
			sb.append(LEVEL_DELIMITER_ATTRIBUTE);
			sb.append(level);
			value = configurationService.getValue(sb.toString());
			result = value == null ? result : !isLoggingOnByLevel(value);
		}
		return result;
	}
	
	/**
	 *  Internal log formatter.
	 *
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

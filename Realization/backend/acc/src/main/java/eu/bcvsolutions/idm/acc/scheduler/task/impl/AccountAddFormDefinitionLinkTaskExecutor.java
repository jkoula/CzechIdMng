package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.beust.jcommander.internal.Maps;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Add account form definition links. The form definitions are needed to manually set the account attribute's values. This task is used for migration only.
 * 
 * @author Tomáš Doischer
 *
 */
@DisallowConcurrentExecution
@Component(AccountAddFormDefinitionLinkTaskExecutor.TASK_NAME)
public class AccountAddFormDefinitionLinkTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	public static final String TASK_NAME = "acc-account-add-form-definition-link-long-running-task";
	private static final Logger LOG = LoggerFactory.getLogger(AccountAddFormDefinitionLinkTaskExecutor.class);
	
	public static final String PARAM_SYSTEM = "system";
	//
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccSchemaFormAttributeService schemaFormAttributeService;
	
	private UUID systemId;
	//
	@Override
	public String getName() {
		return TASK_NAME;
	}
	@Override
	public Boolean process() {
		this.counter = 0L;
		Map<SysSystemMappingDto, UUID> mappingToFormDefinitionMap = Maps.newHashMap();
		
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setHasFormDefinition(Boolean.FALSE);
		if (systemId != null) {
			accountFilter.setSystemId(systemId);
		}
		
		Pageable pageable = PageRequest.of(0, 100);
		do {
			Page<AccAccountDto> accounts = accountService.find(accountFilter, pageable, IdmBasePermission.UPDATE);
			if (count == null) {
				count = accounts.getTotalElements();
			}
			boolean canContinue = true;
			for (Iterator<AccAccountDto> i = accounts.iterator(); i.hasNext() && canContinue;) {
				// set the form definition
				AccAccountDto account = i.next();
				SysSystemMappingDto mapping = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class, null);
				if (mapping == null) {
					count--;
					continue;
				}
				UUID formDefinitionId = mappingToFormDefinitionMap.get(mapping);
				if (formDefinitionId == null) {
					IdmFormDefinitionDto formDefinition = schemaFormAttributeService.getSchemaFormDefinition(mapping);
					if (formDefinition != null && formDefinition.getId() != null) {
						formDefinitionId = formDefinition.getId();
						mappingToFormDefinitionMap.put(mapping, formDefinitionId);
					}
				}
				
				if (formDefinitionId != null) {
					LOG.info("Adding schema form definition id [{}] to account [{}].", formDefinitionId, account.getId());
					account.setFormDefinition(formDefinitionId);
					accountService.save(account, IdmBasePermission.UPDATE);
				}
				//
				++counter;
				canContinue = updateState();
			}			
			pageable = accounts.hasNext() && canContinue ? accounts.nextPageable() : null;
		} while (pageable != null);
		
		return Boolean.TRUE;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();

		IdmFormAttributeDto systemAttribute = new IdmFormAttributeDto(PARAM_SYSTEM, PARAM_SYSTEM,
				PersistentType.UUID, AccFaceType.SYSTEM_SELECT);
		formAttributes.add(systemAttribute);
		return formAttributes;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> props = super.getProperties();
		props.put(PARAM_SYSTEM, systemId);

		return props;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		systemId = getParameterConverter().toUuid(properties, PARAM_SYSTEM);
	}
}

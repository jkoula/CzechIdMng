package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Save account processor
 * 
 * @author Svanda
 * @author Roman Kucera
 */
@Component("accAccountSaveProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class AccountSaveProcessor extends CoreEventProcessor<AccAccountDto> implements AccountProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountSaveProcessor.class);

	private static final String PROCESSOR_NAME = "account-save-processor";
	private final AccAccountService service;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private AccSchemaFormAttributeService schemaFormAttributeService;

	@Autowired
	public AccountSaveProcessor(AccAccountService service) {
		super(AccountEventType.CREATE, AccountEventType.UPDATE);
		//
		Assert.notNull(service, "Service is required.");
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		AccAccountDto entity = event.getContent();
		SysSystemMappingDto mapping = null;
		SysSystemDto systemDto = null;

		// no mapping relation, add default backward compatible mapping
		if (entity.getSystemMapping() == null) {
			systemDto = systemService.get(entity.getSystem());
			List<SysSystemMappingDto> systemMappings = systemMappingService.findBySystem(systemDto,
					SystemOperationType.PROVISIONING, entity.getEntityType());

			if (systemMappings != null && !systemMappings.isEmpty()) {
				LOG.debug("Setting default provisioning mapping for account with system entity {} of type {} on system {}.",
						entity.getSystemEntity() ,entity.getEntityType(), entity.getSystem());
				entity.setSystemMapping(systemMappings.get(0).getId());
				mapping = systemMappings.get(0);
			} else {
				LOG.debug("No provisioning mapping found for account with system entity {} of type {} on system {}.",
						entity.getSystemEntity(), entity.getEntityType(), entity.getSystem());
			}
		}
		
		
		if (entity.getFormDefinition() == null) {
			SysSchemaObjectClassDto schema = null;
			if (mapping != null) {
				schema = schemaObjectClassService.get(mapping.getObjectClass());
			} else {
				// no provisioning mapping just yet so we need to find the object class for synchronization
				schema = getObjectClassSchema(entity);
			}
			
			if (schema != null) {
				IdmFormDefinitionDto formDefinition = schemaFormAttributeService.getSchemaFormDefinition(systemDto, schema);
				if (formDefinition != null) {
					entity.setFormDefinition(formDefinition.getId());
				}
			}
		}

		entity = service.saveInternal(entity);
		event.setContent(entity);

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Try to find the object class schema for the account.
	 * 
	 * @param entity
	 * @return
	 */
	private SysSchemaObjectClassDto getObjectClassSchema(AccAccountDto entity) {
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(entity.getSystem());
		mappingFilter.setEntityType(entity.getEntityType());
		// we will use the first one found - this should almost certainly be correct
		SysSystemMappingDto mapping = systemMappingService.find(mappingFilter, null).stream().findFirst().orElse(null);
		
		if (mapping != null && mapping.getObjectClass() != null) {
			return schemaObjectClassService.get(mapping.getObjectClass());
		}
		
		return null;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}

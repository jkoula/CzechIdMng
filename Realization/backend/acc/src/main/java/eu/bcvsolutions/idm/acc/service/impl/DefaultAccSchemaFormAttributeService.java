package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccSchemaFormAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.repository.AccSchemaFormAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.filter.AccSchemaFormAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

@Service("accSchemaFormAttributeService")
public class DefaultAccSchemaFormAttributeService extends AbstractEventableDtoService<AccSchemaFormAttributeDto, AccSchemaFormAttribute, AccSchemaFormAttributeFilter>
implements AccSchemaFormAttributeService {

	@Autowired @Lazy
	private IdmFormAttributeService formAttributeService;
	@Autowired @Lazy
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private SysSchemaObjectClassService sysSchemaObjectClassService;
	
	public DefaultAccSchemaFormAttributeService(AccSchemaFormAttributeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.ACCOUNTFORMVALUE, getEntityClass());
	}

	@Override
	public AccSchemaFormAttributeDto createFormAttribute(SysSchemaAttributeDto schemaAttribute) {
		SysSchemaObjectClassDto objectClass = sysSchemaObjectClassService.get(schemaAttribute.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(objectClass, SysSchemaObjectClass_.system, SysSystemDto.class);
		
		IdmFormDefinitionDto schemaFormDefinition = getSchemaFormDefinition(system, objectClass);
		if (schemaFormDefinition == null) {
			schemaFormDefinition = createSchemaFormDefinition(system, objectClass);
		}
		
		return createSchemaFormAttribute(schemaAttribute, schemaFormDefinition);
	}

	@Override
	public AccSchemaFormAttributeDto createSchemaFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition) {
		IdmFormAttributeDto formAttribute = findFormAttribute(schemaAttribute, schemaFormDefinition);
				
		if (formAttribute == null) {
			// create
			formAttribute = createFormAttribute(schemaAttribute, schemaFormDefinition);
		} else {
			// update
			updateFormAttribute(schemaAttribute, schemaFormDefinition, formAttribute);
			return null; // TODO this is temporary
		}
				
		
		AccSchemaFormAttributeDto schemaFormAttribute = new AccSchemaFormAttributeDto();
		schemaFormAttribute.setFormAttribute(formAttribute.getId());
		schemaFormAttribute.setSchema(schemaAttribute.getObjectClass());
		
		return this.save(schemaFormAttribute);
	}

	private IdmFormAttributeDto findFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition) {
		//TODO maybe use the new filter and implement toPredicate
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setCode(schemaAttribute.getName());
		filter.setDefinitionId(schemaFormDefinition.getId());
		return formAttributeService.find(filter, null).stream().findFirst().orElse(null);
	}

	private IdmFormAttributeDto updateFormAttribute(SysSchemaAttributeDto schemaAttribute, IdmFormDefinitionDto schemaFormDefinition,
			IdmFormAttributeDto formAttribute) {
		formAttribute.setPersistentType(getPersistentType(schemaAttribute));
		
		return formAttributeService.save(formAttribute);
	}

	private IdmFormAttributeDto createFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition) {
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto();
		formAttribute.setFormDefinition(schemaFormDefinition.getId());
		formAttribute.setCode(schemaAttribute.getName());
		formAttribute.setName(schemaAttribute.getName());
		formAttribute.setMultiple(schemaAttribute.isMultivalued());
		formAttribute.setReadonly(!schemaAttribute.isCreateable());
		formAttribute.setConfidential(schemaAttribute.getClassType().equals(GuardedString.class.getCanonicalName()));
		formAttribute.setPersistentType(getPersistentType(schemaAttribute));
		// TODO maybe add more / remove some?
		
		return formAttributeService.save(formAttribute);
	}
	
	private PersistentType getPersistentType(SysSchemaAttributeDto schemaAttribute) {
		switch(schemaAttribute.getClassType()) {
		case "java.lang.String":
			return PersistentType.SHORTTEXT;
		case "java.lang.Boolean":
			return PersistentType.BOOLEAN;
		default:
			return PersistentType.SHORTTEXT;
		}
	}

	private IdmFormDefinitionDto getSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setCode(createFormDefinitionName(system, objectClass));
		return formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
	}
	
	private IdmFormDefinitionDto createSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
		String name = createFormDefinitionName(system, objectClass);
		formDefinition.setCode(name);
		formDefinition.setName(name);
		formDefinition.setType(SysSchemaObjectClass.class.getCanonicalName());
		
		return formDefinitionService.save(formDefinition);
	}
	
	private String createFormDefinitionName(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("account-eav-definition:systemId=");
		sb.append(system.getId());
		sb.append(":object-class-name=");
		sb.append(objectClass.getObjectClassName());
		
		return sb.toString();
	}
}

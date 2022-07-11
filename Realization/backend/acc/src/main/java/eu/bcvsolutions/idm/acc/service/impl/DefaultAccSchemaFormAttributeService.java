package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccSchemaFormAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.repository.AccSchemaFormAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.filter.AccSchemaFormAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
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
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private FormService formService;
	
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
	public List<AccSchemaFormAttributeDto> createFormAttributes(SysSchemaObjectClassDto objectClass) {
		List<AccSchemaFormAttributeDto> schemaFormAttributes = Lists.newArrayList();
		
		SysSystemDto system = DtoUtils.getEmbedded(objectClass, SysSchemaObjectClass_.system, SysSystemDto.class);
		
		IdmFormDefinitionDto schemaFormDefinition = getSchemaFormDefinition(system, objectClass);
		if (schemaFormDefinition == null) {
			schemaFormDefinition = createSchemaFormDefinition(system, objectClass);
		}
		
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(objectClass.getId());
		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		
		for (SysSchemaAttributeDto schemaAttribute : schemaAttributes) {
			schemaFormAttributes.add(createSchemaFormAttribute(schemaAttribute, schemaFormDefinition));
		}
		
		return schemaFormAttributes;
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
		case "java.lang.Long":
			return PersistentType.LONG;
		case "java.lang.Character":
			return PersistentType.CHAR;
		case "java.lang.Double":
			return PersistentType.DOUBLE;
		case "java.lang.Float":
			return PersistentType.DOUBLE;
		case "java.lang.Integer":
			return PersistentType.INT;
		case "java.lang.Boolean":
			return PersistentType.BOOLEAN;
		case "java.lang.Byte":
			return PersistentType.BYTEARRAY;
		case "java.lang.BigDecimal":
			return PersistentType.DOUBLE;
		case "java.lang.BigInteger":
			return PersistentType.INT;
		case "java.lang.GuardedString":
			return PersistentType.SHORTTEXT;
		case "java.lang.Map":
			return PersistentType.TEXT;
		case "[B":
			return PersistentType.BYTEARRAY;
		default:
			return PersistentType.SHORTTEXT;
		}

	}

	@Override
	public IdmFormDefinitionDto getSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setCode(createFormDefinitionCode(system, objectClass));
		return formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
	}
	
	@Override
	public IdmFormDefinitionDto getSchemaFormDefinition(SysSchemaObjectClassDto objectClass) {
		SysSystemDto system = DtoUtils.getEmbedded(objectClass, SysSchemaObjectClass_.system, SysSystemDto.class);
		
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setCode(createFormDefinitionCode(system, objectClass));
		return formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
	}
	
	@Override
	public IdmFormDefinitionDto getSchemaFormDefinition(SysSystemMappingDto mapping) {
		SysSchemaObjectClassDto objectClass = DtoUtils.getEmbedded(mapping, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
		return getSchemaFormDefinition(objectClass);
	}
	
	private IdmFormDefinitionDto createSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
		formDefinition.setCode(createFormDefinitionCode(system, objectClass));
		formDefinition.setName(createFormDefinitionName(system, objectClass));
		formDefinition.setType(AccAccount.class.getCanonicalName());
		
		return formDefinitionService.save(formDefinition);
	}
	
	@Override
	public String createFormDefinitionCode(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("account-eav-definition:systemId=");
		sb.append(system.getId());
		sb.append(":object-classId=");
		sb.append(objectClass.getId());
		
		return sb.toString();
	}
	
	private String createFormDefinitionName(SysSystemDto system, SysSchemaObjectClassDto objectClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("Definition for EAV attributes for accounts on system ");
		sb.append(system.getName());
		sb.append(" and object class name ");
		sb.append(objectClass.getObjectClassName());
		
		return sb.toString();
	}

	@Override
	public IdmFormInstanceDto getFormInstanceForAccount(AccAccountDto account) {
		// TODO this is not going to work very well
		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setText(system.getName());
		IdmFormDefinitionDto formDefinition = formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
		
		return formService.getFormInstance(account, formDefinition);
	}
}

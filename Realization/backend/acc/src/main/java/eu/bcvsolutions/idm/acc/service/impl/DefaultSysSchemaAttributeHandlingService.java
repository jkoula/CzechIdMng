package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.bcvsolutions.idm.acc.domain.MappingAttribute;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeHandlingService
		extends AbstractReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter>
		implements SysSchemaAttributeHandlingService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DefaultSysSchemaAttributeHandlingService.class);

	private final SysSchemaAttributeHandlingRepository repository;
	private final GroovyScriptService groovyScriptService;
	private final FormService formService;
	private final IdmFormAttributeService formAttributeService;
	private final SysRoleSystemAttributeRepository roleSystemAttributeRepository;

	@Autowired
	public DefaultSysSchemaAttributeHandlingService(
			SysSchemaAttributeHandlingRepository repository,
			GroovyScriptService groovyScriptService, 
			FormService formService,
			IdmFormAttributeService formAttributeService, 
			SysRoleSystemAttributeRepository roleSystemAttributeRepository) {
		super(repository);
		//
		Assert.notNull(groovyScriptService);
		Assert.notNull(formService);
		Assert.notNull(formAttributeService);
		Assert.notNull(roleSystemAttributeRepository);
		//
		this.formService = formService;
		this.repository = repository;
		this.groovyScriptService = groovyScriptService;
		this.formAttributeService = formAttributeService;
		this.roleSystemAttributeRepository = roleSystemAttributeRepository;
	}

	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling) {
		Assert.notNull(entityHandling);

		SchemaAttributeHandlingFilter filter = new SchemaAttributeHandlingFilter();
		filter.setEntityHandlingId(entityHandling.getId());
		Page<SysSchemaAttributeHandling> page = repository.find(filter, null);
		return page.getContent();
	}

	@Override
	public Object transformValueToResource(Object value, MappingAttribute attributeHandling,
			AbstractEntity entity) {
		Assert.notNull(attributeHandling);

		return transformValueToResource(value, attributeHandling.getTransformToResourceScript(), entity,
				attributeHandling.getSchemaAttribute().getObjectClass().getSystem());
	}

	@Override
	public Object transformValueToResource(Object value, String script, AbstractEntity entity, SysSystem system) {
		if (!StringUtils.isEmpty(script)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, system);
			variables.put(ENTITY_KEY, entity);
			return groovyScriptService.evaluate(script, variables);
		}

		return value;
	}

	@Override
	public Object transformValueFromResource(Object value, MappingAttribute attributeHandling,
			List<IcfAttribute> icfAttributes) {
		Assert.notNull(attributeHandling);

		transformValueFromResource(value, attributeHandling.getTransformFromResourceScript(), icfAttributes,
				attributeHandling.getSchemaAttribute().getObjectClass().getSystem());

		return value;
	}

	@Override
	public Object transformValueFromResource(Object value, String script, List<IcfAttribute> icfAttributes,
			SysSystem system) {

		if (!StringUtils.isEmpty(script)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, system);
			variables.put(ICF_ATTRIBUTES_KEY, icfAttributes);
			return groovyScriptService.evaluate(script, variables);
		}

		return value;
	}

	@Override
	@Transactional
	public SysSchemaAttributeHandling save(SysSchemaAttributeHandling entity) {
		// We will do script validation (on compilation errors), before save
		// attribute handling

		if (entity.getTransformFromResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformFromResourceScript());
		}
		if (entity.getTransformToResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformToResourceScript());
		}
		if (entity.isExtendedAttribute() && FormableEntity.class.isAssignableFrom(entity.getSystemEntityHandling().getEntityType().getEntityType())) {
			IdmFormDefinition definition = formService
					.getDefinition(entity.getSystemEntityHandling().getEntityType().getEntityType().getCanonicalName());
			if (definition != null) {
				IdmFormAttribute defAttribute = definition.getMappedAttributeByName(entity.getIdmPropertyName());
				if (defAttribute == null) {
					log.info(MessageFormat.format(
							"IdmFormAttribute for identity and property {0} not found. We will create definition now.",
							entity.getIdmPropertyName()));

					IdmFormAttribute attributeDefinition = convertSchemaAttributeHandling(entity, definition);

					definition.getFormAttributes().add(attributeDefinition);
					formAttributeService.save(attributeDefinition);
				}
			}
		}
		return super.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttributeHandling entity) {
		Assert.notNull(entity);
		// delete attributes
		roleSystemAttributeRepository.deleteBySchemaAttributeHandling(entity);
		//
		super.delete(entity);
	}

	/**
	 * Convert schema attribute handling to Form attribute
	 * 
	 * @param entity
	 * @param definition
	 * @return
	 */
	private IdmFormAttribute convertSchemaAttributeHandling(MappingAttribute entity,
			IdmFormDefinition definition) {

		SysSchemaAttribute schemaAttribute = entity.getSchemaAttribute();
		IdmFormAttribute attributeDefinition = new IdmFormAttribute();
		attributeDefinition.setSeq((short) 0);
		attributeDefinition.setName(entity.getIdmPropertyName());
		attributeDefinition.setDisplayName(entity.getName());
		// TODO: refactor converters to stand alone service
		attributeDefinition.setPersistentType(DefaultSysSystemService.convertPropertyType(schemaAttribute.getClassType()));
		attributeDefinition.setRequired(schemaAttribute.isRequired());
		attributeDefinition.setMultiple(schemaAttribute.isMultivalued());
		attributeDefinition.setReadonly(!schemaAttribute.isUpdateable());
		attributeDefinition.setConfidential(entity.isConfidentialAttribute());
		attributeDefinition.setFormDefinition(definition);
		attributeDefinition.setDescription(
				MessageFormat.format("Genereted by schema attribute {0} in resource {1}. Created by SYSTEM.",
						schemaAttribute.getName(), schemaAttribute.getObjectClass().getSystem().getName()));
		return attributeDefinition;
	}

}

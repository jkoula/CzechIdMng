package eu.bcvsolutions.idm.acc;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariDataSource;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment_;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.config.datasource.CoreEntityManager;
import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayMigrationStrategy;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import joptsimple.internal.Strings;

/**
 * Acc / Provisioning test helper
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 */
@Primary
@Component("accTestHelper")
public class DefaultAccTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {
	
	@Autowired
	@CoreEntityManager
	private EntityManager entityManager;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private FormService formService;
	@Autowired private DataSource dataSource;
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private AccAccountService accountService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private DefaultSysSystemMappingService mappingService;
	@Autowired private ApplicationContext context;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSyncItemLogService syncItemLogService;
	@Autowired private SysSyncActionLogService syncActionLogService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private AccUniformPasswordService uniformPasswordService;
	@Autowired private SysSystemGroupService systemGroupService;
	@Autowired private SysSystemOwnerService systemOwnerService;
	@Autowired private SysSystemOwnerRoleService systemOwnerRoleService;
	@Autowired private AccAccountRoleAssignmentService accAccountRoleAssignmentService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private AccAccountConceptRoleRequestService accountConceptRoleRequestService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource findResource(String uid) {
		return entityManager.find(TestResource.class, uid);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource saveResource(TestResource testResource) {
		entityManager.persist(testResource);
		//
		return testResource;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestResource.TABLE_NAME);
		q.executeUpdate();
	}
	
	/**
	 * Create test system connected to same database (using configuration from dataSource)
	 * Generated system name will be used.
	 * 
	 * @return
	 */
	@Override
	public SysSystemDto createSystem(String tableName) {
		return createSystem(tableName, null);
	}
	
	/**
	 * 
	 * 
	 * @param tableName
	 * @param systemName
	 * @return
	 */
	@Override
	public SysSystemDto createSystem(String tableName, String systemName) {
		return this.createSystem(tableName, systemName, "status", "name");
	}
	
	/**
	 * 
	 * 
	 * @param tableName
	 * @param systemName
	 * @return
	 */
	@Override
	@SuppressWarnings("deprecation")
	public SysSystemDto createSystem(String tableName, String systemName, String statusColumnName, String keyColumnName) {
		// create owner
		SysSystemDto system = new SysSystemDto();
		system.setName(systemName == null ? tableName + "_" + System.currentTimeMillis() : systemName);

		system.setConnectorKey(new SysConnectorKeyDto(systemService.getTestConnectorKey()));

		system = systemService.save(system);

		IdmFormDefinitionDto savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());

		List<IdmFormValueDto> values = new ArrayList<>();

		IdmFormValueDto jdbcUrlTemplate = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue(((HikariDataSource) dataSource).getJdbcUrl());
		values.add(jdbcUrlTemplate);
		IdmFormValueDto jdbcDriver = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcDriver"));
		jdbcDriver.setValue(((HikariDataSource) dataSource).getDriverClassName());
		values.add(jdbcDriver);

		IdmFormValueDto user = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("user"));
		user.setValue(((HikariDataSource) dataSource).getUsername());
		values.add(user);
		IdmFormValueDto password = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("password"));
		password.setValue(((HikariDataSource) dataSource).getPassword());
		values.add(password);
		IdmFormValueDto table = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("table"));
		table.setValue(tableName);
		values.add(table);
		if(!Strings.isNullOrEmpty(keyColumnName)) {
			IdmFormValueDto keyColumn = new IdmFormValueDto(
					savedFormDefinition.getMappedAttributeByCode("keyColumn"));
			keyColumn.setValue(keyColumnName);
			values.add(keyColumn);
		}
		IdmFormValueDto passwordColumn = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		IdmFormValueDto allNative = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("allNative"));
		allNative.setValue(true);
		values.add(allNative);
		IdmFormValueDto rethrowAllSQLExceptions = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(true);
		values.add(rethrowAllSQLExceptions);
		if(!Strings.isNullOrEmpty(statusColumnName)) {
			IdmFormValueDto statusColumn = new IdmFormValueDto(
					savedFormDefinition.getMappedAttributeByCode("statusColumn"));
			statusColumn.setValue(statusColumnName);
			values.add(statusColumn);
		}
		IdmFormValueDto disabledStatusValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		IdmFormValueDto enabledStatusValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);
		IdmFormValueDto changeLogColumnValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("changeLogColumn"));
		changeLogColumnValue.setValue(null);
		values.add(changeLogColumnValue);
		
		formService.saveValues(system, savedFormDefinition, values);

		return system;
	}
	
	@Override
	public SysSystemDto createTestResourceSystem(boolean withMapping) {
		return createTestResourceSystem(withMapping, null);
	}
	
	@Override
	public SysSystemDto createTestResourceSystem(boolean withMapping, String systemName) {
		// create test system
		SysSystemDto system = createSystem(TestResource.TABLE_NAME, systemName);
		//
		if (withMapping) {
			createMapping(system);
		}
		//
		return system;
	}

	@Override
	public SysSystemMappingDto createMapping(SysSystemDto system, String entityType, AccountType accountType) {
		//
		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);
		//
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(entityType);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0).getId());
		systemMapping.setAccountType(accountType);
		systemMapping = systemMappingService.save(systemMapping);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for(SysSchemaAttributeDto schemaAttr : schemaAttributesPage) {
			if (ATTRIBUTE_MAPPING_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.username.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_ENABLE.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName("disabled");
				attributeMapping.setTransformToResourceScript("return String.valueOf(!attributeValue);");
				attributeMapping.setTransformFromResourceScript("return String.valueOf(attributeValue);");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_PASSWORD.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("password");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setPasswordAttribute(true);
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_FIRSTNAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.firstName.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_LASTNAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.lastName.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_EMAIL.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.email.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			}
		}
		return systemMapping;
	}

	@Override
	public SysSystemMappingDto createMapping(SysSystemDto system, AccountType accountType) {
		return createMapping(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, accountType);
	}

	@Override
	public SysSystemMappingDto createMapping(SysSystemDto system, String entityType) {
		return createMapping(system, entityType, AccountType.PERSONAL);
	}

	@Override
	public SysSystemMappingDto createMapping(SysSystemDto system) {
		return createMapping(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, AccountType.PERSONAL);
	}

	@Override
	public SysSystemMappingDto getDefaultMapping(SysSystemDto system) {
		Assert.notNull(system, "System is required to get mapping.");
		//
		return getDefaultMapping(system.getId());
	}
	
	@Override
	public SysSystemMappingDto getDefaultMapping(UUID systemId) {
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystemId(systemId, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		if(mappings.isEmpty()) {
			throw new CoreException(String.format("Default mapping for system[%s] not found", systemId));
		}
		//
		return mappings.get(0);
	}
	
	@Override
	public SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system) {
		return createRoleSystem(role, system, AccountType.PERSONAL);
	}

	@Override
	public SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system, AccountType accountType) {
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setRole(role.getId());
		roleSystem.setSystem(system.getId());
		roleSystem.setCreateAccountByDefault(true);
		// default mapping
		SysSystemMappingFilter systemMappingFilter = new SysSystemMappingFilter();
		systemMappingFilter.setSystemId(system.getId());
		systemMappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemMappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		systemMappingFilter.setAccountType(accountType);
		List<SysSystemMappingDto> mappings = systemMappingService.find(systemMappingFilter, null).getContent();
		// required ...
		roleSystem.setSystemMapping(mappings.get(0).getId());
		//
		return roleSystemService.save(roleSystem);
	}

	@Override
	public SysSystemEntityDto createSystemEntity(SysSystemDto system) {
		SysSystemEntityDto systemEntity = new SysSystemEntityDto(createName(), IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemEntity.setSystem(system.getId());
		systemEntity.setWish(true);
		return systemEntityService.save(systemEntity);
	}

	@Override
	public AccIdentityAccountDto createIdentityAccount(SysSystemDto system, IdmIdentityDto identity) {
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid(identity.getUsername());
		account.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		account = accountService.save(account);

		AccIdentityAccountDto accountIdentity = new AccIdentityAccountDto();
		accountIdentity.setIdentity(identity.getId());
		accountIdentity.setOwnership(true);
		accountIdentity.setAccount(account.getId());

		return identityAccountService.save(accountIdentity);
	}

	@Override
	public SysSystemMappingDto createMappingSystem(String systemEntityType, SysSchemaObjectClassDto objectClass) {
		// system mapping
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		mapping.setName(createName());
		mapping.setEntityType(systemEntityType);
		mapping.setObjectClass(objectClass.getId());
		mapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		mapping.setAccountType(AccountType.PERSONAL);
		//
		return mappingService.save(mapping);
	}

	@Override
	public void startSynchronization(AbstractSysSyncConfigDto config) {
		Assert.notNull(config, "Sync config is required to be start.");
		SynchronizationSchedulableTaskExecutor lrt = context.getAutowireCapableBeanFactory()
		.createBean(SynchronizationSchedulableTaskExecutor.class);
		lrt.init(ImmutableMap.of(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, config.getId().toString()));
		lrt.process();
	}
	
	@Override
	public SysSyncLogDto checkSyncLog(AbstractSysSyncConfigDto config, SynchronizationActionType actionType, int count,
			OperationResultType resultType) {
		SysSyncConfigFilter logFilter = new SysSyncConfigFilter();
		logFilter.setId(config.getId());
		logFilter.setIncludeLastLog(Boolean.TRUE);
		List<AbstractSysSyncConfigDto> configs = syncConfigService.find(logFilter, null).getContent();
		assertEquals(1, configs.size());
		SysSyncLogDto log = configs.get(0).getLastSyncLog();
		if (actionType == null) {
			return log;
		}

		SysSyncActionLogFilter actionLogFilter = new SysSyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLogDto> actions = syncActionLogService.find(actionLogFilter, null).getContent();

		SysSyncActionLogDto actionLog = actions.stream().filter(action -> {
			if (resultType == null) {
				return actionType == action.getSyncAction();
			}
			return actionType == action.getSyncAction() && action.getOperationResult() == resultType;
		}).findFirst().get();

		if (resultType != null) {
			assertEquals(resultType, actionLog.getOperationResult());
		}
		SysSyncItemLogFilter itemLogFilter = new SysSyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLogDto> items = syncItemLogService.find(itemLogFilter, null).getContent();
		assertEquals(count, items.size());

		return log;
	}
	
	/**
	 * Schema is generated in lower case for postgresql.
	 * 
	 * @param columnName
	 * @return
	 */
	@Override
	public String getSchemaColumnName(String columnName) {
		if (columnName.equals(ATTRIBUTE_MAPPING_NAME)
				|| columnName.equals(ATTRIBUTE_MAPPING_ENABLE)
				|| columnName.equals(ATTRIBUTE_MAPPING_PASSWORD)) {
			// reserved names
			return columnName;
		}
		//
        String dbName = getDatabaseName();
        if (dbName.equals(IdmFlywayMigrationStrategy.POSTGRESQL_DBNAME)) {
			return columnName.toLowerCase();
		}
		//
		return columnName;
	}


	/**
	 * Manual delete of all automatic roles, sync, mappings. Because previous tests didn't make a delete well.
	 */
	@Override
	public void cleaner() {
		// Delete all automatic roles.
		roleTreeNodeService.deleteAll(roleTreeNodeService.find(new IdmRoleTreeNodeFilter(), null).getContent());
		automaticRoleAttributeService.deleteAll(automaticRoleAttributeService.find(new IdmRoleTreeNodeFilter(), null).getContent());
		// Delete all syncs.
		syncConfigService.deleteAll(syncConfigService.find(new SysSyncConfigFilter(), null).getContent());
		// Delete all groups.
		systemGroupService.deleteAll(systemGroupService.find(new SysSystemGroupFilter(), null));
		// Delete all mappings.
		systemMappingService.find(new SysSystemMappingFilter(), null).getContent().forEach(sysSystemMappingDto -> {
			try {
				systemMappingService.delete(sysSystemMappingDto);
			} catch (Exception e) {
				// we don't care about exception, because some test broke some data, continue with next record
			}
		});
		// Delete all uniform password definitions.
		uniformPasswordService.deleteAll(uniformPasswordService.find(new AccUniformPasswordFilter(), null).getContent());
	}

	/**
	 * create system owner by identity
	 * @param system
	 * @param owner
	 * @return
	 */
	@Override
	public SysSystemOwnerDto createSystemOwner(SysSystemDto system, IdmIdentityDto owner) {
		SysSystemOwnerDto dto = new SysSystemOwnerDto();
		dto.setSystem(system.getId());
		dto.setOwner(owner.getId());
		return systemOwnerService.save(dto);
	}

	/**
	 * create system owner by role
	 * @param system
	 * @param owner
	 * @return
	 */
	@Override
	public SysSystemOwnerRoleDto createSystemOwnerRole(SysSystemDto system, IdmRoleDto owner) {
		SysSystemOwnerRoleDto dto = new SysSystemOwnerRoleDto();
		dto.setSystem(system.getId());
		dto.setOwnerRole(owner.getId());
		//
		return systemOwnerRoleService.save(dto);
	}

	@Override
	public void deleteSystem(UUID systemId) {
		systemService.deleteById(systemId);
	}

	@Override
	public AccAccountRoleAssignmentDto createAccountRoleAssignment(UUID accountId, UUID roleId) {
		return createAccountRoleAssignment(accountId, roleId, null, null);
	}

	@Override
	public AccAccountRoleAssignmentDto createAccountRoleAssignment(UUID accountId, UUID roleId, LocalDate from, LocalDate to) {
		AccAccountRoleAssignmentDto roleAssignmentDto = new AccAccountRoleAssignmentDto();
		roleAssignmentDto.setAccAccount(accountId);
		roleAssignmentDto.setRole(roleId);
		roleAssignmentDto.setValidFrom(from);
		roleAssignmentDto.setValidTill(to);
		return accAccountRoleAssignmentService.save(roleAssignmentDto);
	}

	@Override
	public void removeAccountRoleAssignment(AccAccountRoleAssignmentDto roleAssignment) {
		accAccountRoleAssignmentService.delete(roleAssignment);
	}

	@Override
	public AccAccountDto createAccount() {
		return createAccount(null);
	}
	@Override
	public AccAccountDto createAccount(GuardedString password){
		IdmIdentityDto identity = this.createIdentity(password);
		//
		SysSystemDto system = this.createSystem("test_resource");
		this.createMapping(system);
		IdmRoleDto roleOne = this.createRole();
		this.createRoleSystem(roleOne, system);
		//
		this.createIdentityRole(identity, roleOne);
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(identity.getUsername());
		AccAccountDto account = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		return account;
	}

	@Override
	public void assignRoleToAccountViaRequest(AccAccountDto accAccountDto, boolean waitTillRequestExecuted, UUID... roleIds) {
		this.assignRoleToAccountViaRequest(accAccountDto, null, null, waitTillRequestExecuted, roleIds);
	}

	@Override
	public void assignRoleToAccountViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, UUID... roleIds) {
		IdmRoleRequestDto roleRequest = createRoleRequest(getAccountOwner(accAccountDto.getId()));
		final UUID roleRequestId = roleRequest.getId();
		for (UUID roleId : roleIds) {
			createAccountConceptRoleRequest(roleRequestId, roleId, accAccountDto.getId(), null, ConceptRoleRequestOperation.ADD, validFrom, validTill);
		}
		IdmRoleRequestDto processedRoleRequest = this.executeRequest(roleRequest, false, true);

		if (waitTillRequestExecuted) {
			this.waitForResult(res -> {
				return roleRequestService.get(processedRoleRequest.getId()).getState() != RoleRequestState.EXECUTED;
			}, 150, 5);
		}
	}

	@Override
	public void updateAssignedAccountRoleViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, AccAccountRoleAssignmentDto roleAssignment) {
		IdmRoleRequestDto roleRequest = createRoleRequest(getAccountOwner(accAccountDto.getId()));
		final UUID roleRequestId = roleRequest.getId();
		createAccountConceptRoleRequest(roleRequestId, roleAssignment.getRole(), accAccountDto.getId(), roleAssignment.getId(), ConceptRoleRequestOperation.UPDATE, validFrom, validTill);
		IdmRoleRequestDto processedRoleRequest = this.executeRequest(roleRequest, false, true);

		if (waitTillRequestExecuted) {
			this.waitForResult(res -> {
				return roleRequestService.get(processedRoleRequest.getId()).getState() != RoleRequestState.EXECUTED;
			}, 150, 5);
		}
	}

	@Override
	public void updateAssignedAccountRoleViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, UUID roleId) {
		AccAccountRoleAssignmentFilter accountRoleFilter = new AccAccountRoleAssignmentFilter();
		accountRoleFilter.setAccountId(accAccountDto.getId());
		accountRoleFilter.setRoleId(roleId);
		List<AccAccountRoleAssignmentDto> accountRoles = accAccountRoleAssignmentService.find(accountRoleFilter, null).getContent();
		accountRoles.forEach(aR -> this.updateAssignedAccountRoleViaRequest(accAccountDto, validFrom, validTill, waitTillRequestExecuted, aR));
	}

	@Override
	public void removeRoleFromAccountViaRequest(AccAccountDto accAccountDto, boolean waitTillRequestExecuted, UUID... roleIds) {
		IdmRoleRequestDto roleRequest = createRoleRequest(getAccountOwner(accAccountDto.getId()));
		final UUID roleRequestId = roleRequest.getId();
		for (UUID roleId : roleIds) {
			AccAccountRoleAssignmentFilter accountRoleFilter = new AccAccountRoleAssignmentFilter();
			accountRoleFilter.setAccountId(accAccountDto.getId());
			accountRoleFilter.setRoleId(roleId);
			List<AccAccountRoleAssignmentDto> roleAssignments = accAccountRoleAssignmentService.find(accountRoleFilter, null).getContent();
			roleAssignments.forEach(rA ->
				createAccountConceptRoleRequest(roleRequestId, roleId, accAccountDto.getId(), rA.getId(), ConceptRoleRequestOperation.REMOVE)
			);
		}
		IdmRoleRequestDto processedRoleRequest = this.executeRequest(roleRequest, false, true);

		if (waitTillRequestExecuted) {
			this.waitForResult(res -> {
				return roleRequestService.get(processedRoleRequest.getId()).getState() != RoleRequestState.EXECUTED;
			}, 150, 5);
		}
	}

	@Override
	public UUID getAccountOwner(UUID accountId) {
		AccIdentityAccountFilter iaFilter = new AccIdentityAccountFilter();
		iaFilter.setAccountId(accountId);
		return identityAccountService.find(iaFilter, null).stream().findFirst().orElseThrow().getIdentity();
	}

	@Override
	public AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId,
			UUID accountId, UUID roleAssignmentId, ConceptRoleRequestOperation operationType, LocalDate validFrom,
			LocalDate validTill) {
		AccAccountConceptRoleRequestDto concept = new AccAccountConceptRoleRequestDto();
		concept.setAccAccount(accountId);
		concept.setRole(roleId);
		concept.setOperation(operationType);
		concept.setRoleAssignmentUuid(roleAssignmentId);
		concept.setValidFrom(validFrom);
		concept.setValidTill(validTill);
		concept.setRoleRequest(requestId);
		return accountConceptRoleRequestService.save(concept);
	}

	@Override
	public AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId,
			UUID accountId, UUID roleAssignmentId, ConceptRoleRequestOperation operationType) {
		return this.createAccountConceptRoleRequest(requestId, roleId, accountId, roleAssignmentId, operationType, null, null);
	}

	@Override
	public AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId,
			UUID accountId) {
		return this.createAccountConceptRoleRequest(requestId, roleId, accountId, null, ConceptRoleRequestOperation.ADD, null, null);
	}

	@Override
	public IdmRoleRequestDto createRoleRequest(UUID identityId, boolean executeImmediately) {
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(identityId);
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(executeImmediately);
		return roleRequestService.save(roleRequest);
	}

	@Override
	public IdmRoleRequestDto createRoleRequest(UUID identityId) {
		return this.createRoleRequest(identityId, true);
	}

	@Override
	public AccAccountRoleAssignmentDto createAccountRoleAssignment(AccAccountDto accAccountDto, IdmRoleDto roleA) {
		return createAccountRoleAssignment(accAccountDto.getId(), roleA.getId());
	}

	public AccAccountRoleAssignmentDto createAccountRoleAssignment(AccAccountDto accAccountDto, IdmRoleDto role, LocalDate from, LocalDate to) {
		return createAccountRoleAssignment(accAccountDto.getId(), role.getId(), from, to);
	}
}

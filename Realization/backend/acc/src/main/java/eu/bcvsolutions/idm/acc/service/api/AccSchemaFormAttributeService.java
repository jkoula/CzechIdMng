package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.repository.filter.AccSchemaFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for relation between an account and definition of form-attribution. It is an elementary part
 * of the account form "sub-definition".
 * 
 * @author Tomáš Doischer
 *
 */
public interface AccSchemaFormAttributeService extends EventableDtoService<AccSchemaFormAttributeDto, AccSchemaFormAttributeFilter>,
	AuthorizableService<AccSchemaFormAttributeDto>, ScriptEnabled {

	/**
	 * Creates one form attribute based on one schema attribute. If no form definition
	 * for the schema is found, a new one is created.
	 * 
	 * @param schemaAttribute
	 * @return
	 */
	AccSchemaFormAttributeDto createSchemaFormAttribute(SysSchemaAttributeDto schemaAttribute);
	
	/**
	 * Creates form attributes based on schema attributes. If no form definition
	 * for the schema is found, a new one is created.
	 * 
	 * @param objectClass
	 * @return
	 */
	List<AccSchemaFormAttributeDto> createSchemaFormAttributes(SysSchemaObjectClassDto objectClass);
	
	/**
	 * Creates one form attribute based on one schema attribute.
	 * 
	 * @param schemaAttribute
	 * @param schemaFormDefinition
	 * @return
	 */
	AccSchemaFormAttributeDto createSchemaFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition);
	
	/**
	 * Generated a schema form definition code. The code is a 
	 * 
	 * @param system
	 * @param objectClass
	 * @return
	 */
	String createFormDefinitionCode(SysSystemDto system, SysSchemaObjectClassDto objectClass);
	
	/**
	 * Find the form instance of an account.
	 * 
	 * @param account
	 * @return
	 */
	IdmFormInstanceDto getFormInstanceForAccount(AccAccountDto account);
	
	/**
	 * Find the schema form definition.
	 * 
	 * @param system
	 * @param objectClass
	 * @return
	 */
	IdmFormDefinitionDto getSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass);
	
	/**
	 * Find the schema form definition.
	 * 
	 * @param objectClass
	 * @return
	 */
	IdmFormDefinitionDto getSchemaFormDefinition(SysSchemaObjectClassDto objectClass);
	
	/**
	 * Find the schema form definition.
	 * 
	 * @param mapping
	 * @return
	 */
	IdmFormDefinitionDto getSchemaFormDefinition(SysSystemMappingDto mapping);

	boolean isUidAttributeOverriddenForAccount(AccAccountDto account);

	boolean isAttributeOverriddenForAccount(AccAccountDto account, SysSchemaAttributeDto schemaAttribute);

	boolean isAttributeOverriddenForAccount(AccIdentityAccountDto identityAccount, SysSchemaAttributeDto schemaAttribute);
}

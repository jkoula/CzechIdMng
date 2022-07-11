package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.repository.filter.AccSchemaFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;


public interface AccSchemaFormAttributeService extends EventableDtoService<AccSchemaFormAttributeDto, AccSchemaFormAttributeFilter>,
	AuthorizableService<AccSchemaFormAttributeDto>, ScriptEnabled {

	AccSchemaFormAttributeDto createFormAttribute(SysSchemaAttributeDto schemaAttribute);
	
	List<AccSchemaFormAttributeDto> createFormAttributes(SysSchemaObjectClassDto objectClass);
	
	AccSchemaFormAttributeDto createSchemaFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition);
	
	String createFormDefinitionCode(SysSystemDto system, SysSchemaObjectClassDto objectClass);
	
	IdmFormInstanceDto getFormInstanceForAccount(AccAccountDto account);
	
	IdmFormDefinitionDto getSchemaFormDefinition(SysSystemDto system, SysSchemaObjectClassDto objectClass);
	
	IdmFormDefinitionDto getSchemaFormDefinition(SysSchemaObjectClassDto objectClass);
}

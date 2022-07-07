package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.repository.filter.AccSchemaFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;


public interface AccSchemaFormAttributeService extends EventableDtoService<AccSchemaFormAttributeDto, AccSchemaFormAttributeFilter>,
	AuthorizableService<AccSchemaFormAttributeDto>, ScriptEnabled {

	AccSchemaFormAttributeDto createFormAttribute(SysSchemaAttributeDto schemaAttribute);
	
	AccSchemaFormAttributeDto createSchemaFormAttribute(SysSchemaAttributeDto schemaAttribute,
			IdmFormDefinitionDto schemaFormDefinition);
}

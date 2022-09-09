package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SystemEntityTypeRegistrableDto;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

public interface SysSystemEntityTypeManager {

	SystemEntityTypeRegistrable getSystemEntityByCode(String code);
	
	SystemEntityTypeRegistrable getSystemEntityByClass(Class<? extends AbstractDto> clazz);
	
	List<SystemEntityTypeRegistrableDto> getSupportedEntityTypes();
	
	SystemEntityTypeRegistrableDto getSystemEntityDtoByCode(String code);

	SystemEntityTypeRegistrableDto getSystemEntityDtoByCode(String code, String mappingId);
}

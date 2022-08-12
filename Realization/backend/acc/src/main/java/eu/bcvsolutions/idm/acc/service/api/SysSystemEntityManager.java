package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

public interface SysSystemEntityManager {

	SystemEntityTypeRegistrable getSystemEntityByCode(String code);
	
	SystemEntityTypeRegistrable getSystemEntityByClass(Class<? extends AbstractDto> clazz);
}

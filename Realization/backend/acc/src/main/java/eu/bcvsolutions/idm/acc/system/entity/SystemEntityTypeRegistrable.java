package eu.bcvsolutions.idm.acc.system.entity;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

public interface SystemEntityTypeRegistrable {

	String getSystemEntityCode();
	
	Class<? extends AbstractDto> getEntityType();
	
	Class<? extends AbstractDto> getExtendedAttributeOwnerType();
	
	boolean isSupportsProvisioning();
	
	boolean isSupportsSync();
}

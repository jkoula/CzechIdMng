package eu.bcvsolutions.idm.acc.system.entity;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

public interface SystemEntityTypeRegistrable {

	String getSystemEntityCode();
	
	String getModule();
	
	Class<? extends AbstractDto> getEntityType();
	
	Class<? extends AbstractDto> getExtendedAttributeOwnerType();
	
	boolean isSupportsProvisioning();
	
	boolean isSupportsSync();
	
	List<String> getSupportedAttributes();

	default List<String> getAdditionalAttributes(String mappingId) {
		return new ArrayList<>();
	}
}

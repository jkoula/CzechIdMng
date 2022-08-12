package eu.bcvsolutions.idm.acc.system.entity;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

@Component
public class IdentityRoleSystemEntityType implements SystemEntityTypeRegistrable {

	public static final String SYSTEM_ENTITY_TYPE = "IDENTITY_ROLE";
	
	@Override
	public String getSystemEntityCode() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public Class<? extends AbstractDto> getEntityType() {
		return IdmIdentityRoleDto.class;
	}

	@Override
	public Class<? extends AbstractDto> getExtendedAttributeOwnerType() {
		return IdmIdentityRoleDto.class;
	}

	@Override
	public boolean isSupportsProvisioning() {
		return false;
	}

	@Override
	public boolean isSupportsSync() {
		return true;
	}

}

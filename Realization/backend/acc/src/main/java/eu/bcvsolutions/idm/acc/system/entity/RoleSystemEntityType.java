package eu.bcvsolutions.idm.acc.system.entity;

import java.util.List;

import org.springframework.stereotype.Component;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

@Component
public class RoleSystemEntityType implements SystemEntityTypeRegistrable {

	public static final String SYSTEM_ENTITY_TYPE = "ROLE";
	
	@Override
	public String getSystemEntityCode() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public Class<? extends AbstractDto> getEntityType() {
		return IdmRoleDto.class;
	}

	@Override
	public Class<? extends AbstractDto> getExtendedAttributeOwnerType() {
		return IdmRoleDto.class;
	}

	@Override
	public boolean isSupportsProvisioning() {
		return true;
	}

	@Override
	public boolean isSupportsSync() {
		return true;
	}

	@Override
	public String getModule() {
		return AccModuleDescriptor.MODULE_ID;
	}

	@Override
	public List<String> getSupportedAttributes() {
		return Lists.newArrayList(
				"NAME",
				"BASE_CODE",
				"ENVIRONMENT",
				"ROLE_TYPE",
				"PRIORITY",
				"APPROVE_REMOVE",
				"DESCRIPTION",
				"DISABLED",
				"ROLE_MEMBERSHIP_ID",
				"ROLE_FORWARD_ACM",
				"ROLE_SKIP_VALUE_IF_EXCLUDED",
				"ROLE_MEMBERS_FIELD",
				"ROLE_CATALOGUE_FIELD");
	}
}

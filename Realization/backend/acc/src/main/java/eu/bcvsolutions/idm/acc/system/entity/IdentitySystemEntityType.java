package eu.bcvsolutions.idm.acc.system.entity;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

@Component
public class IdentitySystemEntityType implements SystemEntityTypeRegistrable {

	public static final String SYSTEM_ENTITY_TYPE = "IDENTITY";
	
	@Override
	public String getSystemEntityCode() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public Class<? extends AbstractDto> getEntityType() {
		return IdmIdentityDto.class;
	}

	@Override
	public Class<? extends AbstractDto> getExtendedAttributeOwnerType() {
		return IdmIdentityDto.class;
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
				"username",
				"externalCode",
				"disabled",
				"firstName",
				"lastName",
				"email",
				"phone",
				"titleBefore",
				"titleAfter",
				"description",
				"state",
				"assignedRoles",
				"assignedRolesForSystem",
				"formProjection",
				"password");
	}
}

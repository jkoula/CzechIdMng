package eu.bcvsolutions.idm.acc.system.entity;

import java.util.List;

import org.springframework.stereotype.Component;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

@Component
public class ContractSystemEntityType implements SystemEntityTypeRegistrable {

	public static final String SYSTEM_ENTITY_TYPE = "CONTRACT";
	
	@Override
	public String getSystemEntityCode() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public Class<? extends AbstractDto> getEntityType() {
		return IdmIdentityContractDto.class;
	}

	@Override
	public Class<? extends AbstractDto> getExtendedAttributeOwnerType() {
		return IdmIdentityContractDto.class;
	}

	@Override
	public boolean isSupportsProvisioning() {
		return false;
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
				"identity",
				"main",
				"state",
				"position",
				"workPosition",
				"validFrom",
				"validTill",
				"externe",
				"description",
				"guarantees",
				"positions");
	}
}

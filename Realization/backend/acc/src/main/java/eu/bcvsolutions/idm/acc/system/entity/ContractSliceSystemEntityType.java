package eu.bcvsolutions.idm.acc.system.entity;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

@Component
public class ContractSliceSystemEntityType implements SystemEntityTypeRegistrable {

	public static final String SYSTEM_ENTITY_TYPE = "CONTRACT_SLICE";
	
	@Override
	public String getSystemEntityCode() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public Class<? extends AbstractDto> getEntityType() {
		return IdmContractSliceDto.class;
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
}

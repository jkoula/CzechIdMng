package eu.bcvsolutions.idm.acc.system.entity;

import java.util.List;

import org.springframework.hateoas.core.Relation;
import org.springframework.stereotype.Component;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

@Component
@Relation(collectionRelation = "entityTypes")
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

	@Override
	public String getModule() {
		return AccModuleDescriptor.MODULE_ID;
	}

	@Override
	public List<String> getSupportedAttributes() {
		return Lists.newArrayList(
				"CONTRACT_CODE",
				"IDENTITY",
				"VALID_FROM",
				"CONTRACT_VALID_FROM",
				"CONTRACT_VALID_TILL",
				"MAIN",
				"STATE",
				"POSITION",
				"WORK_POSITION",
				"EXTERNE",
				"GUARANTEES",
				"DESCRIPTION");
	}
}

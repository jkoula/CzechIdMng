package eu.bcvsolutions.idm.core.api.dto;

import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * IdentityRole DTO.
 *
 * @author svanda
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "identityRoles")
public class IdmIdentityRoleDto extends AbstractRoleAssignmentDto {
	
	private static final long serialVersionUID = 1L;
	//
	public static final String PROPERTY_IDENTITY_CONTRACT = "identityContract";
	public static final String PROPERTY_ROLE = "role";
	public static final String PROPERTY_ROLE_TREE_NODE = "roleTreeNode";
	@Embedded(dtoClass = IdmIdentityContractDto.class)
    private UUID identityContract;

	@Embedded(dtoClass = IdmContractPositionDto.class)
	protected UUID contractPosition;

	@Embedded(dtoClass = IdmIdentityRoleDto.class)
	protected UUID directRole; // direct identity role

	@Deprecated
	@SuppressWarnings("unused")
    private boolean automaticRole; // this attribute can't be removed (serializable backward compatibility)

	public IdmIdentityRoleDto() {
		super();
	}

    public IdmIdentityRoleDto(UUID id) {
        super(id);
    }

	public UUID getIdentityContract() {
        return identityContract;
    }

    public void setIdentityContract(UUID identityContract) {
        this.identityContract = identityContract;
    }
    
    @JsonIgnore
    public void setIdentityContractDto(IdmIdentityContractDto identityContract) {
    	Assert.notNull(identityContract, "Contract is requred to se into assigned role.");
    	//
        this.identityContract = identityContract.getId();
        this.getEmbedded().put(PROPERTY_IDENTITY_CONTRACT, identityContract);
    }


	/**
	 * DTO are serialized in WF and embedded objects.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param ois
	 * @throws Exception
	 */
	protected void readObject(ObjectInputStream ois) throws Exception {
		super.readObject(ois);
		GetField readFields = ois.readFields();
		//
	    identityContract = (UUID) readFields.get("identityContract", null);
	    contractPosition = (UUID) readFields.get("contractPosition", null);
		directRole = (UUID) readFields.get("directRole", null);
    }

	@Override
	public UUID getEntity() {
		return getIdentityContract();
	}

	public UUID getContractPosition() {
		return contractPosition;
	}

	public void setContractPosition(UUID contractPosition) {
		this.contractPosition = contractPosition;
	}

	public UUID getDirectRole() {
		return directRole;
	}

	public void setDirectRole(UUID directRole) {
		this.directRole = directRole;
	}
}

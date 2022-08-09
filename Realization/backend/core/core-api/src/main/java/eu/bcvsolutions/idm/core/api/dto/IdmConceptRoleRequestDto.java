package eu.bcvsolutions.idm.core.api.dto;

import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Dto for concept role request
 *
 * @author svandav
 */
@Relation(collectionRelation = "conceptRoleRequests")
public class IdmConceptRoleRequestDto extends AbstractConceptRoleRequestDto {

    private static final long serialVersionUID = 1L;

    @Embedded(dtoClass = IdmIdentityContractDto.class)
    private UUID identityContract;
    private UUID contractPosition;
    @Embedded(dtoClass = IdmIdentityRoleDto.class)
    private UUID identityRole; // For update and delete operations
    // this attribute can't be renamed (backward compatibility) - AutomaticRole reference

    public UUID getIdentityContract() {
        return identityContract;
    }

    public void setIdentityContract(UUID identityContract) {
        this.identityContract = identityContract;
    }

    public UUID getIdentityRole() {
        return identityRole;
    }

    public void setIdentityRole(UUID identityRole) {
        this.identityRole = identityRole;
    }


    /**
	 * Relation only without embedded.
	 * 
	 * @return
	 * @since 9.6.0
	 */
	public UUID getContractPosition() {
		return contractPosition;
	}
	
	/**
	 * Relation only without embedded.
	 * 
	 * @param contractPosition
	 * @since 9.6.0
	 */
	public void setContractPosition(UUID contractPosition) {
		this.contractPosition = contractPosition;
	}


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((identityContract == null) ? 0 : identityContract.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IdmConceptRoleRequestDto)) {
            return false;
        }
        IdmConceptRoleRequestDto other = (IdmConceptRoleRequestDto) obj;
        if (identityContract == null) {
            if (other.identityContract != null) {
                return false;
            }
        } else if (!identityContract.equals(other.identityContract)) {
            return false;
        }
        if (identityRole == null) {
            if (other.identityRole != null) {
                return false;
            }
        } else if (!identityRole.equals(other.identityRole)) {
            return false;
        }
        return super.equals(obj);
    }
    
    /**
	 * DTO are serialized in WF and embedded objects.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param ois
	 * @throws Exception
	 */
	private void readObject(ObjectInputStream ois) throws Exception {

		GetField readFields = ois.readFields();
		//
	    identityContract = (UUID) readFields.get("identityContract", null);
	    contractPosition = (UUID) readFields.get("contractPosition", null);
	    identityRole = (UUID) readFields.get("identityRole", null);
    }

}

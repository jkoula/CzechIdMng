package eu.bcvsolutions.idm.core.api.dto;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import org.springframework.hateoas.core.Relation;

/**
 * DTO for show changes on assigned identity roles
 *
 * @author Vít Švanda
 */
@Relation(collectionRelation = "requestIdentityRoles")
public class IdmRequestIdentityRoleDto extends AbstractConceptRoleRequestDto {

	private static final long serialVersionUID = 2552559375838158151L;
	/**
	 * Roles for create adding concepts (use only for assign new roles). You can
	 * still use field "role" for assign one role.
	 */
	private Set<UUID> roles;
	private Set<IdmIdentityDto> candidates;

	private UUID ownerUuid;

	private Class<? extends Identifiable> ownerType = IdmIdentityContractDto.class;
	private UUID roleAssignmentUuid;

	private Class<? extends AbstractConceptRoleRequestDto> assignmentType = IdmConceptRoleRequestDto.class;


	public Set<UUID> getRoles() {
		return roles;
	}

	public void setRoles(Set<UUID> roles) {
		this.roles = roles;
	}


	public Set<IdmIdentityDto> getCandidates() {
		return candidates;
	}

	public void setCandidates(Set<IdmIdentityDto> candidates) {
		this.candidates = candidates;
	}

	@Override
	public UUID getRoleAssignmentUuid() {
		return this.roleAssignmentUuid;
	}

	@Override
	public void setRoleAssignmentUuid(UUID id) {
		this.roleAssignmentUuid = id;
	}

	@Override
	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	@Override
	public void setOwnerUuid(UUID id) {
		this.ownerUuid = id;
	}

	@Override
	public AbstractConceptRoleRequestDto copy() {
		IdmRequestIdentityRoleDto conceptRoleRequest = new IdmRequestIdentityRoleDto();
		conceptRoleRequest.setRoleRequest(getRoleRequest());
		conceptRoleRequest.setOperation(getOperation());
		// from concept
		conceptRoleRequest.setValidFrom(getValidFrom());
		conceptRoleRequest.setValidTill(getValidTill());
		conceptRoleRequest.setOwnerUuid(getOwnerUuid());
		// from assigned (~changed) sub role
		conceptRoleRequest.setDirectConcept(getId());
		conceptRoleRequest.setRoles(getRoles());
		conceptRoleRequest.setCandidates(getCandidates());
		conceptRoleRequest.setRoleAssignmentUuid(getRoleAssignmentUuid());
		conceptRoleRequest.setDirectConcept(getId());
		// save and add to concepts to be processed
		return conceptRoleRequest;
	}

	@Override
	public Class<? extends Identifiable> getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(Class<? extends Identifiable> ownerType) {
		this.ownerType = ownerType;
	}

	public Class<? extends AbstractConceptRoleRequestDto> getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(Class<? extends AbstractConceptRoleRequestDto> assignmentType) {
		this.assignmentType = assignmentType;
	}
}
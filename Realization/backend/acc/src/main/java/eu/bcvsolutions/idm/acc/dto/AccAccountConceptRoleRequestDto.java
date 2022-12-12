package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation(collectionRelation = "accAccountConceptRoleRequests")
public class AccAccountConceptRoleRequestDto extends AbstractConceptRoleRequestDto {

    private static final long serialVersionUID = 1L;

    @Embedded(dtoClass = AccAccountDto.class)
    private UUID account;

    @Embedded(dtoClass = AccAccountRoleAssignmentDto.class)
    private UUID accountRole;

    public UUID getAccount() {
        return account;
    }

    public void setAccount(UUID account) {
        this.account = account;
    }

    public UUID getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(UUID accountRole) {
        this.accountRole = accountRole;
    }

    @Override
    public UUID getRoleAssignmentUuid() {
        return getAccountRole();
    }

    @Override
    public void setRoleAssignmentUuid(UUID id) {
        setAccountRole(id);
    }

    @Override
    public UUID getOwnerUuid() {
        return getAccount();
    }

    @Override
    public void setOwnerUuid(UUID id) {
        setAccount(id);
    }

    @Override
    public AccAccountConceptRoleRequestDto copy() {
        AccAccountConceptRoleRequestDto conceptRoleRequest = new AccAccountConceptRoleRequestDto();
        conceptRoleRequest.setRoleRequest(getRoleRequest());
        conceptRoleRequest.setOperation(getOperation());
        // from concept
        conceptRoleRequest.setValidFrom(getValidFrom());
        conceptRoleRequest.setValidTill(getValidTill());
        conceptRoleRequest.setAccount(getAccount());
        // from assigned (~changed) sub role
        conceptRoleRequest.setDirectConcept(getId());
        // save and add to concepts to be processed
        return conceptRoleRequest;
    }

    @Override
    public Class<? extends Identifiable> getOwnerType() {
        return AccAccountDto.class;
    }
}

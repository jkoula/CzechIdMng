package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import org.springframework.hateoas.core.Relation;

import java.io.ObjectInputStream;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation(collectionRelation = "accountRoleAssignments")
public class AccAccountRoleAssignmentDto extends AbstractRoleAssignmentDto {

    private static final long serialVersionUID = 1L;
    @Embedded(dtoClass = AccAccountDto.class)
    private UUID account;

    @Embedded(dtoClass = AccAccountRoleAssignmentDto.class)
    protected UUID directRole; // direct identity role

    public AccAccountRoleAssignmentDto(UUID identityRoleId) {
        super(identityRoleId);
    }

    public AccAccountRoleAssignmentDto() {
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
        ObjectInputStream.GetField readFields = ois.readFields();
        //
        account = (UUID) readFields.get("accAccount", null);
        directRole = (UUID) readFields.get("directRole", null);
    }

    public UUID getAccount() {
        return account;
    }

    public void setAccount(UUID account) {
        this.account = account;
    }

    @Override
    public UUID getEntity() {
        return getAccount();
    }

    public UUID getDirectRole() {
        return directRole;
    }

    public void setDirectRole(UUID directRole) {
        this.directRole = directRole;
    }
}

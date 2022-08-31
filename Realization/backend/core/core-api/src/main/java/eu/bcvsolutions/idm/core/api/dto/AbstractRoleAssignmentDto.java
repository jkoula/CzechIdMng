package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentDto extends FormableDto implements ValidableEntity, ExternalIdentifiable {
    //
    @Size(max = DefaultFieldLengths.NAME)
    @ApiModelProperty(notes = "Unique external identifier.")
    protected String externalId;
    @Embedded(dtoClass = IdmRoleDto.class)
    protected UUID role;
    protected LocalDate validFrom;
    protected LocalDate validTill;
    @Embedded(dtoClass = AbstractIdmAutomaticRoleDto.class)
    protected UUID roleTreeNode; // this attribute can't be renamed (backward compatibility) - AutomaticRole reference
    @Embedded(dtoClass = IdmIdentityRoleDto.class)
    protected UUID directRole; // direct identity role
    @Embedded(dtoClass = IdmRoleCompositionDto.class)
    protected UUID roleComposition; // direct role
    // Relation on sys-system form ACC. We need to working with that also in the core module (cross-domains).
    protected UUID roleSystem;

    public AbstractRoleAssignmentDto(UUID id) {
        super(id);
    }

    public AbstractRoleAssignmentDto() {

    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTo) {
        this.validTill = validTo;
    }

    public UUID getRole() {
        return role;
    }

    public void setRole(UUID role) {
        this.role = role;
    }

    public UUID getAutomaticRole() {
		return roleTreeNode;
	}

    public void setAutomaticRole(UUID automaticRole) {
		this.roleTreeNode = automaticRole;
	}

    @Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

    @Override
	public String getExternalId() {
		return externalId;
	}

    public UUID getDirectRole() {
		return directRole;
	}

    public void setDirectRole(UUID directRole) {
		this.directRole = directRole;
	}

    public UUID getRoleComposition() {
		return roleComposition;
	}

    public void setRoleComposition(UUID roleComposition) {
		this.roleComposition = roleComposition;
	}

    public UUID getRoleSystem() {
		return roleSystem;
	}

    public void setRoleSystem(UUID roleSystem) {
		this.roleSystem = roleSystem;
	}

    protected void readObject(ObjectInputStream ois) throws Exception {
        ObjectInputStream.GetField readFields = ois.readFields();
        //
        externalId = (String) readFields.get("externalId", null);
        role = (UUID) readFields.get("role", null);
        validFrom = DtoUtils.toLocalDate(readFields.get("validFrom", null));
        validTill = DtoUtils.toLocalDate(readFields.get("validTill", null));
        roleTreeNode = (UUID) readFields.get("roleTreeNode", null);
        directRole = (UUID) readFields.get("directRole", null);
        roleComposition = (UUID) readFields.get("roleComposition", null);
        roleSystem = (UUID) readFields.get("roleSystem", null);
    }

    public abstract UUID getEntity();
}

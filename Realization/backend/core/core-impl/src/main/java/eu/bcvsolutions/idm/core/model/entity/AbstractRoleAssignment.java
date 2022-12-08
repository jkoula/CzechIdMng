package eu.bcvsolutions.idm.core.model.entity;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractRoleAssignment extends AbstractEntity implements FormableEntity, ValidableEntity, Identifiable {

    @NotNull
    @Audited
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    protected IdmRole role;

    @Audited
    @ManyToOne
    @JoinColumn(name = "automatic_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    protected IdmAutomaticRole automaticRole; // Assigned role depends on automatic role

    @Audited
    @Column(name = "valid_from")
    protected LocalDate validFrom;

    @Audited
    @Column(name = "valid_till")
    protected LocalDate validTill;

    @Audited
    @ManyToOne
    @JoinColumn(name = "role_composition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    protected IdmRoleComposition roleComposition;

    // Relation on sys-system form ACC. We need to working with that also in the core module (cross-domains).
    @Audited
    @Column(name = "role_system_id", length = 16)
    protected UUID roleSystem;

    public AbstractRoleAssignment(UUID uuid) {
        super(uuid);
    }

    public AbstractRoleAssignment(ValidableEntity validable) {
        Assert.notNull(validable, "Validable target is required, when has to be set into assigned role.");
        //
        this.validFrom = validable.getValidFrom();
        this.validTill = validable.getValidTill();
    }

    public AbstractRoleAssignment() {

    }

    public UUID getRoleSystem() {
        return roleSystem;
    }

    public void setRoleSystem(UUID roleSystem) {
        this.roleSystem = roleSystem;
    }

    public IdmRoleComposition getRoleComposition() {
        return roleComposition;
    }

    public void setRoleComposition(IdmRoleComposition roleComposition) {
        this.roleComposition = roleComposition;
    }

    public IdmAutomaticRole getAutomaticRole() {
        return automaticRole;
    }

    public void setAutomaticRole(IdmAutomaticRole automaticRole) {
        this.automaticRole = automaticRole;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTo) {
        this.validTill = validTo;
    }

    public IdmRole getRole() {
        return role;
    }

    public void setRole(IdmRole role) {
        this.role = role;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }
}

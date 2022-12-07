package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import org.hibernate.envers.Audited;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Role assignment extended attributes for {@link AccAccount}
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@Entity
@Table(name = "acc_role_assignment_form_value", indexes = {
		@Index(name = "idx_acc_role_ass_form_a", columnList = "owner_id"),
		@Index(name = "idx_acc_role_ass_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_acc_role_ass_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_acc_role_ass_form_uuid", columnList = "uuid_value") })
public class AccAccountRoleAssignmentFormValue extends AbstractFormValue<AccAccountRoleAssignment> {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccountRoleAssignment owner;

	public AccAccountRoleAssignmentFormValue() {
	}

	public AccAccountRoleAssignmentFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}

	@Override
	public AccAccountRoleAssignment getOwner() {
		return owner;
	}

	public void setOwner(AccAccountRoleAssignment owner) {
		this.owner = owner;
	}

}

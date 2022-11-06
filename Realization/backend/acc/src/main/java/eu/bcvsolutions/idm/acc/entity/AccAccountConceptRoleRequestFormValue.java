package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import org.hibernate.envers.Audited;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Role request concept extended attributes for {@link AccAccount}
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@Entity
@Table(name = "idm_concept_role_form_value", indexes = {
		@Index(name = "idx_acc_concept_rol_form_a", columnList = "owner_id"),
		@Index(name = "idx_acc_concept_rol_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_acc_concept_rol_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_acc_concept_rol_form_uuid", columnList = "uuid_value") })
public class AccAccountConceptRoleRequestFormValue extends AbstractFormValue<AccAccountConceptRoleRequest> {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccountConceptRoleRequest owner;

	public AccAccountConceptRoleRequestFormValue() {
	}

	public AccAccountConceptRoleRequestFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}

	@Override
	public AccAccountConceptRoleRequest getOwner() {
		return owner;
	}

	public void setOwner(AccAccountConceptRoleRequest owner) {
		this.owner = owner;
	}

}

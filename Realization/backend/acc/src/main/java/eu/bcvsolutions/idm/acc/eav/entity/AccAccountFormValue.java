package eu.bcvsolutions.idm.acc.eav.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * EAV for {@link AccAccount}.
 * 
 * @author Tomáš Doischer
 *
 */
@Entity
@Table(name = "prp_property_form_value", indexes = {
		@Index(name = "idx_acc_account_form_a", columnList = "owner_id"),
		@Index(name = "idx_acc_account_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_acc_account_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_acc_account_form_uuid", columnList = "uuid_value") })
public class AccAccountFormValue extends AbstractFormValue<AccAccount> implements AuditSearchable {

	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none")
	private AccAccount owner;
	
	public AccAccountFormValue() {
	}
	
	public AccAccountFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public AccAccount getOwner() {
		return owner;
	}
	
	public void setOwner(AccAccount owner) {
		this.owner = owner;
	}
}

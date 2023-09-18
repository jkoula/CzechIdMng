package eu.bcvsolutions.idm.doc.entity.eav;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.doc.entity.DocDocument;

/**
 * Document extended attributes.
 *
 * @author Jirka Koula
 *
 */
@Entity
@Table(name = "doc_document_form_value", indexes = {
		@Index(name = "idx_sys_sys_form_a", columnList = "owner_id"),
		@Index(name = "idx_sys_sys_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_sys_sys_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_sys_sys_form_uuid", columnList = "uuid_value") })
public class DocDocumentFormValue extends AbstractFormValue<DocDocument> {

	private static final long serialVersionUID = -6873566385389649927L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private DocDocument owner;

	public DocDocumentFormValue() {
	}

	public DocDocumentFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}

	@Override
	public DocDocument getOwner() {
		return owner;
	}

	@Override
	public void setOwner(DocDocument owner) {
		this.owner = owner;
	}

}

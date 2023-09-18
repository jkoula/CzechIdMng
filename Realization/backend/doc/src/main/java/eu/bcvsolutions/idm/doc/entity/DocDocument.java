package eu.bcvsolutions.idm.doc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;

/**
 * Identity document.
 *
 * @author Jirka Koula
 */
@Entity
@Table(name = "doc_document", indexes = {
		@Index(name = "idx_doc_document_number", columnList = "number")
})
public class DocDocument extends AbstractEntity implements FormableEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "uuid", length = DefaultFieldLengths.UID, nullable = false)
	private String uuid;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = DefaultFieldLengths.ENUMARATION)
	private DocDocumentType type;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "number", length = DefaultFieldLengths.UID, nullable = false)
	private String number;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "first_name", length = DefaultFieldLengths.NAME)
	private String firstName;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "last_name", length = DefaultFieldLengths.NAME)
	private String lastName;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = DefaultFieldLengths.ENUMARATION)
	private DocDocumentState state;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentity identity;

	public String getUuid() { return uuid; }

	public void setUuid(String uuid) { this.uuid = uuid; }

	public DocDocumentType getType() { return type; }

	public void setType(DocDocumentType type) { this.type = type; }

	public String getNumber() { return number; }

	public void setNumber(String number) { this.number = number; }

	public String getFirstName() { return firstName; }

	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }

	public void setLastName(String lastName) { this.lastName = lastName; }

	public DocDocumentState getState() { return state; }

	public void setState(DocDocumentState state) { this.state = state; }

	public IdmIdentity getIdentity() { return identity; }

	public void setIdentity(IdmIdentity identity) { this.identity = identity; }

}

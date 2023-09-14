package eu.bcvsolutions.idm.doc.dto;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.domain.DocumentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Document
 * 
 * @author Jirka Koula
 *
 */
@Relation(collectionRelation = "documents")
@ApiModel(description = "Document")
public class DocumentDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Size(min = 1, max = DefaultFieldLengths.UID)
	@ApiModelProperty(required = true, notes = "Unique identifier for the document. Could be used as identifier in rest endpoints.")
	private String uuid;

	@ApiModelProperty(required = true, notes = "Type of document (e.g. passport, ID card).")
	private DocumentType type;

	@Size(min = 1, max = DefaultFieldLengths.UID)
	@ApiModelProperty(required = true, notes = "Document number.")
	private String number;

	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = false, notes = "First name associated with the document.")
	private String firstName;

	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Last name associated with the document.")
	private String lastName;

	@ApiModelProperty(required = true, notes = "Current state of the document (VALID or INVALID).")
	private DocumentState state;

	@Embedded(dtoClass = IdmIdentityDto.class)
	@ApiModelProperty(required = true, notes = "Identity the document relates to.")
	private UUID identity;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public DocumentType getType() {
		return type;
	}

	public void setType(DocumentType type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public DocumentState getState() {
		return state;
	}

	public void setState(DocumentState state) {
		this.state = state;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}
}

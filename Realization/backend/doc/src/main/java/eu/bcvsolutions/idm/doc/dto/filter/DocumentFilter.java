package eu.bcvsolutions.idm.doc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.domain.DocumentType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;

/**
 * Filter for documents
 * 
 * @author Jirka Koula
 *
 */
public class DocumentFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_IDENTITY_ID = "identity";
	public static final String PARAMETER_TYPE = "type";
	public static final String PARAMETER_STATE = "state";

	public DocumentFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public DocumentFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}

	public DocumentFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(DocumentDto.class, data, parameterConverter);
	}

	public UUID getIdentityId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_ID);
	}

	public void setIdentityId(UUID identityId) {
		set(PARAMETER_IDENTITY_ID, identityId);
	}

	public DocumentType getType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_TYPE, DocumentType.class);
	}

	public void setType(DocumentType type) {
		set(PARAMETER_TYPE, type);
	}

	public DocumentState getState() { return getParameterConverter().toEnum(getData(), PARAMETER_STATE, DocumentState.class); }

	public void setState(DocumentState state) { set(PARAMETER_STATE, state); }

}

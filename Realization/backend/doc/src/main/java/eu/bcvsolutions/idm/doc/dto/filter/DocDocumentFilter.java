package eu.bcvsolutions.idm.doc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;

/**
 * Filter for documents
 * 
 * @author Jirka Koula
 *
 */
public class DocDocumentFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_IDENTITY_ID = "identity";
	public static final String PARAMETER_TYPE = "type";
	public static final String PARAMETER_STATE = "state";

	public DocDocumentFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public DocDocumentFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}

	public DocDocumentFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(DocDocumentDto.class, data, parameterConverter);
	}

	public UUID getIdentityId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_ID);
	}

	public void setIdentityId(UUID identityId) {
		set(PARAMETER_IDENTITY_ID, identityId);
	}

	public DocDocumentType getType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_TYPE, DocDocumentType.class);
	}

	public void setType(DocDocumentType type) {
		set(PARAMETER_TYPE, type);
	}

	public DocDocumentState getState() { return getParameterConverter().toEnum(getData(), PARAMETER_STATE, DocDocumentState.class); }

	public void setState(DocDocumentState state) { set(PARAMETER_STATE, state); }

}

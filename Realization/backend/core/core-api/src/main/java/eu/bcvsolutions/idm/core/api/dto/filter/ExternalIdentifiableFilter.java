package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;

/**
 * Filter for objects that own external identifier.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface ExternalIdentifiableFilter extends ExternalIdentifiable, BaseDataFilter {

	/**
	 * Get external identifier.
	 *
	 * @return
	 */
	@Override
	default public String getExternalId() {
		return getParameterConverter().toString(getData(), PROPERTY_EXTERNAL_ID);
	}
	
	/**
	 * Set external identifier.
	 * 
	 * @param externalId
	 */
	@Override
	default public void setExternalId(String externalId) {
		set(PROPERTY_EXTERNAL_ID, externalId);
	}

	/**
	 * This method is useful in those cases, where user wants to distinguish between cases, when externalId is null and
	 * when it has not been set at all. Typical usecase is enabling search of {@link ExternalIdentifiable} objects with null
	 * externalId.
	 *
	 * @return true, if property externalId has been set to this filter.
	 */
	default public boolean containsExternalId() {
		return getData().containsKey(PROPERTY_EXTERNAL_ID);
	}
}

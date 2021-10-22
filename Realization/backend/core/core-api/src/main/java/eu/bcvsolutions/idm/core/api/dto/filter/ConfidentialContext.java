package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Context (~filter) for load confidential metadata.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
public interface ConfidentialContext extends BaseDataFilter {

	/**
	 * Fill secret proxy string, if confidential value is filled. 
	 */
	String PARAMETER_ADD_SECRED_PROXY_STRING = "addSecredProxyString";
	
	/**
	 * Fill secret proxy string, if confidential value is filled. 
	 * 
	 * @return true - secret proxy string will be set, if confidential value is filled
	 */
    default boolean isAddSecredProxyString() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_SECRED_PROXY_STRING, true);
    }

    /**
     * Fill secret proxy string, if confidential value is filled.
     * 
     * @param value true - secret proxy string will be set, if confidential value is filled
     */
    default void setAddSecredProxyString(boolean addSecredProxyString) {
    	set(PARAMETER_ADD_SECRED_PROXY_STRING, addSecredProxyString);
    }
}
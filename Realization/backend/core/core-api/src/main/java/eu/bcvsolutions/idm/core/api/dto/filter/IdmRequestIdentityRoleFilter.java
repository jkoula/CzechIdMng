package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.MultiValueMap;

/**
 * Filter for changed assigned identity roles
 *
 * @author Vít Švanda
 */
public class IdmRequestIdentityRoleFilter extends IdmConceptRoleRequestFilter implements FormableFilter{
	private Boolean includeEav = false;
    private Boolean onlyChanges = false;
    private Boolean includeCandidates = false;
    private Boolean includeCrossDomainsSystemsCount = false;

	public static final String ONLY_ASSIGNMENTS_PARAMETER = "onlyAssignments";
	public static final String VALID_PARAMETER = "isValid";
	public static final String ROLE_SYSTEM_ID = "roleSystemId";

    public IdmRequestIdentityRoleFilter() {
		super();
	}
    
    public IdmRequestIdentityRoleFilter(MultiValueMap<String, Object> data) {
    	super(data);
    }

	public Boolean isOnlyAssignments() {
		return getParameterConverter().toBoolean(getData(), ONLY_ASSIGNMENTS_PARAMETER, false);
	}

	public void setOnlyAssignments(Boolean onlyAssignments) {
		set(ONLY_ASSIGNMENTS_PARAMETER, onlyAssignments);
	}

	public Boolean isIncludeEav() {
		return includeEav;
	}

	public void setIncludeEav(Boolean includeEav) {
		this.includeEav = includeEav;
	}

	public Boolean isOnlyChanges() {
		return onlyChanges;
	}

	public void setOnlyChanges(Boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	public Boolean isIncludeCandidates() {
		return includeCandidates;
	}

	public void setIncludeCandidates(Boolean includeCandidates) {
		this.includeCandidates = includeCandidates;
	}

	public Boolean isIncludeCrossDomainsSystemsCount() {
		return includeCrossDomainsSystemsCount;
	}

	public void setIncludeCrossDomainsSystemsCount(Boolean includeCrossDomainsSystemsCount) {
		this.includeCrossDomainsSystemsCount = includeCrossDomainsSystemsCount;
	}

	public void setRoleSystemId(UUID id) {
		set(ROLE_SYSTEM_ID, id);
	}

	public UUID getRoleSystemId() {
		return getParameterConverter().toUuid(getData(), ROLE_SYSTEM_ID);
	}

	public void setValid(Boolean val) {
		set(VALID_PARAMETER, val);
	}

	public Boolean isValid() {
		return getParameterConverter().toBoolean(getData(), VALID_PARAMETER);
	}

}

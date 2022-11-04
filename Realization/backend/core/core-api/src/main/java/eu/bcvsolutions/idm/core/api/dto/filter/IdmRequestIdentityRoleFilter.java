package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.MultiValueMap;

/**
 * Filter for changed assigned identity roles
 *
 * @author Vít Švanda
 */
public class IdmRequestIdentityRoleFilter extends IdmConceptRoleRequestFilter {
	private boolean includeEav = false;
    private boolean onlyChanges = false;
    private boolean includeCandidates = false;
    private boolean includeCrossDomainsSystemsCount = false;

	public static final String ONLY_ASSIGNMENTS_PARAMETER = "onlyAssignments";
	public static final String ROLE_SYSTEM_ID = "roleSystemId";
    public IdmRequestIdentityRoleFilter() {
		super();
	}
    
    public IdmRequestIdentityRoleFilter(MultiValueMap<String, Object> data) {
    	super(data);
    }

	public boolean isOnlyAssignments() {
		return getParameterConverter().toBoolean(getData(), ONLY_ASSIGNMENTS_PARAMETER, false);
	}

	public void setOnlyAssignments(boolean onlyAssignments) {
		set(ONLY_ASSIGNMENTS_PARAMETER, onlyAssignments);
	}

	public boolean isIncludeEav() {
		return includeEav;
	}

	public void setIncludeEav(boolean includeEav) {
		this.includeEav = includeEav;
	}

	public boolean isOnlyChanges() {
		return onlyChanges;
	}

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	public boolean isIncludeCandidates() {
		return includeCandidates;
	}

	public void setIncludeCandidates(boolean includeCandidates) {
		this.includeCandidates = includeCandidates;
	}

	public boolean isIncludeCrossDomainsSystemsCount() {
		return includeCrossDomainsSystemsCount;
	}

	public void setIncludeCrossDomainsSystemsCount(boolean includeCrossDomainsSystemsCount) {
		this.includeCrossDomainsSystemsCount = includeCrossDomainsSystemsCount;
	}

	public void setRoleSystemId(UUID id) {
		set(ROLE_SYSTEM_ID, id);
	}

	public UUID getRoleSystemId() {
		return getParameterConverter().toUuid(getData(), ROLE_SYSTEM_ID);
	}

}

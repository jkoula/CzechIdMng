package eu.bcvsolutions.idm.acc.service.api.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.service.adapter.PluggableRoleAssignmentDeduplicator;

/**
 * This deduplicator checks whether two assigned roles create two different accounts. If so, it does not
 * return the role as duplicate.
 *
 * @author Tomáš Doischer
 */
@Component
public class AccIdentityAccountPluggableRoleAssignmentDeduplicator implements PluggableRoleAssignmentDeduplicator {

	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Override
	public AbstractRoleAssignmentDto getDuplicated(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two, Boolean skipSubdefinition) {
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(one.getId());
		List<AccIdentityAccountDto> accIdentityAccountsOne = identityAccountService.find(identityAccountFilter, null).getContent();

		identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(two.getId());
		List<AccIdentityAccountDto> accIdentityAccountsTwo = identityAccountService.find(identityAccountFilter, null).getContent();
		boolean canBeRemoved = true;

		for (AccIdentityAccountDto identityAccountOne : accIdentityAccountsOne) {
			for (AccIdentityAccountDto identityAccountTwo : accIdentityAccountsTwo) {
				if (!identityAccountOne.equals(identityAccountTwo)) {
					canBeRemoved = false;
				}
			}
		}

		if (canBeRemoved) {
			return one;
		}

		return null;
	}

	@Override
	public boolean considerOrder() {
		return false;
	}
}

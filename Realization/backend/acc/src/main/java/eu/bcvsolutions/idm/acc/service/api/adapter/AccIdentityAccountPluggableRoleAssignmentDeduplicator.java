package eu.bcvsolutions.idm.acc.service.api.adapter;

import java.util.List;

import org.slf4j.Logger;
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

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AccIdentityAccountPluggableRoleAssignmentDeduplicator.class);
	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Override
	public AbstractRoleAssignmentDto getDuplicated(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two, Boolean skipSubdefinition) {
		if (one == null || two == null || one.getId() == null || two.getId() == null) {
			LOG.debug("One or both of the role assignments are null, or are not yet persisted (do not assign any account), returning null");
			return null;
		}
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(one.getId());

		LOG.debug("Searching for identity accounts for role: {}", one.getId());
		List<AccIdentityAccountDto> accIdentityAccountsOne = identityAccountService.find(identityAccountFilter, null).getContent();
		LOG.debug("Found {} identity accounts for role: {}", accIdentityAccountsOne.size(), one.getId());

		identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(two.getId());

		LOG.debug("Searching for identity accounts for role: {}", two.getId());
		List<AccIdentityAccountDto> accIdentityAccountsTwo = identityAccountService.find(identityAccountFilter, null).getContent();
		LOG.debug("Found {} identity accounts for role: {}", accIdentityAccountsTwo.size(), two.getId());
		boolean canBeRemoved = true;

		for (AccIdentityAccountDto identityAccountOne : accIdentityAccountsOne) {
			for (AccIdentityAccountDto identityAccountTwo : accIdentityAccountsTwo) {
				if (!identityAccountOne.equals(identityAccountTwo)) {
					LOG.debug("Identity accounts {} and {} are different, so the role {} cannot be removed", identityAccountOne.getId(), identityAccountTwo.getId(), one.getId());
					canBeRemoved = false;
				}
			}
		}
		LOG.debug("Can the role {} be removed? {}", one.getId(), canBeRemoved);
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

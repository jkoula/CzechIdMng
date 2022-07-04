package eu.bcvsolutions.idm.acc.security.evaluator;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Permission to system owner relations (identity)
 *
 * @author Roman Kucera
 */
@Transactional
public class SystemOwnerAssignedByIdentityEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private SysSystemOwnerService systemOwnerService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private DefaultAccTestHelper accTestHelper;

	@Test
	public void canReadByRole() {
		List<SysSystemOwnerDto> systemOwners;

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		SysSystemDto system = accTestHelper.createSystem(getHelper().createName());
		SysSystemOwnerDto systemOwner = accTestHelper.createSystemOwner(system, identity);
		getHelper().createIdentityRole(identity, role);
		getHelper().createUuidPolicy(role.getId(), system.getId(), IdmBasePermission.READ);

		// check created identity doesn't have compositions
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			Assert.assertEquals(system.getId(), systemService.get(system.getId(), IdmBasePermission.READ).getId());
			systemOwners = systemOwnerService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(systemOwners.isEmpty());
		} finally {
			logout();
		}

		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				AccGroupPermission.SYSTEMOWNER,
				SysSystemOwner.class,
				SystemOwnerAssignedByIdentityEvaluator.class);

		try {
			getHelper().login(identity);

			// evaluate	access
			systemOwners = systemOwnerService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, systemOwners.size());
			Assert.assertEquals(systemOwner.getId(), systemOwners.get(0).getId());

			Set<String> permissions = systemOwnerService.getPermissions(systemOwner);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}

		getHelper().createUuidPolicy(role.getId(), system.getId(), IdmBasePermission.UPDATE);

		try {
			getHelper().login(identity);

			Set<String> permissions = systemOwnerService.getPermissions(systemOwner);
			Assert.assertEquals(4, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.CREATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.DELETE.name())));
		} finally {
			logout();
		}
	}
}

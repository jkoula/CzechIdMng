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
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Permission to system owner relations (role)
 * 
 * @author Roman Kucera
 *
 */
@Transactional
public class SystemOwnerAssignedByRoleEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private SysSystemOwnerRoleService systemOwnerRoleService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private DefaultAccTestHelper accTestHelper;
	
	@Test
	public void canReadByRole() {
		List<SysSystemOwnerRoleDto> systemOwners;

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		SysSystemDto system = accTestHelper.createSystem(getHelper().createName());
		SysSystemOwnerRoleDto systemOwnerRole;
		try {
			getHelper().loginAdmin();
			systemOwnerRole = accTestHelper.createSystemOwnerRole(system, role);
			getHelper().createIdentityRole(identity, role);
			getHelper().createUuidPolicy(role.getId(), system.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}

		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			Assert.assertEquals(system.getId(), systemService.get(system.getId(), IdmBasePermission.READ).getId());
			systemOwners = systemOwnerRoleService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(systemOwners.isEmpty());
		} finally {
			logout();
		}

		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				AccGroupPermission.SYSTEMOWNERROLE,
				SysSystemOwnerRole.class,
				SystemOwnerAssignedByRoleEvaluator.class);

		try {
			getHelper().login(identity.getUsername(), identity.getPassword());

			// evaluate	access
			systemOwners = systemOwnerRoleService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, systemOwners.size());
			Assert.assertEquals(systemOwnerRole.getId(), systemOwners.get(0).getId());

			Set<String> permissions = systemOwnerRoleService.getPermissions(systemOwnerRole);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}

		getHelper().createUuidPolicy(role.getId(), system.getId(), IdmBasePermission.UPDATE);

		try {
			getHelper().login(identity.getUsername(), identity.getPassword());

			Set<String> permissions = systemOwnerRoleService.getPermissions(systemOwnerRole);
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

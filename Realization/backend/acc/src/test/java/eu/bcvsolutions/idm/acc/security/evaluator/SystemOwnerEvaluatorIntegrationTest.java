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
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * System owner evaluator
 * - owner defined by identity
 * - owner defined by role
 * 
 * @author Roman Kucera
 *
 */
@Transactional
public class SystemOwnerEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private SysSystemService service;
	@Autowired
	private DefaultAccTestHelper accTestHelper;
	@Autowired
	private SecurityService securityService;

	@Test
	public void canReadByIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		SysSystemDto system = accTestHelper.createSystem(getHelper().createName());
		accTestHelper.createSystemOwner(system, identity);
		getHelper().createIdentityRole(identity, role);
		List<SysSystemDto> systems;

		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			Set<String> allAuthorities = securityService.getAllAuthorities();
			systems = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(systems.isEmpty());
		} finally {
			logout();
		}

		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				AccGroupPermission.SYSTEM,
				SysSystem.class,
				SystemOwnerEvaluator.class,
				IdmBasePermission.READ);

		try {
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			systems = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, systems.size());
			Assert.assertEquals(system.getId(), systems.get(0).getId());

			Set<String> permissions = service.getPermissions(system);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
	
	@Test
	public void canReadByRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		SysSystemDto system = accTestHelper.createSystem(getHelper().createName());
		accTestHelper.createSystemOwnerRole(system, role);
		getHelper().createIdentityRole(identity, role);
		List<SysSystemDto> systems;

		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			systems = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(systems.isEmpty());
		} finally {
			logout();
		}

		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				AccGroupPermission.SYSTEM,
				SysSystem.class,
				SystemOwnerEvaluator.class,
				IdmBasePermission.READ);

		try {
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			systems = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, systems.size());
			Assert.assertEquals(system.getId(), systems.get(0).getId());

			Set<String> permissions = service.getPermissions(system);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
}
	

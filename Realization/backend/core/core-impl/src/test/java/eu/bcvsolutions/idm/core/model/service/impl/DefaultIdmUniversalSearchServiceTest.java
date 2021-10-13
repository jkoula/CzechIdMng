package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmUniversalSearchFilter;
import eu.bcvsolutions.idm.core.api.service.IdmUniversalSearchService;
import eu.bcvsolutions.idm.core.unisearch.type.IdentityUniversalSearchType;
import eu.bcvsolutions.idm.core.unisearch.type.RoleUniversalSearchType;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultIdmUniversalSearchServiceTest extends AbstractIntegrationTest {

	@Autowired
	private IdmUniversalSearchService universalSearchService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	@Override
	public void logout() {
		super.logout();
	}

	@Test
	public void testFindIdentityByUsername() {
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, true);

		IdmIdentityDto identity = getHelper().createIdentity();

		IdmUniversalSearchFilter universalSearchFilter = new IdmUniversalSearchFilter();
		universalSearchFilter.setText(identity.getUsername());

		List<UniversalSearchDto> universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(1, universalSearchDtos.size());
		assertEquals(IdmIdentityDto.class.getCanonicalName(), universalSearchDtos.get(0).getOwnerType());
		assertEquals(identity, universalSearchDtos.get(0).getOwnerDto());

		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
	}

	@Test
	public void testFindRoleByCode() {
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, true);

		IdmRoleDto role = getHelper().createRole();

		IdmUniversalSearchFilter universalSearchFilter = new IdmUniversalSearchFilter();
		universalSearchFilter.setText(role.getCode());

		List<UniversalSearchDto> universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(1, universalSearchDtos.size());
		assertEquals(IdmRoleDto.class.getCanonicalName(), universalSearchDtos.get(0).getOwnerType());
		assertEquals(role, universalSearchDtos.get(0).getOwnerDto());

		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
	}

	@Test
	public void testFindRoleAndIdentity() {
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, true);

		String name = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity(name + "_suffix_identity");
		IdmRoleDto role = getHelper().createRole(name + "_suffix_role");

		IdmUniversalSearchFilter universalSearchFilter = new IdmUniversalSearchFilter();
		universalSearchFilter.setText(name);

		List<UniversalSearchDto> universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(2, universalSearchDtos.size());
		// Role
		UniversalSearchDto roleUniversalSearchDto = universalSearchDtos.stream()
				.filter(universalSearchDto -> RoleUniversalSearchType.NAME.equals(universalSearchDto.getType().getType())).findFirst().orElse(null);
		assertNotNull(roleUniversalSearchDto);
		assertEquals(IdmRoleDto.class.getCanonicalName(), roleUniversalSearchDto.getOwnerType());
		assertEquals(role, roleUniversalSearchDto.getOwnerDto());
		// Identity
		UniversalSearchDto identityUniversalSearchDto = universalSearchDtos.stream()
				.filter(universalSearchDto -> IdentityUniversalSearchType.NAME.equals(universalSearchDto.getType().getType())).findFirst().orElse(null);
		assertNotNull(identityUniversalSearchDto);
		assertEquals(IdmIdentityDto.class.getCanonicalName(), identityUniversalSearchDto.getOwnerType());
		assertEquals(identity, identityUniversalSearchDto.getOwnerDto());

		// Turn off identity.
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		// Only one role should be returned now.
		universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(1, universalSearchDtos.size());
		roleUniversalSearchDto = universalSearchDtos.get(0);
		assertNotNull(roleUniversalSearchDto);
		assertEquals(IdmRoleDto.class.getCanonicalName(), roleUniversalSearchDto.getOwnerType());
		assertEquals(role, roleUniversalSearchDto.getOwnerDto());

		// Turn off role.
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		// Only one identity should be returned now.
		universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(1, universalSearchDtos.size());
		identityUniversalSearchDto = universalSearchDtos.get(0);
		assertNotNull(identityUniversalSearchDto);
		assertEquals(IdmIdentityDto.class.getCanonicalName(), identityUniversalSearchDto.getOwnerType());
		assertEquals(identity, identityUniversalSearchDto.getOwnerDto());

		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
	}

}

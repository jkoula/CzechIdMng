package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.unisearch.type.SystemUniversalSearchType;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmUniversalSearchFilter;
import eu.bcvsolutions.idm.core.api.service.IdmUniversalSearchService;
import eu.bcvsolutions.idm.core.unisearch.type.IdentityUniversalSearchType;
import eu.bcvsolutions.idm.core.unisearch.type.RoleUniversalSearchType;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleUniversalSearchTypeTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
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
	public void testFindSystemByCode() {
		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, true);
		getHelper().setConfigurationValue(SystemUniversalSearchType.PROPERTY_SEARCH_TYPE, true);

		String name = getHelper().createName();

		SysSystemDto system = helper.createSystem(name + "_suffix_system");

		IdmUniversalSearchFilter universalSearchFilter = new IdmUniversalSearchFilter();
		universalSearchFilter.setText(name);

		List<UniversalSearchDto> universalSearchDtos = universalSearchService.find(universalSearchFilter, null).getContent();
		assertEquals(1, universalSearchDtos.size());
		assertEquals(SysSystemDto.class.getCanonicalName(), universalSearchDtos.get(0).getOwnerType());
		assertEquals(system, universalSearchDtos.get(0).getOwnerDto());

		getHelper().setConfigurationValue(IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(RoleUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
		getHelper().setConfigurationValue(SystemUniversalSearchType.PROPERTY_SEARCH_TYPE, false);
	}
}

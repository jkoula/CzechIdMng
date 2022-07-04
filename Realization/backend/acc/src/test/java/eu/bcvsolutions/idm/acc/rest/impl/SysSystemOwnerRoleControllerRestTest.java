package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 *
 * @author Roman Kucera
 */
public class SysSystemOwnerRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysSystemOwnerRoleDto> {

	@Autowired
	private SysSystemOwnerRoleController controller;
	@Autowired
	private DefaultAccTestHelper accTestHelper;

	@Override
	protected AbstractReadWriteDtoController<SysSystemOwnerRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysSystemOwnerRoleDto prepareDto() {
		SysSystemOwnerRoleDto dto = new SysSystemOwnerRoleDto();
		dto.setSystem(accTestHelper.createSystem(getHelper().createName()).getId());
		dto.setOwnerRole(getHelper().createRole().getId());
		return dto;
	}

	@Test
	public void testFindBySystem() {
		SysSystemOwnerRoleDto dto = createDto(prepareDto());

		SysSystemOwnerRoleFilter filter = new SysSystemOwnerRoleFilter();
		filter.setSystem(dto.getSystem());
		List<SysSystemOwnerRoleDto> results = find(filter);
		Assert.assertEquals(1, results.size());

		filter.setSystem(UUID.randomUUID());
		results = find(filter);
		Assert.assertEquals(0, results.size());
	}

	@Test
	public void testFindByRole() {
		SysSystemOwnerRoleDto dto = createDto(prepareDto());

		SysSystemOwnerRoleFilter filter = new SysSystemOwnerRoleFilter();
		filter.setOwnerRole(dto.getOwnerRole());
		List<SysSystemOwnerRoleDto> results = find(filter);
		Assert.assertEquals(1, results.size());

		filter.setOwnerRole(UUID.randomUUID());
		results = find(filter);
		Assert.assertEquals(0, results.size());
	}
}

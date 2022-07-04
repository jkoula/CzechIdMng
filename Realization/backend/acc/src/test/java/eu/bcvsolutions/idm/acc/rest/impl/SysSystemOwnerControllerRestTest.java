package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests
 *
 * @author Roman Kucera
 */
public class SysSystemOwnerControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysSystemOwnerDto> {

	@Autowired
	private SysSystemOwnerController controller;
	@Autowired
	private DefaultAccTestHelper accTestHelper;

	@Override
	protected AbstractReadWriteDtoController<SysSystemOwnerDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysSystemOwnerDto prepareDto() {
		SysSystemOwnerDto dto = new SysSystemOwnerDto();
		dto.setSystem(accTestHelper.createSystem(getHelper().createName()).getId());
		dto.setOwner(getHelper().createIdentity((GuardedString) null).getId());
		return dto;
	}

	@Test
	@Transactional
	public void testFindBySystem() {
		SysSystemOwnerDto dto = createDto(prepareDto());

		SysSystemOwnerFilter filter = new SysSystemOwnerFilter();
		filter.setSystem(dto.getSystem());
		List<SysSystemOwnerDto> results = find(filter);
		Assert.assertEquals(1, results.size());

		filter.setSystem(UUID.randomUUID());
		results = find(filter);
		Assert.assertEquals(0, results.size());
	}

	@Test
	@Transactional
	public void testFindByIdentity() {
		SysSystemOwnerDto dto = createDto(prepareDto());

		SysSystemOwnerFilter filter = new SysSystemOwnerFilter();
		filter.setOwner(dto.getOwner());
		List<SysSystemOwnerDto> results = find(filter);
		Assert.assertEquals(1, results.size());

		filter.setOwner(UUID.randomUUID());
		results = find(filter);
		Assert.assertEquals(0, results.size());
	}
}

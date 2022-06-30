package eu.bcvsolutions.idm.acc.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests
 * - TODO: move filters here
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
}

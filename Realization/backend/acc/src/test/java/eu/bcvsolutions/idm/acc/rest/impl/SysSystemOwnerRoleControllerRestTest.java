package eu.bcvsolutions.idm.acc.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
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
}

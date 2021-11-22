package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Asynchronous account management
 * - asynchronous event processing is enabled - check account management and provisioning
 * - asynchronous event processing is enabled - account managment fails.
 * 
 * @author Radek Tomiška
 *
 */
public class AsynchronousAccountManagementIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemAttributeMappingService attributeMappingService;
	@Autowired private IdmEntityEventService entityEventService;
	
	@Before
	public void init() {
		loginAsAdmin();
		//
		getHelper().enableAsynchronousProcessing();
	}

	@After
	public void logout() {
		getHelper().disableAsynchronousProcessing();
		//
		super.logout();
	}
	
	@Test
	public void testAsynchronousAccountManagementGreenLine() {
		IdmIdentityDto identity = getHelper().createIdentity();
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);
		//
		try {			
			getHelper().waitForResult(res -> {
				IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
				eventFilter.setTransactionId(role.getTransactionId());
				eventFilter.setStates(Lists.newArrayList(OperationState.CREATED, OperationState.RUNNING));
				//
				return entityEventService.count(eventFilter) != 0;
			});
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(account);
			Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
		} finally {
			identityService.delete(identity);
			systemService.delete(system);
		}
	}
	
	@Test
	public void testAsynchronousAccountManagementError() {
		// add error to some script
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		SysSystemMappingDto mapping = getHelper().getDefaultMapping(system);
		SysSystemAttributeMappingDto attributeHandlingUserName = attributeMappingService
				.findBySystemMappingAndName(mapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		// username is transformed with error
		attributeHandlingUserName.setTransformToResourceScript("returan \"" + "error" + "\";");
		attributeHandlingUserName = attributeMappingService.save(attributeHandlingUserName);
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(identity, role);
		try {
			getHelper().waitForResult(res -> {
				IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
				eventFilter.setTransactionId(identityRole.getTransactionId());
				eventFilter.setStates(Lists.newArrayList(OperationState.CREATED, OperationState.RUNNING));
				//
				return entityEventService.count(eventFilter) != 0;
			});
			
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNull(account);
			//
			// find event result with exception
			IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
			eventFilter.setOwnerId(identityRole.getId());
			eventFilter.setStates(Lists.newArrayList(OperationState.EXCEPTION));
			List<IdmEntityEventDto> failedEvents = entityEventService.find(eventFilter, null).getContent();
			//
			Assert.assertEquals(1, failedEvents.size());
			Assert.assertEquals(AccResultCode.GROOVY_SCRIPT_ATTR_TRANSFORMATION_FAILED.getCode(), failedEvents.get(0).getResult().getCode());
			
		} finally {
			identityService.delete(identity);
			systemService.delete(system);
		}
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}

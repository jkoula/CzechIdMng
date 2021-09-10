package eu.bcvsolutions.idm.core.audit.service.impl;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * SIEM logger integration test.
 * 
 * @author Ondrej Husnik
 *
 */
public class SiemLoggerManagerIntegrationTest extends AbstractIntegrationTest {
	
	private String LEVEL1_KEY = "LEVEL1_KEY";
	private String LEVEL2_KEY = "LEVEL2_KEY";
	
	private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
	private final PrintStream originalStdOut = System.out;

	@Autowired
	private SiemLoggerManager logger;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Before
	public void init() {
		setLogPropertyValue("INFO");
	}

	@After
	public void cleanUp() {
		setLogPropertyValue("ERROR");
		setOriginalStandardOut();
		stdOut.reset();
	}
	
	
	@Test
	public void basicLogAllNullTest() {
		setTestStandardOut();
		logger.log(null, null, null, null, null, null, null, null);
		String out = stdOut.toString();		
		String pattern = createPattern(SiemLoggerManager.ROOT_LEVEL_KEY, "", "", "", "", "", "", "");
		isLogPatternValid(out, pattern);
	}
	
	@Test
	public void dtoLogTest() {
		IdmIdentityDto identityDto = getHelper().createIdentity();
		IdmRoleDto roleDto = getHelper().createRole();
		String action = logger.buildAction(LEVEL1_KEY, LEVEL2_KEY);
		String transactionId = UUID.randomUUID().toString();
		
		
		String expectedAction = String.format("%s.%s.%s", SiemLoggerManager.ROOT_LEVEL_KEY, LEVEL1_KEY, LEVEL2_KEY);
		
		setTestStandardOut();
		logger.log(action, SiemLoggerManager.SUCCESS_ACTION_STATUS, identityDto, null, transactionId, null);
		String out = stdOut.toString();
		setOriginalStandardOut();
		String pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, identityDto.getCode(), identityDto.getId().toString(), "",  "", transactionId, "");
		isLogPatternValid(out, pattern);
		
		setTestStandardOut();
		logger.log(action, SiemLoggerManager.SUCCESS_ACTION_STATUS, identityDto, roleDto, transactionId, null);
		out = stdOut.toString();
		setOriginalStandardOut();
		pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, identityDto.getCode(), identityDto.getId().toString(), roleDto.getCode(),  roleDto.getId().toString(), transactionId, "");
		isLogPatternValid(out, pattern);
		
		setTestStandardOut();
		logger.log(action, SiemLoggerManager.SUCCESS_ACTION_STATUS, null, roleDto, transactionId, null);
		out = stdOut.toString();
		setOriginalStandardOut();
		pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, "", "", roleDto.getCode(),  roleDto.getId().toString(), transactionId, "");
		isLogPatternValid(out, pattern);
		
		setTestStandardOut();
		logger.log(action, SiemLoggerManager.SUCCESS_ACTION_STATUS, null, null, transactionId, null);
		out = stdOut.toString();
		setOriginalStandardOut();
		pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, "", "", "", "", transactionId, "");
		isLogPatternValid(out, pattern);	
	}
	
	
	@Test
	public void identityEventLogTest() {
		IdmIdentityDto identityDto = getHelper().createIdentity();
		IdentityEvent event = new IdentityEvent(IdentityEvent.IdentityEventType.CREATE, identityDto);
		
		String expectedAction = String.format("%s.%s.%s", SiemLoggerManager.ROOT_LEVEL_KEY, SiemLoggerManager.IDENTITY_LEVEL_KEY, event.getType().toString());
		String transactionId = Objects.toString(identityDto.getTransactionId(),"");
		
		setTestStandardOut();
		Class<?> clazz = identityService.getClass();
		Method method;
		try {
			method = clazz.getDeclaredMethod("siemLog", EntityEvent.class, String.class, String.class);
			method.setAccessible(true);;
			method.invoke(identityService, event, SiemLoggerManager.SUCCESS_ACTION_STATUS, "");
		} catch (Exception e) {
			fail("Failed to invoke crucial method");
		}
		String out = stdOut.toString();
		setOriginalStandardOut();
		String pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, identityDto.getCode(), identityDto.getId().toString(), "",  "", transactionId, "");
		isLogPatternValid(out, pattern);	
	}
	
	@Test
	public void roleRequestEventLogTest() {
		IdmIdentityDto identityDto = getHelper().createIdentity();
		IdmRoleDto roleDto = getHelper().createRole();
		IdmRoleRequestDto roleRequestDto = getHelper().createRoleRequest(identityDto, roleDto);
		RoleRequestEvent event = new RoleRequestEvent(RoleRequestEventType.CREATE, roleRequestDto);

		String expectedAction = String.format("%s.%s.%s", SiemLoggerManager.ROOT_LEVEL_KEY, SiemLoggerManager.ROLE_REQUEST_LEVEL_KEY, event.getType().toString());
		String transactionId = Objects.toString(roleRequestDto.getTransactionId(),"");
		
		setTestStandardOut();
		Class<?> clazz = roleRequestService.getClass();
		Method method;
		try {
			method = clazz.getDeclaredMethod("siemLog", EntityEvent.class, String.class, String.class);
			method.setAccessible(true);
			method.invoke(roleRequestService, event, SiemLoggerManager.SUCCESS_ACTION_STATUS, "");
		} catch (Exception e) {
			fail("Failed to invoke crucial method");
		}
		String out = stdOut.toString();
		setOriginalStandardOut();
		String pattern = createPattern(expectedAction, SiemLoggerManager.SUCCESS_ACTION_STATUS, "", roleRequestDto.getId().toString(), "", "", transactionId, roleRequestDto.getState().toString());
		isLogPatternValid(out, pattern);		
	}
	
	/**
	 * Performance test of the new SIEM logging.
	 * Serves for manual performance evaluation. 
	 */
	@Test
	@Ignore
	public void loggingPerformanceTest() {
		IdmIdentityDto identityDto = getHelper().createIdentity();
		IdmRoleDto roleDto = getHelper().createRole();
		
		setLogPropertyValue("ERROR");
		
		int testCycles=10000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < testCycles; ++i) {
			IdmRoleRequestDto roleRequestDto = getHelper().createRoleRequest(identityDto, ConceptRoleRequestOperation.ADD, roleDto);
			getHelper().executeRequest(roleRequestDto, false, true);
			
			roleRequestDto = getHelper().createRoleRequest(identityDto, ConceptRoleRequestOperation.REMOVE, roleDto);
			getHelper().executeRequest(roleRequestDto, false, true);
		}
		long duration = System.currentTimeMillis() - start;
		
		System.out.println(String.format("************AUDIT LOG *************\nCycles: %d in %dsec", testCycles, duration/1000));
	}
	
	private String createPattern(String action, String status, String targetName, String targetUuid, 
				String subjectName, String subjectUuid, String transactionUuid, String reason) {
		StringBuffer pattern = new StringBuffer();
		pattern.append(String.format("%s.log : ", action))
		.append(String.format("%s:[%s] ", SiemLoggerManager.RESULT_ATTRIBUTE, status))
		.append(String.format("%s:[%s] ", SiemLoggerManager.TARGET_NAME_ATTRIBUTE, targetName))
		.append(String.format("%s:[%s] ", SiemLoggerManager.TARGET_UUID_ATTRIBUTE, targetUuid))
		.append(String.format("%s:[%s] ", SiemLoggerManager.SUBJECT_NAME_ATTRIBUTE, subjectName))
		.append(String.format("%s:[%s] ", SiemLoggerManager.SUBJECT_UUID_ATTRIBUTE, subjectUuid))
		.append(String.format("%s:[%s] ", SiemLoggerManager.PERFORMED_BY_NAME_ATTRIBUTE, securityService.getCurrentUsername()))
		.append(String.format("%s:[%s] ", SiemLoggerManager.PERFORMED_BY_UUID_ATTRIBUTE, Objects.toString(securityService.getCurrentId(),"")))
		.append(String.format("%s:[%s] ", SiemLoggerManager.TRANSACTION_UUID_ATTRIBUTE, transactionUuid))
		.append(String.format("%s:[%s]", SiemLoggerManager.DETAIL_ATTRIBUTE, reason));
		return pattern.toString();
	}
	
	private void isLogPatternValid(String log, String pattern) {
		int start = StringUtils.indexOf(log, SiemLoggerManager.ROOT_LEVEL_KEY);
		String out = log.substring(start);
		out = out.trim();
		Assert.assertEquals(pattern, out);
	}

	private void setTestStandardOut() {
		stdOut.reset();
		System.setOut(new PrintStream(stdOut));
	}
	
	private void setOriginalStandardOut() {
		System.setOut(originalStdOut);
		stdOut.reset();
	}
	
	private void setLogPropertyValue(String level) {
		getHelper().setConfigurationValue(SiemLoggerManager.CONFIGURATION_PROPERTY_PREFIX+"."+SiemLoggerManager.ROOT_LEVEL_KEY, level);
	}
}

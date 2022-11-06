package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test change permissions for identity.
 *
 * @author svandav
 * @author Tomáš Doischer
 *
 */
public abstract class AbstractChangeIdentityPermissionTest extends AbstractCoreWorkflowIntegrationTest {

	public static final String APPROVE_ROLE_BY_GUARANTEE_KEY = "approve-role-by-guarantee";
	public static final String APPROVE_ROLE_BY_SECURITY_KEY = "approve-role-by-guarantee-security";
	public static final String APPROVE_ROLE_BY_MANAGER_KEY = "approve-role-by-manager";
	public static final String APPROVE_REMOVE_ROLE_BY_MANAGER_KEY = "approve-remove-role-by-manager";
	public static final String SECURITY_ROLE_TEST = "securityRoleTest";
	// FIXME: move to api (workflow config constant)
	public static final String APPROVE_BY_HELPDESK_ROLE = "idm.sec.core.wf.approval.helpdesk.role";
	public static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	public static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	public static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	public static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	public static final String APPROVE_BY_SECURITY_ROLE = "idm.sec.core.wf.approval.security.role";
	public static final String APPROVE_INCOMPATIBLE_ENABLE = "idm.sec.core.wf.approval.incompatibility.enabled";
	public static final String APPROVE_INCOMPATIBLE_ROLE = "idm.sec.core.wf.approval.incompatibility.role";
	public static final String INCOMPATIBILITY_ROLE_TEST = "IncompatibilityRoleTest";

	public static final GuardedString TEST_APPLICANT_PWD = new GuardedString(UUID.randomUUID().toString());
	//
	@Autowired
	public WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	public WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;
	@Autowired
	public WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	public IdmIdentityService identityService;
	@Autowired
	public IdmIdentityContractService identityContractService;
	@Autowired
	public IdmRoleRequestService roleRequestService;
	@Autowired
	public IdmRoleService roleService;
	@Autowired
	public RoleConfiguration roleConfiguration;
	@Autowired
	public IdmConfigurationService configurationService;
	@Autowired
	public SecurityService securityService;
	@Autowired
	public IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired
	public IdmRequestIdentityRoleService requestIdentityRoleService;
	@Autowired
	public RuntimeService runtimeService;
	@Autowired
	public LoginService loginService;
	@Autowired
	public WorkflowHistoricTaskInstanceService workflowHistoricTaskInstanceService;

	@Before
	public void init() {
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, true);
	}

	@After
	@Override
	public void evictCaches() {
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_INCOMPATIBLE_ENABLE, false);
		//
		configurationService.deleteValue(APPROVE_BY_HELPDESK_ROLE);
		configurationService.deleteValue(APPROVE_BY_SECURITY_ROLE);
		configurationService.deleteValue(APPROVE_INCOMPATIBLE_ROLE);
		//
		configurationService.deleteValue(APPROVE_ROLE_BY_MANAGER_KEY);
		configurationService.deleteValue(APPROVE_ROLE_BY_SECURITY_KEY);
		configurationService.deleteValue(APPROVE_ROLE_BY_GUARANTEE_KEY);
		configurationService.deleteValue(APPROVE_REMOVE_ROLE_BY_MANAGER_KEY);
		//
		super.logout();
	}

	@Test
	public void approveIncompatibleRolesTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_INCOMPATIBLE_ENABLE, "true");
		// Set incompatibility role test
		configurationService.setValue(APPROVE_INCOMPATIBLE_ROLE, INCOMPATIBILITY_ROLE_TEST);
		// Create test role for load candidates on approval incompatibility (TEST_USER_1)
		IdmRoleDto role = getHelper().createRoleIfNotExists(INCOMPATIBILITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);

		// Create definition of incompatible roles
		IdmRoleDto incompatibleRoleOne = getHelper().createRole();
		IdmRoleDto incompatibleRoleTwo = getHelper().createRole();
		IdmIncompatibleRoleDto incompatibleRole = new IdmIncompatibleRoleDto();
		incompatibleRole.setSub(incompatibleRoleOne.getId());
		incompatibleRole.setSuperior(incompatibleRoleTwo.getId());
		incompatibleRole = incompatibleRoleService.save(incompatibleRole);

		// Assign first incompatible role
		createRoleAssignment(incompatibleRoleOne.getId(), owner.getId(), null, null);
		loginAsAdmin();

		// Create request
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, incompatibleRoleTwo, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
		// Check on incompatible role in the request
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = roleRequestService.getIncompatibleRoles(request);
		assertEquals(2, incompatibleRoles.size());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
		// SECURITY turn off
		// ROLE INCOMPATIBILITY turn on
		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve", "approveIncompatibilities");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertNotNull(concept.getWfProcessId());
		// Find all identity roles for applicant
		List<AbstractRoleAssignmentDto> assignedRoles = findRoleAssignmentsForOwner(owner);
		boolean exists = assignedRoles.stream()
			.anyMatch(identityRole -> incompatibleRoleTwo.getId().equals(identityRole.getRole()));
		// Incompatible role two must be assigned for applicant
		assertTrue(exists);

		// Create next request
		IdmRoleRequestDto requestNext = createRoleRequest(applicantId);
		requestNext = roleRequestService.save(requestNext);
		// Check on incompatible role in the request
		// Incompatibilities exist for this user, but not in this request (none concept
		// added new role is presents)
		incompatibleRoles = roleRequestService.getIncompatibleRoles(requestNext);
		assertEquals(0, incompatibleRoles.size());
	}

	@Test
	public void defaultWithoutApproveTest() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_INCOMPATIBLE_ENABLE, "false");
		// Set incompatibility role test
		configurationService.setValue(APPROVE_INCOMPATIBLE_ROLE, INCOMPATIBILITY_ROLE_TEST);
		// Create test role for load candidates on approval incompatibility (TEST_USER_1)
		IdmRoleDto role = getHelper().createRole(INCOMPATIBILITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);

		// Create definition of incompatible roles
		IdmRoleDto incompatibleRoleOne = getHelper().createRole();
		IdmRoleDto incompatibleRoleTwo = getHelper().createRole();
		IdmIncompatibleRoleDto incompatibleRole = new IdmIncompatibleRoleDto();
		incompatibleRole.setSub(incompatibleRoleOne.getId());
		incompatibleRole.setSuperior(incompatibleRoleTwo.getId());
		incompatibleRole = incompatibleRoleService.save(incompatibleRole);

		// Assign first incompatible role
		createRoleAssignment(incompatibleRoleOne.getId(), owner.getId(), null, null);
		loginAsAdmin();

		// Create request
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		createConceptRoleRequest(request, incompatibleRoleTwo, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
		// Check on incompatible role in the request
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = roleRequestService.getIncompatibleRoles(request);
		assertEquals(2, incompatibleRoles.size());

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
		// SECURITY turn off
		// ROLE INCOMPATIBILITY turn off
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
	}

	@Test
	public void testTaskCount() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto guarantee = getHelper().createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		role = roleService.save(role);
		getHelper().createRoleGuarantee(role, guarantee);
		//
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());


		// check task before create request
		loginAsAdmin();
		int taskCount = getHistoricProcess(now).size();

		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// check tasks after create request, must be +1
		int taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 1, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));

		// HELPDESK
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		checkAndCompleteOneTask(taskFilter, applicantId, "approve", null);

		// check tasks by identity, must be + 2 (main process + sub process)
		taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 2, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));

		// Subprocess - approve by GUARANTEE
		loginAsAdmin(guarantee.getUsername());
		taskFilter.setCandidateOrAssigned(guarantee.getUsername());
		checkAndCompleteOneTask(taskFilter, applicantId, "approve", null);

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		// check task on the end (same as before)
		taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 2, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));
	}

	@Test
	public void testCompleteTaskByStarter() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		IdmIdentityDto noAdmin = getHelper().createIdentity();
		//
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicantId);

		createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + applicant.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCompleteTaskByAnotherUser() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");

		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + applicant.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertEquals(CoreResultCode.FORBIDDEN.name(), ex.getError().getError().getStatusEnum());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(test2.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + test2.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertEquals(CoreResultCode.FORBIDDEN.name(), ex.getError().getError().getStatusEnum());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCompleteTaskByPreviosApprover() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto guarantee = getHelper().createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		getHelper().createRoleGuarantee(role, guarantee);
		role = roleService.save(role);
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicantId);

		createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + applicant.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertEquals(CoreResultCode.FORBIDDEN.name(), ex.getError().getError().getStatusEnum());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
			fail("This user: " + helpdeskIdentity.getUsername() + " should not be able to approve task.");
		} catch (ResultCodeException ex) {
			assertEquals(CoreResultCode.FORBIDDEN.name(), ex.getError().getError().getStatusEnum());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(applicant.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
			fail("This user: " + applicant.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertEquals(CoreResultCode.FORBIDDEN.name(), ex.getError().getError().getStatusEnum());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(guarantee.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testGetTaskByAnotherUser() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto anotherUser = getHelper().createIdentity();

		IdmRoleDto role = getHelper().createRole();

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		// check task before create request
		loginAsAdmin(applicant.getUsername());

		IdmRoleRequestDto request = createRoleRequest(applicantId);

		createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK login
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());

		WorkflowTaskInstanceDto taskInstanceDto = tasks.get(0);
		String id = taskInstanceDto.getId();

		WorkflowTaskInstanceDto workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// check task get by id
		loginWithout(applicant.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNull(workflowTaskInstanceDto);

		loginWithout(anotherUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNull(workflowTaskInstanceDto);

		// candidate
		loginWithout(helpdeskIdentity.getUsername(), IdmGroupPermission.APP_ADMIN,
				CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// WF admin
		loginWithout(InitTestDataProcessor.TEST_ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// Attacker
		loginWithout(anotherUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		try {
			tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			fail();
		} catch (ResultCodeException ex) {
			assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
		} catch (Exception e) {
			fail();
		}
	}


	@Test
	public void addRoleWithSubprocessManagerTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		IdmRoleDto directRole = roleService.save(role);
		getHelper().createRoleGuarantee(directRole, test2);
		IdmRoleDto subRole = getHelper().createRole();
		getHelper().createRoleComposition(role, subRole);
		
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");

		// SECURITY
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		List<AbstractRoleAssignmentDto> assigedRoles = findRoleAssignmentsForOwner(owner);
		Assert.assertEquals(2, assigedRoles.size());
		Assert.assertTrue(assigedRoles.stream().anyMatch(ir -> ir.getRole().equals(directRole.getId())));
		Assert.assertTrue(assigedRoles.stream().anyMatch(ir -> ir.getRole().equals(subRole.getId())));
	}

	@Test
	public void addRoleWithSubprocessDisapproveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		getHelper().createRoleGuarantee(role, test2);
		role = roleService.save(role);
		IdmRoleDto subRole = getHelper().createRole();
		getHelper().createRoleComposition(role, subRole);
		
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);


		IdmRoleRequestDto request = createRoleRequest(applicantId);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "disapprove");

		// SECURITY
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		List<AbstractRoleAssignmentDto> assignedRoles = findRoleAssignmentsForOwner(owner);
		assertEquals(0, assignedRoles.size());
	}

	@Test
	public void cancelWfOnRoleRequestDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();

		IdmRoleRequestDto request = createRoleRequest(applicantId);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		String conceptWf = concept.getWfProcessId();
		assertNull(conceptWf);

		request = roleRequestService.get(request.getId());
		String requestWf = request.getWfProcessId();
		assertNotNull(requestWf);
		assertNotNull(workflowProcessInstanceService.get(requestWf));
		// Delete the request
		roleRequestService.delete(request);
		// WF have to be cancelled

		assertNull(roleRequestService.get(request.getId()));
		assertNull(workflowProcessInstanceService.get(requestWf));
	}

	@Test
	public void cancelSubprocessOnContractDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		deleteOwner(owner);

		// Concept has to be in the Cancel state and WF must be ended
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertEquals(RoleRequestState.CANCELED, concept.getState());
		assertNotNull(concept.getWfProcessId());
		assertNull(workflowProcessInstanceService.get(conceptWf));
		request = roleRequestService.get(request.getId());
		// Main process has to be executed
		assertEquals(RoleRequestState.EXECUTED, request.getState());
	}

	protected abstract void deleteOwner(AbstractDto owner);

	@Test
	public void cancelSubprocessOnRoleDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = getHelper().createRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		IdmRoleRequestDto request = createRoleRequest(applicantId);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		// Delete the role that is using in the concept
		UUID roleId = concept.getRole();
		assertNotNull(roleId);

		findRoleAssignmentsForOwner(owner).forEach(assignedRole -> getRoleAssignmentService().delete(assignedRole));

		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		roleService.deleteById(roleId);

		// Concept has to be in the Cancel state and WF must be ended
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());
		assertEquals(RoleRequestState.CANCELED, concept.getState());
		assertNotNull(concept.getWfProcessId());
		assertNull(workflowProcessInstanceService.get(conceptWf));
		request = roleRequestService.get(request.getId());
		// Main process has to be executed
		assertEquals(RoleRequestState.EXECUTED, request.getState());

	}

	@Test
	public void testFindCandidatesWithoutSubprocess() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		//
		loginAsAdmin();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());


		loginAsNoAdmin(applicant.getUsername());
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		createConceptRoleRequest(request, role, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicantId);
		requestIdentityRoleFilter.setRoleId(role.getId()); // TODO temporary
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		IdmRequestIdentityRoleDto requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		Set<IdmIdentityDto> candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(0, candidates.size());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicantId);
		requestIdentityRoleFilter.setRoleId(role.getId()); // TODO temporary
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());
	}

	@Test
	public void testFindCandidatesWithSubprocess() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// approve only by help desk
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");

		loginAsAdmin();

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto guarantee = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, guarantee);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);
				//APPROVE_ROLE_BY_MANAGER_KEY);

		IdmRoleRequestDto request = createRoleRequest(applicantId);

		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicantId);
		requestIdentityRoleFilter.setRoleId(adminRole.getId()); // TODO temporary
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		IdmRequestIdentityRoleDto requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		Set<IdmIdentityDto> candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(0, candidates.size());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicantId);
		requestIdentityRoleFilter.setRoleId(adminRole.getId()); // TODO temporary
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		IdmRoleRequestFilter filter = new IdmRoleRequestFilter();
		filter.setIncludeApprovers(true);
		IdmRoleRequestDto requestDto = roleRequestService.get(request.getId(), filter);
		assertEquals(1, requestDto.getApprovers().size());

		// HELPDESK
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");

		filter.setIncludeApprovers(false);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertNull(requestDto.getApprovers());

		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(guarantee.getUsername());
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = (AbstractConceptRoleRequestDto) getConceptRoleService().get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());
		IdmIdentityDto approversFromProcess = candidates.stream().findFirst().get();

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(1, candidates.size());
		IdmIdentityDto approversFromSubProcess = candidates.stream().findFirst().get();
		assertEquals(approversFromProcess.getId(), approversFromSubProcess.getId());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicantId);
		requestIdentityRoleFilter.setRoleId(adminRole.getId()); // TODO temporary
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertEquals(1, requestIdentityRoleDto.getCandidates().size());

		requestIdentityRoleFilter.setIncludeCandidates(false);
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		filter = new IdmRoleRequestFilter();
		filter.setIncludeApprovers(true);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertEquals(1, requestDto.getApprovers().size());

		filter.setIncludeApprovers(false);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertNull(requestDto.getApprovers());
	}

	@Test
	public void testAccessIsAddedForOwnerAndImplementerToSubprocesses() {
		// reset approvers
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, false);
		// role with guarantees and critical 2 => approve by guarantee
		IdmRoleDto role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setPriority(2); // default by configuration
		IdmRoleDto roleOne = roleService.save(role);
		role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setPriority(2); // default by configuration
		IdmRoleDto roleTwo = roleService.save(role);
		//
		IdmIdentityDto implementer = getHelper().createIdentity();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto guaranteeOne = getHelper().createIdentity();
		IdmIdentityDto guaranteeTwo = getHelper().createIdentity();
		//
		getHelper().createRoleGuarantee(roleOne, guaranteeOne);
		getHelper().createRoleGuarantee(roleTwo, guaranteeTwo);
		//
		loginAsAdmin(implementer.getUsername()); // login as implementer
		//
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		AbstractConceptRoleRequestDto concept = 
				createConceptRoleRequest(request, roleOne, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
		concept = createConceptRoleRequest(request, roleTwo, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicant.getId());
		requestIdentityRoleFilter.setOnlyChanges(true);

		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());
		Assert.assertTrue(requestIdentityRoles.stream().anyMatch(rir -> rir.getRole().equals(roleOne.getId())
				&& rir.getCandidates().size() == 1
				&& rir.getCandidates().iterator().next().getId().equals(guaranteeOne.getId())));
		Assert.assertTrue(requestIdentityRoles.stream().anyMatch(rir -> rir.getRole().equals(roleTwo.getId())
				&& rir.getCandidates().size() == 1
				&& rir.getCandidates().iterator().next().getId().equals(guaranteeTwo.getId())));
		//
		// check applicant and implemented can read process instance
		getHelper().login(implementer);
		List<WorkflowProcessInstanceDto> processes = workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getContent();
		Assert.assertEquals(3, processes.size());
		getHelper().login(applicant.getUsername(), TEST_APPLICANT_PWD);
		Assert.assertEquals(3, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		getHelper().login(guaranteeOne);
		Assert.assertEquals(1, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		getHelper().login(guaranteeTwo);
		Assert.assertEquals(1, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		//
		// test identity links are created (=> access added)
		processes.forEach(process -> {
			List<IdentityLink> links = runtimeService.getIdentityLinksForProcessInstance(process.getProcessInstanceId());
			Assert.assertTrue(links.stream().anyMatch(l -> l.getUserId().equals(implementer.getId().toString())
					&& l.getType().equals(IdentityLinkType.STARTER)));
			Assert.assertTrue(links.stream().anyMatch(l -> l.getUserId().equals(applicant.getId().toString())
					&& l.getType().equals(IdentityLinkType.OWNER)));
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSwitchUserAuditVariables() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		AbstractConceptRoleRequestDto concept = null;
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			concept = createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
			roleRequestService.startRequestInternal(request.getId(), true);
			request = roleRequestService.get(request.getId());
			assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			//
			// check original identity is filled in process variables
			WorkflowProcessInstanceDto workflowProcessInstanceDto = workflowProcessInstanceService.get(request.getWfProcessId());
			Assert.assertEquals(adminSwitched.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER));
			Assert.assertEquals(adminOriginal.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}		
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminHelpdesk);
			//
			// change concept - modify dates
			concept.setValidTill(LocalDate.now().minusDays(10));
			concept = (AbstractConceptRoleRequestDto) getConceptRoleService().save(concept);
			Assert.assertEquals(adminHelpdesk.getId(), concept.getModifierId());
			Assert.assertEquals(adminOriginal.getId(), concept.getOriginalModifierId());
			//
			// complete the first task (~helpdesk)
			WorkflowFilterDto taskFilter = new WorkflowFilterDto();
			taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
			List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			Assert.assertEquals(1, tasks.size());
			Assert.assertEquals(applicantId.toString(), tasks.get(0).getApplicant());
			checkAndCompleteOneTask(taskFilter, applicantId, "approve");
//			workflowTaskInstanceService.completeTask(tasks.get(0).getId(), "approve");
			//
			// check original identity is filled in task variables
			WorkflowHistoricTaskInstanceDto workflowHistoricTaskInstanceDto = workflowHistoricTaskInstanceService.get(tasks.get(0).getId());
			Assert.assertEquals(adminHelpdesk.getId().toString(), workflowHistoricTaskInstanceDto.getAssignee());
			Assert.assertEquals(adminOriginal.getId(), workflowHistoricTaskInstanceDto.getVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}
		//
		// request has to be approved
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		//
		// check created identity role audit fields
		List<AbstractRoleAssignmentDto> assignedRoles = findRoleAssignmentsForOwner(owner);
		//
		Assert.assertEquals(1, assignedRoles.size());
		AbstractRoleAssignmentDto assignedRole = assignedRoles.get(0);
		Assert.assertEquals(adminHelpdesk.getId(), assignedRole.getCreatorId());
		Assert.assertEquals(adminOriginal.getId(), assignedRole.getOriginalCreatorId());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSwitchUserAuditVariablesModify() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		AbstractConceptRoleRequestDto concept = null;
		createRoleAssignment(adminRole.getId(), owner.getId(), null, null);
		List<AbstractRoleAssignmentDto> assignedRoles = findRoleAssignmentsForOwner(owner);
		Assert.assertEquals(1, assignedRoles.size());
		AbstractRoleAssignmentDto assignedRole = assignedRoles.get(0);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			request = roleRequestService.save(request);
			concept = createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
			concept.setRoleAssignmentUuid(assignedRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.UPDATE);
			concept.setValidFrom(LocalDate.now().minusDays(20));
			concept = (AbstractConceptRoleRequestDto) getConceptRoleService().save(concept);
			roleRequestService.startRequestInternal(request.getId(), true);
			request = roleRequestService.get(request.getId());
			assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			//
			// check original identity is filled in process variables
			WorkflowProcessInstanceDto workflowProcessInstanceDto = workflowProcessInstanceService.get(request.getWfProcessId());
			Assert.assertEquals(adminSwitched.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER));
			Assert.assertEquals(adminOriginal.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}		
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminHelpdesk);
			//
			// change concept - modify dates
			concept.setValidFrom(LocalDate.now().minusDays(10));
			concept = (AbstractConceptRoleRequestDto) getConceptRoleService().save(concept);
			Assert.assertEquals(adminHelpdesk.getId(), concept.getModifierId());
			Assert.assertEquals(adminOriginal.getId(), concept.getOriginalModifierId());
			//
			// complete the first task (~helpdesk)
			WorkflowFilterDto taskFilter = new WorkflowFilterDto();
			taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
			List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			Assert.assertEquals(1, tasks.size());
			Assert.assertEquals(applicantId.toString(), tasks.get(0).getApplicant());
			checkAndCompleteOneTask(taskFilter, applicantId, "approve");
			getHelper().waitForResult(res -> {
				return workflowTaskInstanceService.find(taskFilter, null).getTotalElements() != 0;
			}, 1000, 30);
			//
			// check original identity is filled in task variables
			WorkflowHistoricTaskInstanceDto workflowHistoricTaskInstanceDto = workflowHistoricTaskInstanceService.get(tasks.get(0).getId());
			Assert.assertEquals(adminHelpdesk.getId().toString(), workflowHistoricTaskInstanceDto.getAssignee());
			Assert.assertEquals(adminOriginal.getId(), workflowHistoricTaskInstanceDto.getVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}
		//
		// request has to be approved
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		//
		// check created identity role audit fields
		assignedRoles = findRoleAssignmentsForOwner(owner);
		//
		Assert.assertEquals(1, assignedRoles.size());
		assignedRole = assignedRoles.get(0);
		Assert.assertEquals(adminHelpdesk.getId(), assignedRole.getModifierId());
		Assert.assertEquals(adminOriginal.getId(), assignedRole.getOriginalModifierId());
	}
	
	@Test
	public void testSwitchUserDeleteProcess() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(applicantId);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			request = roleRequestService.save(request);
			createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);
			roleRequestService.startRequestInternal(request.getId(), true);
			request = roleRequestService.get(request.getId());
			assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			//
			// check original identity is filled in process variables
			WorkflowProcessInstanceDto workflowProcessInstanceDto = workflowProcessInstanceService.get(request.getWfProcessId());
			Assert.assertEquals(adminSwitched.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER));
			Assert.assertEquals(adminOriginal.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}		
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminHelpdesk);
			//
			// delete process
			request = roleRequestService.get(request.getId());
			Assert.assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			workflowProcessInstanceService.delete(request.getWfProcessId(), null);
			WorkflowHistoricProcessInstanceDto workflowHistoricProcessInstanceDto = workflowHistoricProcessInstanceService.get(request.getWfProcessId());
			Assert.assertTrue(workflowHistoricProcessInstanceDto.getDeleteReason().contains(adminOriginal.getUsername()));
			Assert.assertTrue(workflowHistoricProcessInstanceDto.getDeleteReason().contains(adminHelpdesk.getUsername()));
		} finally {
			logout();
		}
	}

	@Test
	public void testGetTaskByUserInvolvedInProcess() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_SECURITY_KEY);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);

		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// Help Desk
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		WorkflowTaskInstanceDto notApprovedTask = tasks.get(0);

		// Approver of this task has permission to read this task.
		loginWithout(InitTestDataProcessor.TEST_USER_2, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(UUID.fromString(notApprovedTask.getId()));
		assertNotNull(task);
		// This task isn't resolved yet.
		assertFalse(task instanceof WorkflowHistoricTaskInstanceDto);

		// Login as applicant.
		loginWithout(applicant.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		// Applicant can read a process -> but cannot read all tasks of a process.
		task = workflowTaskInstanceService.get(UUID.fromString(notApprovedTask.getId()));
		assertNull(task);

		// Login as random user.
		IdmIdentityDto randomUser = getHelper().createIdentity();
		loginWithout(randomUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		// Random user doesn't have read permission on the task and process.
		task = workflowTaskInstanceService.get(UUID.fromString(notApprovedTask.getId()));
		assertNull(task);

		// Random user can read the task if filter has set OnlyInvolved to FALSE.
		WorkflowFilterDto taskFilterApproved = new WorkflowFilterDto();
		taskFilterApproved.setOnlyInvolved(Boolean.FALSE);
		task = workflowTaskInstanceService.get(UUID.fromString(notApprovedTask.getId()), taskFilterApproved);
		assertNotNull(task);
		// This task isn't resolved yet.
		assertFalse(task instanceof WorkflowHistoricTaskInstanceDto);
	}

	@Test
	public void testGetHistoricTaskByUserInvolvedInProcess() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		AbstractDto owner = createOwner(TEST_APPLICANT_PWD);
		UUID applicantId = getApplicant(owner);
		IdmIdentityDto applicant = identityService.get(applicantId);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(applicant), test2);
		// FIXME the line above should not be necessary but the test does not pass without it
		
		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_SECURITY_KEY);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);

		IdmRoleRequestDto request = createRoleRequest(applicantId);
		request = roleRequestService.save(request);

		createConceptRoleRequest(request, adminRole, owner.getId(), null, ConceptRoleRequestOperation.ADD, null, null);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// Help Desk
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, applicantId, "approve");
		// Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		WorkflowTaskInstanceDto approvedTask = checkAndCompleteOneTask(taskFilter, applicantId, "approve");

		// Approver of this task has permission to read this task.
		loginWithout(InitTestDataProcessor.TEST_USER_2, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(UUID.fromString(approvedTask.getId()));
		assertNotNull(task);
		assertTrue(task instanceof WorkflowHistoricTaskInstanceDto);

		// Login as applicant.
		loginWithout(applicant.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		// Applicant can read a process -> but cannot read all tasks of a process.
		task = workflowTaskInstanceService.get(UUID.fromString(approvedTask.getId()));
		assertNull(task);
		assertTrue(workflowProcessInstanceService.canReadProcessOrHistoricProcess(approvedTask.getProcessInstanceId()));

		// Login as random user.
		IdmIdentityDto randomUser = getHelper().createIdentity();
		loginWithout(randomUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		// Random user doesn't have read permission on the task and process.
		task = workflowTaskInstanceService.get(UUID.fromString(approvedTask.getId()));
		assertNull(task);
		assertFalse(workflowProcessInstanceService.canReadProcessOrHistoricProcess(approvedTask.getProcessInstanceId()));

		// Random user can read the task if filter has set OnlyInvolved to FALSE.
		WorkflowFilterDto taskFilterApproved = new WorkflowFilterDto();
		taskFilterApproved.setOnlyInvolved(Boolean.FALSE);
		task = workflowTaskInstanceService.get(UUID.fromString(approvedTask.getId()), taskFilterApproved);
		assertNotNull(task);
		assertTrue(task instanceof WorkflowHistoricTaskInstanceDto);
	}

	/**
	 * Return {@link WorkflowHistoricProcessInstanceDto} for current logged user
	 *
	 * @return
	 */
	public List<WorkflowHistoricProcessInstanceDto> getHistoricProcess(ZonedDateTime from) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(from);
		return workflowHistoricProcessInstanceService.find(taskFilter, null).getContent();
	}
	
	/**
	 * Return count of historic processes for current logged user
	 *
	 * @return
	 */
	public long getHistoricProcessCount(ZonedDateTime from) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(from);
		return workflowHistoricProcessInstanceService.count(taskFilter);
	}

	public WorkflowTaskInstanceDto checkAndCompleteOneTask(WorkflowFilterDto taskFilter, UUID applicantId, String decision) {
		return this.checkAndCompleteOneTask(taskFilter, applicantId, decision, null);
	}

	public WorkflowTaskInstanceDto checkAndCompleteOneTask(WorkflowFilterDto taskFilter, UUID applicantId, String decision, String userTaskId) {
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		if (userTaskId != null) {
			assertEquals(userTaskId,  tasks.get(0).getDefinition().getId());
		}
		assertEquals(applicantId.toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
		return tasks.get(0);
	}

	/**
	 * Complete all tasks from user given in parameters. Complete will be done by
	 * currently logged user.
	 *
	 * @param approverUser
	 * @param decision
	 */
	public void completeTasksFromUsers(String approverUser, String decision) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(approverUser);
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		//
		Assert.assertFalse("No tasks to complete.",tasks.isEmpty());
		//
		for (WorkflowTaskInstanceDto task : tasks) {
			workflowTaskInstanceService.completeTask(task.getId(), decision);
		}
	}
	
	public abstract AbstractConceptRoleRequestDto createConceptRoleRequest(
			IdmRoleRequestDto request,
			IdmRoleDto role,
			UUID assigneeId,
			UUID roleAssignmentId,
			ConceptRoleRequestOperation operationType,
			LocalDate validFrom,
			List<IdmFormInstanceDto> eavs);
	
	public abstract IdmGeneralConceptRoleRequestService getConceptRoleService();
	
	public abstract IdmRoleAssignmentService getRoleAssignmentService();
	
	public abstract AbstractDto createOwner(final GuardedString password);
	
	public abstract UUID getApplicant(AbstractDto owner);
	
	public abstract List<AbstractRoleAssignmentDto> findRoleAssignmentsForOwner(AbstractDto owner);
	
	public abstract AbstractRoleAssignmentDto createRoleAssignment(UUID roleId, UUID ownerId, LocalDate validFrom, LocalDate validTill);
	
	public IdmRoleRequestDto createRoleRequest(UUID applicantId) {
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(applicantId);
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(false);
		roleRequest.setCreatorId(applicantId);
		return roleRequestService.save(roleRequest);
	}
}

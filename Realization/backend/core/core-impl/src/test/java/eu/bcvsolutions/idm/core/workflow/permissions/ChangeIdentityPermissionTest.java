package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;

/**
 * Test change permissions for identity.
 *
 * @author svandav
 * @author Tomáš Doischer
 *
 */
@Transactional
public class ChangeIdentityPermissionTest extends AbstractChangeIdentityPermissionTest {

	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	
	
	
	@Override
	@Transactional
	public void testGetHistoricTaskByUserInvolvedInProcess() {
		super.testGetHistoricTaskByUserInvolvedInProcess();
	}

	@Override
	@Transactional
	public void testSwitchUserAuditVariables() {
		super.testSwitchUserAuditVariables();
	}

	@Override
	@Transactional
	public void testSwitchUserAuditVariablesModify() {
		super.testSwitchUserAuditVariablesModify();
	}
	
	

	@Override
	@Transactional
	public void testTaskCount() {
		super.testTaskCount();
	}

	@Override
	@Transactional
	public void testCompleteTaskByStarter() {
		super.testCompleteTaskByStarter();
	}

	@Override
	@Transactional
	public void testCompleteTaskByAnotherUser() {
		super.testCompleteTaskByAnotherUser();
	}

	@Override
	@Transactional
	public void testCompleteTaskByPreviosApprover() {
		super.testCompleteTaskByPreviosApprover();
	}

	@Override
	@Transactional
	public void testGetTaskByAnotherUser() {
		super.testGetTaskByAnotherUser();
	}

	@Override
	@Transactional
	public void testFindCandidatesWithoutSubprocess() {
		super.testFindCandidatesWithoutSubprocess();
	}

	@Override
	@Transactional
	public void testFindCandidatesWithSubprocess() {
		super.testFindCandidatesWithSubprocess();
	}

	@Override
	@Transactional
	public void testAccessIsAddedForOwnerAndImplementerToSubprocesses() {
		super.testAccessIsAddedForOwnerAndImplementerToSubprocesses();
	}

	@Override
	@Transactional
	public void testSwitchUserDeleteProcess() {
		super.testSwitchUserDeleteProcess();
	}

	@Override
	@Transactional
	public void testGetTaskByUserInvolvedInProcess() {
		super.testGetTaskByUserInvolvedInProcess();
	}

	@Test
	public void addSuperAdminRoleTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1);

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleSkipTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

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
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// SECURITY - must be skipped
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleDisapproveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

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
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "disapprove");
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.DISAPPROVED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(null, concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

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
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		// SECURITY
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleApproveBySecurityTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);
		
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		// Set security role test
		configurationService.setValue(APPROVE_BY_SECURITY_ROLE, SECURITY_ROLE_TEST);
		// Create test role for load candidates on security department (TEST_USER_1)
		IdmRoleDto role = getHelper().createRole(SECURITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

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
		// SECURITY
		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessSecurityTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_SECURITY_KEY);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

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
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// User Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Role Guarantee - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Security - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Security
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessRemoveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		IdmIdentityDto test1 = getHelper().createIdentity("TestUser" + System.currentTimeMillis());
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());
		IdmRoleDto adminRole = getHelper().createRole();
		adminRole.setApproveRemove(true);
		roleService.save(adminRole);

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> page = identityRoleService.find(filter, null);
		assertEquals(1, page.getContent().size());

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		// Guarantee
		int priority = 500;
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);

		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + (priority + priority),
				APPROVE_REMOVE_ROLE_BY_MANAGER_KEY);
		IdmRoleRequestDto requestRemove = createRoleRequest(test1);
		requestRemove = roleRequestService.save(requestRemove);

		// Mock set of validity - only for test if created remove concept doesn't have filled validity
		contract.setValidFrom(LocalDate.now());
		contract.setValidTill(LocalDate.now());
		IdmConceptRoleRequestDto conceptRemove = createRoleRemoveConcept(page.getContent().get(0).getId(), adminRole,
				contract, requestRemove);
		conceptRemove = conceptRoleRequestService.save(conceptRemove);

		// Remove concept should have not set a validity.
		assertNull(conceptRemove.getValidFrom());
		assertNull(conceptRemove.getValidTill());

		roleRequestService.startRequestInternal(requestRemove.getId(), true);
		requestRemove = roleRequestService.get(requestRemove.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, requestRemove.getState());

		WorkflowFilterDto taskRemoveFilter = new WorkflowFilterDto();
		taskRemoveFilter.setCreatedAfter(now);

		// HELPDESK
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// USER MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// Subprocess - approve by GUARANTEE
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// SECURITY
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");

		requestRemove = roleRequestService.get(requestRemove.getId());
		assertEquals(RoleRequestState.EXECUTED, requestRemove.getState());
		assertNotNull(requestRemove.getWfProcessId());
		conceptRemove = conceptRoleRequestService.get(conceptRemove.getId());
		assertNotNull(conceptRemove.getWfProcessId());

		IdmIdentityRoleFilter filterRemove = new IdmIdentityRoleFilter();
		filterRemove.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> pageRemove = identityRoleService.find(filterRemove, null);
		assertEquals(0, pageRemove.getContent().size());
	}
	
	private IdmConceptRoleRequestDto createRoleConcept(IdmRoleDto adminRole, IdmIdentityContractDto contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	private IdmConceptRoleRequestDto createRoleRemoveConcept(UUID identityRole, IdmRoleDto adminRole,
			IdmIdentityContractDto contract, IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.REMOVE);
		concept.setRole(adminRole.getId());
		concept.setIdentityRole(identityRole);
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	private IdmRoleRequestDto createRoleRequest(IdmIdentityDto test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(test1.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	private WorkflowTaskInstanceDto checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision) {
		return this.checkAndCompleteOneTask(taskFilter, user, decision, null);
	}

	private WorkflowTaskInstanceDto checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision, String userTaskId) {
		IdmIdentityDto identity = identityService.getByUsername(user);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		if (userTaskId != null) {
			assertEquals(userTaskId,  tasks.get(0).getDefinition().getId());
		}
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);

		return tasks.get(0);
	}

	@Override
	public AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role,
			UUID assigneeId, UUID roleAssignmentId, ConceptRoleRequestOperation operationType, LocalDate validFrom,
			List<IdmFormInstanceDto> eavs) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setIdentityContract(assigneeId);
		concept.setIdentityRole(roleAssignmentId);
		concept.setRole(role.getId());
		concept.setOperation(operationType);
		concept.setRoleRequest(request.getId());
		concept.setValidFrom(validFrom);
		if (eavs != null && !eavs.isEmpty()) {
			concept.setEavs(eavs);
		}
		return conceptRoleRequestService.save(concept);
	}

	@Override
	public IdmGeneralConceptRoleRequestService getConceptRoleService() {
		return conceptRoleRequestService;
	}

	@Override
	public IdmRoleAssignmentService getRoleAssignmentService() {
		return identityRoleService;
	}

	@Override
	public AbstractDto createOwner() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		return contract;
	}

	@Override
	public UUID getApplicant(AbstractDto owner) {
		if (owner instanceof IdmIdentityContractDto) {
			return ((IdmIdentityContractDto) owner).getIdentity();
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractRoleAssignmentDto> findRoleAssignmentsForOwner(AbstractDto owner) {
		if (owner instanceof IdmIdentityContractDto) {
			IdmIdentityRoleFilter irFilter = new IdmIdentityRoleFilter();
			irFilter.setIdentityContractId(owner.getId());
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(irFilter, null).getContent();
			return (List<AbstractRoleAssignmentDto>) (List<?>) identityRoles;
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@Override
	public AbstractRoleAssignmentDto createRoleAssignment(UUID roleId, UUID ownerId, LocalDate validFrom,
			LocalDate validTill) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(ownerId);
		identityRole.setRole(roleId);
		identityRole.setValidFrom(validFrom);
		identityRole.setValidTill(validTill);
		return identityRoleService.save(identityRole);
	}
}

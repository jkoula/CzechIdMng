package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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

	@Test
	public void cancelWfOnRoleRequestDeleteTest() {
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
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());
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
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

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
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		// Delete the contract that is using in the concept
		UUID contractId = concept.getIdentityContract();
		assertNotNull(contractId);
		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		identityContractService.deleteById(contractId);

		// Concept has to be in the Cancel state and WF must be ended
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(RoleRequestState.CANCELED, concept.getState());
		assertNotNull(concept.getWfProcessId());
		assertNull(workflowProcessInstanceService.get(conceptWf));
		request = roleRequestService.get(request.getId());
		// Main process has to be executed
		assertEquals(RoleRequestState.EXECUTED, request.getState());
	}

	@Test
	public void cancelSubprocessOnRoleDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
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
				APPROVE_ROLE_BY_MANAGER_KEY);

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
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		// Delete the role that is using in the concept
		UUID roleId = concept.getRole();
		assertNotNull(roleId);

		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setRoleId(roleId);
		identityRoleService.find(identityRoleFilter, null).getContent().forEach(identityRole -> identityRoleService.delete(identityRole));

		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		roleService.deleteById(roleId);

		// Concept has to be in the Cancel state and WF must be ended
		concept = conceptRoleRequestService.get(concept.getId());
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
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());

		loginAsNoAdmin(identity.getUsername());
		IdmRoleRequestDto request = createRoleRequest(identity);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(identity.getId());
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
		requestIdentityRoleFilter.setIdentity(identity.getId());
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

		IdmIdentityDto identity = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto guarantee = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, guarantee);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());

		IdmRoleRequestDto request = createRoleRequest(identity);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(identity.getId());
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
		requestIdentityRoleFilter.setIdentity(identity.getId());
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
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		filter.setIncludeApprovers(false);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertNull(requestDto.getApprovers());

		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(guarantee.getUsername());
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

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
		requestIdentityRoleFilter.setIdentity(identity.getId());
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
		IdmIdentityDto applicant = getHelper().createIdentity();
		IdmIdentityContractDto applicantContract = getHelper().getPrimeContract(applicant);
		IdmIdentityDto guaranteeOne = getHelper().createIdentity();
		IdmIdentityDto guaranteeTwo = getHelper().createIdentity();
		//
		getHelper().createRoleGuarantee(roleOne, guaranteeOne);
		getHelper().createRoleGuarantee(roleTwo, guaranteeTwo);
		//
		loginAsAdmin(implementer.getUsername()); // login as implementer
		//
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);
		IdmConceptRoleRequestDto concept = createRoleConcept(roleOne, applicantContract, request);
		conceptRoleRequestService.save(concept);
		concept = createRoleConcept(roleTwo, applicantContract, request);
		conceptRoleRequestService.save(concept);
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentity(applicant.getId());
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
		getHelper().login(applicant);
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
	
	@Test
	public void testSwitchUserAuditVariables() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
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
		IdmRoleRequestDto request = createRoleRequest(identity);
		IdmConceptRoleRequestDto concept = null;
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			request = roleRequestService.save(request);
			concept = createRoleConcept(adminRole, contract, request);
			concept = conceptRoleRequestService.save(concept);
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
			concept = conceptRoleRequestService.save(concept);
			Assert.assertEquals(adminHelpdesk.getId(), concept.getModifierId());
			Assert.assertEquals(adminOriginal.getId(), concept.getOriginalModifierId());
			//
			// complete the first task (~helpdesk)
			WorkflowFilterDto taskFilter = new WorkflowFilterDto();
			taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
			List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			Assert.assertEquals(1, tasks.size());
			Assert.assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());
			workflowTaskInstanceService.completeTask(tasks.get(0).getId(), "approve");
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
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		//
		Assert.assertEquals(1, assignedRoles.size());
		IdmIdentityRoleDto assignedRole = assignedRoles.get(0);
		Assert.assertEquals(adminHelpdesk.getId(), assignedRole.getCreatorId());
		Assert.assertEquals(adminOriginal.getId(), assignedRole.getOriginalCreatorId());
	}
	
	@Test
	public void testSwitchUserAuditVariablesModify() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
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
		IdmRoleRequestDto request = createRoleRequest(identity);
		IdmConceptRoleRequestDto concept = null;
		getHelper().createIdentityRole(identity, adminRole);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		IdmIdentityRoleDto assignedRole = assignedRoles.get(0);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			request = roleRequestService.save(request);
			concept = createRoleConcept(adminRole, contract, request);
			concept.setIdentityRole(assignedRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.UPDATE);
			concept.setValidFrom(LocalDate.now().minusDays(20));
			concept = conceptRoleRequestService.save(concept);
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
			concept = conceptRoleRequestService.save(concept);
			Assert.assertEquals(adminHelpdesk.getId(), concept.getModifierId());
			Assert.assertEquals(adminOriginal.getId(), concept.getOriginalModifierId());
			//
			// complete the first task (~helpdesk)
			WorkflowFilterDto taskFilter = new WorkflowFilterDto();
			taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
			List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			Assert.assertEquals(1, tasks.size());
			Assert.assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());
			workflowTaskInstanceService.completeTask(tasks.get(0).getId(), "approve");
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
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
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
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(identity);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			request = roleRequestService.save(request);
			IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
			concept = conceptRoleRequestService.save(concept);
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
		loginWithout(test1.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
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
		WorkflowTaskInstanceDto approvedTask = checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		// Approver of this task has permission to read this task.
		loginWithout(InitTestDataProcessor.TEST_USER_2, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(UUID.fromString(approvedTask.getId()));
		assertNotNull(task);
		assertTrue(task instanceof WorkflowHistoricTaskInstanceDto);

		// Login as applicant.
		loginWithout(test1.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
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
	private List<WorkflowHistoricProcessInstanceDto> getHistoricProcess(ZonedDateTime from) {
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
	private long getHistoricProcessCount(ZonedDateTime from) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(from);
		return workflowHistoricProcessInstanceService.count(taskFilter);
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
	protected void deleteOwner(AbstractDto owner) {
		identityContractService.deleteById(owner.getId());
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
	public AbstractDto createOwner(GuardedString password) {
		IdmIdentityDto identity = getHelper().createIdentity(password);
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

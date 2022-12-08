package eu.bcvsolutions.idm.core.model.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.InvalidFormException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests with request-identity-role service.
 * 
 * @author Vít Švanda
 *
 */
public class IdmRequestIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRequestIdentityRoleService requestIdentityRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;

	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	private final static String IP = "IP";
	private final static String NUMBER_OF_FINGERS = "NUMBER_OF_FINGERS";

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void testAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		// cast here is probably ok
		IdmConceptRoleRequestDto concept = (IdmConceptRoleRequestDto) request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(role.getId(), identityRoles.get(0).getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testAssignRoles() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		IdmRoleDto roleThree = this.getHelper().createRole();
		Set<UUID> roles = Sets.newSet(role.getId(), roleTwo.getId(), roleThree.getId());

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());
		dto.setRoles(Sets.newSet(roleTwo.getId(), roleThree.getId()));
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(3, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		request.getConceptRoles().forEach(concept -> {
			Assert.assertEquals(contract.getId(), concept.getOwnerUuid());
			Assert.assertTrue(roles.contains(concept.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		});

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(3, identityRoles.size());
		identityRoles.forEach(identityRole -> {
			Assert.assertTrue(roles.contains(identityRole.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRole.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRole.getValidTill());
		});
	}

	@Test
	@Transactional
	public void testAssignRemove() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for remove identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRoleAssignmentUuid(identityRoles.get(0).getId());
		dto.setId(dto.getRoleAssignmentUuid());

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService
				.deleteRequestIdentityRole(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		AbstractConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getOwnerUuid());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.REMOVE, concept.getOperation());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(0, identityRoles.size());
	}

	@Test
	@Transactional
	public void testUpdateAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role, LocalDate.now().minusDays(1), null);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for updated identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRoleAssignmentUuid(identityRoles.get(0).getId());
		dto.setId(dto.getRoleAssignmentUuid());
		dto.setValidFrom(LocalDate.now().minusDays(10));
		dto.setValidTill(LocalDate.now().plusDays(10));
		dto.setState(RoleRequestState.IN_PROGRESS);

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		AbstractConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getOwnerUuid());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.UPDATE, concept.getOperation());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testUpdateAddingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		// We want to update created concept -> update validity
		createdRequestIdentityRole.setValidFrom(LocalDate.now().minusDays(1));
		createdRequestIdentityRole.setValidTill(LocalDate.now().plusDays(10));
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		AbstractConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getOwnerUuid());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		Assert.assertEquals(ConceptRoleRequestOperation.ADD, concept.getOperation());

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(role.getId(), identityRoles.get(0).getRole());
		Assert.assertEquals(LocalDate.now().minusDays(1), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(LocalDate.now().plusDays(10), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testUpdateUpdatingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role, LocalDate.now().minusDays(1), null);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for updated identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRoleAssignmentUuid(identityRoles.get(0).getId());
		dto.setId(dto.getRoleAssignmentUuid());
		dto.setValidFrom(LocalDate.now().plusDays(5));
		dto.setValidTill(LocalDate.now().minusDays(10));
		dto.setState(RoleRequestState.IN_PROGRESS);

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		Assert.assertEquals(LocalDate.now().plusDays(5), createdRequestIdentityRole.getValidFrom());
		Assert.assertEquals(LocalDate.now().minusDays(10), createdRequestIdentityRole.getValidTill());

		// We want to update created concept -> update validity
		createdRequestIdentityRole.setValidFrom(LocalDate.now().minusDays(1));
		createdRequestIdentityRole.setValidTill(LocalDate.now().plusDays(10));
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		AbstractConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getOwnerUuid());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.UPDATE, concept.getOperation());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(LocalDate.now().minusDays(1), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(LocalDate.now().plusDays(10), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testRemoveAddingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getOwnerUuid());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant().getId());

		// Delete adding concept
		requestIdentityRoleService.deleteRequestIdentityRole(createdRequestIdentityRole);

		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are empty now
		Assert.assertEquals(0, request.getConceptRoles().size());
	}

	@Test
	@Transactional
	public void testFind() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto assignedRole = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, assignedRole);
		IdmRoleDto role = this.getHelper().createRole();

		IdmRequestIdentityRoleFilter filter = new IdmRequestIdentityRoleFilter();
		filter.setIdentity(identity.getId());
		final List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());

		// We expecting only one already assigned identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(filter, null)
				.getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(identityRole.getId(), requestIdentityRoles.get(0).getId());

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));
		dto.setOperation(ConceptRoleRequestOperation.ADD);

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		// Filter will be filtering by this request
		filter.setRoleRequestId(createdRequestIdentityRole.getRoleRequest());


		IdmIdentityRoleFilter irf = new IdmIdentityRoleFilter();

		irf.setIdentityId(identity.getId());
		final Page<IdmIdentityRoleDto> idmIdentityRoleDtos = identityRoleService.find(irf,null);

		// We expecting two items, one assigned identity-role and one adding concept
		IdmConceptRoleRequestFilter crf = new IdmConceptRoleRequestFilter();
		crf.setIdentity(identity.getId());
		crf.setRoleRequestId(createdRequestIdentityRole.getRoleRequest());
		crf.setIdentityRoleIsNull(true);
		final Page<IdmConceptRoleRequestDto> idmConceptRoleRequestDtos = conceptRoleRequestService.find(crf, null);
		requestIdentityRoles = requestIdentityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());

		IdmRequestIdentityRoleDto addingConcept = requestIdentityRoles.stream()
				.filter(requestIdentityRole -> ConceptRoleRequestOperation.ADD == requestIdentityRole.getOperation())
				.findFirst().orElse(null);
		Assert.assertNotNull(addingConcept);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), addingConcept.getRoleRequest());
		Assert.assertEquals(role.getId(), addingConcept.getRole());
		Assert.assertEquals(dto.getValidFrom(), addingConcept.getValidFrom());
		Assert.assertEquals(dto.getValidTill(), addingConcept.getValidTill());

		// Create request for remove identity-role
		IdmRequestIdentityRoleDto dtoForRemove = new IdmRequestIdentityRoleDto();
		dtoForRemove.setRoleRequest(createdRequestIdentityRole.getRoleRequest());
		dtoForRemove.setRoleAssignmentUuid(identityRole.getId());
		dtoForRemove.setId(identityRole.getId());

		// Remove existing identity-role -> new removing concept
		IdmRequestIdentityRoleDto deleteRequestIdentityRole = requestIdentityRoleService
				.deleteRequestIdentityRole(dtoForRemove);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), deleteRequestIdentityRole.getRoleRequest());

		// We expecting two items, one adding concept and one removing concept
		requestIdentityRoles = requestIdentityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());

		IdmRequestIdentityRoleDto removingConcept = requestIdentityRoles.stream()
				.filter(requestIdentityRole -> ConceptRoleRequestOperation.REMOVE == requestIdentityRole.getOperation())
				.findFirst().orElse(null);
		Assert.assertNotNull(removingConcept);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), removingConcept.getRoleRequest());
		Assert.assertEquals(identityRole.getId(), removingConcept.getRoleAssignmentUuid());
		Assert.assertNotEquals(removingConcept.getId(), removingConcept.getRoleAssignmentUuid());
	}
	
	@Test
	@Transactional
	public void testFindByRoleText() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto assignedRole = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, assignedRole);
		IdmRoleDto role = this.getHelper().createRole();

		IdmRequestIdentityRoleFilter filter = new IdmRequestIdentityRoleFilter();
		filter.setIdentity(identity.getId());
		filter.setRoleText(assignedRole.getCode());

		// We expecting only one already assigned identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(filter, null)
				.getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(identityRole.getId(), requestIdentityRoles.get(0).getId());

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setOwnerUuid(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		// Filter will be filtering by this request
		filter.setRoleRequestId(createdRequestIdentityRole.getRoleRequest());

		// We expecting one item
		requestIdentityRoles = requestIdentityRoleService.find(filter, null).getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
	}

	@Test
	public void testFindAddingConceptWithEAVs() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes(false);
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());

		// Create request identity-role
		IdmRequestIdentityRoleDto createdRequestIdentityRole = new IdmRequestIdentityRoleDto();
		createdRequestIdentityRole.setOwnerUuid(contract.getId());
		// Change the valid from
		createdRequestIdentityRole.setValidFrom(LocalDate.now());
		createdRequestIdentityRole.setRole(role.getId());

		// Create role attribute value in concept

		IdmFormDefinitionDto formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		IdmFormAttributeDto attribute = formDefinitionDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(BigDecimal.TEN);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		createdRequestIdentityRole.setEavs(forms);
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);

		IdmRequestIdentityRoleFilter filterRequestIdentityRole = new IdmRequestIdentityRoleFilter();
		filterRequestIdentityRole.setIdentity(identity.getId());
		filterRequestIdentityRole.setRoleRequestId(request.getId());
		// Include EAV attributes
		filterRequestIdentityRole.setIncludeEav(true);

		// Check EAV value in the request-identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService
				.find(filterRequestIdentityRole, null).getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(role.getId(), requestIdentityRoles.get(0).getRole());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().size());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().get(0).getValues().size());
		Serializable value = requestIdentityRoles.get(0).getEavs().get(0).getValues().get(0).getValue();
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal)value).longValue());
	}
	
	@Test
	public void testUniqueValidation() {
		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes(true);
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());

		// Create request identity-role
		IdmRequestIdentityRoleDto createdRequestIdentityRole = new IdmRequestIdentityRoleDto();
		createdRequestIdentityRole.setOwnerUuid(contract.getId());
		// Change the valid from
		createdRequestIdentityRole.setValidFrom(LocalDate.now());
		createdRequestIdentityRole.setRole(role.getId());

		// Create role attribute value in concept
		IdmFormDefinitionDto formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		IdmFormAttributeDto attribute = formDefinitionDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(5);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		createdRequestIdentityRole.setEavs(forms);
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Execute a role-request.
		getHelper().executeRequest(request, false, true);

		IdmRequestIdentityRoleFilter filterRequestIdentityRole = new IdmRequestIdentityRoleFilter();
		filterRequestIdentityRole.setIdentity(identity.getId());
		filterRequestIdentityRole.setRoleRequestId(request.getId());
		// Include EAV attributes
		filterRequestIdentityRole.setIncludeEav(true);

		// Check EAV value in the request-identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService
				.find(filterRequestIdentityRole, null).getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(role.getId(), requestIdentityRoles.get(0).getRole());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().size());
		IdmFormInstanceDto formInstance = requestIdentityRoles.get(0).getEavs().get(0);
		Assert.assertEquals(1, formInstance.getValues().size());
		IdmFormValueDto formValue = formInstance.getValues().get(0);
		Serializable value = formValue.getValue();
		Assert.assertEquals(((BigDecimal)formValueDto.getValue()).longValue(), ((BigDecimal)value).longValue());
		IdmFormAttributeDto mappedAttribute = formInstance.getMappedAttribute(formValue.getFormAttribute());
		Assert.assertNotNull(mappedAttribute);
		Assert.assertEquals(0, formInstance.getValidationErrors().size());

		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());

		// Create request for change an identity-role
		IdmRequestIdentityRoleDto changeRequestIdentityRole = new IdmRequestIdentityRoleDto();
		changeRequestIdentityRole.setId(identityRoles.get(0).getId());
		changeRequestIdentityRole.setOwnerUuid(contract.getId());
		// Change the valid from
		changeRequestIdentityRole.setValidFrom(LocalDate.now());
		changeRequestIdentityRole.setRoleAssignmentUuid(identityRoles.get(0).getId());
		changeRequestIdentityRole.setEavs(forms);
		changeRequestIdentityRole = requestIdentityRoleService.save(changeRequestIdentityRole);

		IdmRoleRequestDto requestChange = roleRequestService.get(changeRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(requestChange);
		// Execute a role-request.
		requestChange = getHelper().executeRequest(requestChange, false, true);
		assertEquals(RoleRequestState.EXECUTED, requestChange.getState());
	}
	
	@Test(expected = InvalidFormException.class)
	public void testUniqueConceptValidation() {
		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes(true);
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());

		// Create request identity-role
		IdmRequestIdentityRoleDto createdRequestIdentityRole = new IdmRequestIdentityRoleDto();
		createdRequestIdentityRole.setOwnerUuid(contract.getId());
		// Change the valid from
		createdRequestIdentityRole.setValidFrom(LocalDate.now());
		createdRequestIdentityRole.setRole(role.getId());

		// Create role attribute value in concept
		IdmFormDefinitionDto formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		IdmFormAttributeDto attribute = formDefinitionDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(5);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		createdRequestIdentityRole.setEavs(forms);
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Execute a role-request.
		getHelper().executeRequest(request, false, true);

		IdmRequestIdentityRoleFilter filterRequestIdentityRole = new IdmRequestIdentityRoleFilter();
		filterRequestIdentityRole.setIdentity(identity.getId());
		filterRequestIdentityRole.setRoleRequestId(request.getId());
		// Include EAV attributes
		filterRequestIdentityRole.setIncludeEav(true);

		// Check EAV value in the request-identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService
				.find(filterRequestIdentityRole, null).getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(role.getId(), requestIdentityRoles.get(0).getRole());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().size());
		IdmFormInstanceDto formInstance = requestIdentityRoles.get(0).getEavs().get(0);
		Assert.assertEquals(1, formInstance.getValues().size());
		IdmFormValueDto formValue = formInstance.getValues().get(0);
		Serializable value = formValue.getValue();
		Assert.assertEquals(((BigDecimal)formValueDto.getValue()).longValue(), ((BigDecimal)value).longValue());
		IdmFormAttributeDto mappedAttribute = formInstance.getMappedAttribute(formValue.getFormAttribute());
		Assert.assertNotNull(mappedAttribute);
		Assert.assertEquals(0, formInstance.getValidationErrors().size());

		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		
		// Create request identity-role
		createdRequestIdentityRole = new IdmRequestIdentityRoleDto();
		createdRequestIdentityRole.setOwnerUuid(contract.getId());
		// Change the valid from
		createdRequestIdentityRole.setValidFrom(LocalDate.now());
		createdRequestIdentityRole.setRole(role.getId());

		// Create role attribute value in concept
		formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
		formInstanceDto = new IdmFormInstanceDto();

		attribute = formDefinitionDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(5);
		values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		forms = Lists.newArrayList(formInstanceDto);
		createdRequestIdentityRole.setEavs(forms);
		requestIdentityRoleService.save(createdRequestIdentityRole);
	}

	private IdmRoleDto createRoleWithAttributes(boolean unique) {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());

		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(true);
		ipAttribute.setDefaultValue(getHelper().createName());

		IdmFormAttributeDto numberOfFingersAttribute = new IdmFormAttributeDto(NUMBER_OF_FINGERS);
		numberOfFingersAttribute.setPersistentType(PersistentType.DOUBLE);
		numberOfFingersAttribute.setRequired(false);
		numberOfFingersAttribute.setUnique(unique);
		numberOfFingersAttribute.setMax(BigDecimal.TEN);

		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.of(ipAttribute, numberOfFingersAttribute));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		return role;
	}
}

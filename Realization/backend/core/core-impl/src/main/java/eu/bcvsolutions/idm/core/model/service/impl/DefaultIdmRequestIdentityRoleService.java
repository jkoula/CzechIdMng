package eu.bcvsolutions.idm.core.model.service.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestManager;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.service.util.MultiSourcePagedResource;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.ADD;
import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.UPDATE;

/**
 * Default implementation of service for search and processing changes in assigned identity roles
 *
 * @author Vít Švanda
 */
@Service("requestIdentityRoleService")
public class DefaultIdmRequestIdentityRoleService extends
			AbstractBaseDtoService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter>
		implements IdmRequestIdentityRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmRequestIdentityRoleService.class);

	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired(required = false)
	@SuppressWarnings(value = "rawtypes")
	private IdmRoleSystemService roleSystemService;

	@Autowired
	IdmRoleAssignmentManager roleAssignmentManager;
	@Autowired
	IdmConceptRoleRequestManager conceptRoleRequestManager;

	/**
	 * This method returns both {@link eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto} and {@link AbstractRoleAssignmentDto} paged and filtered
	 * using given {@link IdmRequestIdentityRoleFilter} and {@link Pageable}.
	 *
	 * @param filter
	 * @param pageable
	 * @param permission base permissions to evaluate  (AND / OR by {@link eu.bcvsolutions.idm.core.api.dto.filter.PermissionContext})
	 * @return
	 */
	@Override
	public Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable, BasePermission... permission) {
		LOG.debug(MessageFormat.format("Find idm-request-identity-roles by filter [{0}] ", filter));
		Assert.notNull(filter, "Filter is required.");
		
		if (pageable == null) {
			// Page is null, so we set page to max value
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		
		// If is true, then we want to return only concepts (not assigned roles)
		boolean returnOnlyChanges = filter.isOnlyChanges();
		
		List<IdmRequestIdentityRoleDto> results = new ArrayList<>();
		
		long total = 0;

		if (filter.getRoleRequestId() != null) {
			if (!returnOnlyChanges) {
				// We want to load only new added roles
				filter.setOperation(ADD);
				// We don`t want load ADD concepts with filled identityRoleId (such concepts were already executed )
				filter.setIdentityRoleIsNull(true);
			}
			Page<IdmRequestIdentityRoleDto> concepts = conceptRoleRequestManager.find(filter, pageable, permission);
			results.addAll(concepts.getContent());
		}

		final List<AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>> resources = new ArrayList<>();
		resources.add(conceptRoleRequestManager);
		if (shouldLoadAssignedRoles(filter, returnOnlyChanges)) {
			resources.add(roleAssignmentManager);
		}

		return new MultiSourcePagedResource<>(resources, modelMapper).find(filter, pageable, permission);

		

	}

	private static boolean shouldLoadAssignedRoles(IdmRequestIdentityRoleFilter filter, boolean returnOnlyChanges) {
		return !returnOnlyChanges && filter.getIdentityId() != null;
	}

	@Override
	@Transactional
	public IdmRequestIdentityRoleDto save(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		LOG.debug(MessageFormat.format("Save idm-request-identity-role [{0}] ", dto));
		Assert.notNull(dto, "DTO is required.");
		if (dto.getRoleRequest() == null) {
			final IdmRoleRequestDto request = roleRequestService.createRequest(dto);
			dto.setRoleRequest(request.getId());
			dto.getEmbedded().put(AbstractConceptRoleRequest_.ROLE_REQUEST, request);
		}
		//
		return conceptRoleRequestManager.getServiceForConcept(dto.getAssignmentType()).saveRequestRole(dto, permission);
	}

	@Override
	@Transactional
	public IdmRequestIdentityRoleDto deleteRequestIdentityRole(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		LOG.debug(MessageFormat.format("Delete idm-request-identity-role [{0}] ", dto));
		Assert.notNull(dto, "DTO cannot be null!");
		Assert.notNull(dto.getId(), "ID of request-identity-role DTO cannot be null!");

		if (dto.getRoleRequest() == null) {
			final IdmRoleRequestDto request = roleRequestService.createRequest(dto);
			dto.setRoleRequest(request.getId());
			dto.getEmbedded().put(AbstractConceptRoleRequest_.ROLE_REQUEST, request);
		}
		//
		return conceptRoleRequestManager.getServiceForConcept(dto.getAssignmentType()).deleteRequestRole(dto, permission);
	}
	
	/**
	 * Not supported, use deleteRequestIdentityRole!
	 */
	@Override
	@Deprecated
	public void delete(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		throw new UnsupportedOperationException();
	}
	

	
	/**
	 * Creates new manual request for given identity
	 * 
	 * @param identityId
	 * @return
	 */
	private IdmRoleRequestDto createRequest(UUID identityId) {
		Assert.notNull(identityId, "Identity id must be filled for create role request!");
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(identityId);
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);
		LOG.debug(MessageFormat.format("New manual role-request [{1}] was created.", roleRequest));
		
		return roleRequest;
	}




}

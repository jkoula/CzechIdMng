package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.InvalidFormException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;
import eu.bcvsolutions.idm.core.api.service.thin.IdmIdentityRoleThinService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConceptRoleRequestRepository;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of concept role request service
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Service("conceptRoleRequestService")
public class DefaultIdmConceptRoleRequestService extends
		AbstractConceptRoleRequestService<IdmConceptRoleRequestDto, IdmConceptRoleRequest, IdmConceptRoleRequestFilter> implements IdmConceptRoleRequestService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmConceptRoleRequestService.class);

	@Autowired
	private IdmIdentityRoleThinService identityRoleThinService;

	@Autowired
	private FormService formService;

	@Autowired
	public DefaultIdmConceptRoleRequestService(IdmConceptRoleRequestRepository repository,
			WorkflowProcessInstanceService workflowProcessInstanceService, LookupService lookupService,
			IdmAutomaticRoleRepository automaticRoleRepository) {
		super(repository, workflowProcessInstanceService, lookupService, automaticRoleRepository);
		//
		Assert.notNull(workflowProcessInstanceService, "Workflow process instance service is required!");
		Assert.notNull(lookupService, "Service is required.");
		Assert.notNull(automaticRoleRepository, "Repository is required.");
	}

	@Override
	protected IdmConceptRoleRequestDto toDto(IdmConceptRoleRequest entity, IdmConceptRoleRequestDto dto) {
		final IdmConceptRoleRequestDto result = super.toDto(entity, dto);
		// Contract from identity role has higher priority then contract ID in concept
		// role
		if (entity != null && entity.getIdentityRole() != null) {
			result.setIdentityContract(entity.getIdentityRole().getIdentityContract().getId());
		}
		return result;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmConceptRoleRequest> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmConceptRoleRequestFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getIdentityRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityRole).get(IdmIdentityRole_.id),
					filter.getIdentityRoleId()));
		}
		if (filter.getIdentityContractId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityContract).get(IdmIdentityContract_.id),
					filter.getIdentityContractId()));
		}
		
		Set<UUID> ids = filter.getIdentityRoleIds();
		if (ids != null && !ids.isEmpty()) {
			predicates.add(root.get(IdmConceptRoleRequest_.identityRole).get(IdmIdentityRole_.id).in(ids));
		}
		
		if (filter.isIdentityRoleIsNull()) {
			predicates.add(builder.isNull(root.get(IdmConceptRoleRequest_.identityRole)));
		}
		//
		return predicates;
	}


	@Override
	protected UUID getIdentityRoleId(IdmConceptRoleRequestDto concept) {
		UUID identityRoleId = null;
		IdmIdentityRoleDto identityRoleDto = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityRole,
				IdmIdentityRoleDto.class, null);
		if(identityRoleDto == null) {
			identityRoleId = concept.getIdentityRole();
		} else {
			identityRoleId = identityRoleDto.getId();
		}
		return identityRoleId;
	}

	@Override
	protected IdmFormInstanceDto getFormInstance(IdmConceptRoleRequestDto dto, IdmFormDefinitionDto formDefinition) {
		IdmIdentityRoleDto identityRoleDto = DtoUtils.getEmbedded(dto, IdmConceptRoleRequest_.identityRole,
				IdmIdentityRoleDto.class, null);
		if(identityRoleDto == null) {
			identityRoleDto = identityRoleThinService.get(dto.getIdentityRole());
		}
		return formService.getFormInstance(
				new IdmIdentityRoleDto(identityRoleDto.getId()),
				formDefinition);

	}

	@Override
	protected boolean shouldProcessChanges(IdmConceptRoleRequestDto dto, ConceptRoleRequestOperation operation) {
		return dto.getIdentityRole() != null && ConceptRoleRequestOperation.UPDATE == operation;
	}

	@Override
	protected IdmConceptRoleRequestFilter getFilter() {
		return new IdmConceptRoleRequestFilter();
	}

	@Override
	protected IdmFormValueDto getCurrentFormValue(UUID identityRoleId, IdmFormValueDto value, IdmFormAttributeDto formAttributeDto) {
		IdmFormValueDto identityRoleValueDto = formService.getValues(new IdmIdentityRoleDto(identityRoleId), formAttributeDto)
				.stream()
				.filter(identityRoleValue -> identityRoleValue.getSeq() == value.getSeq())
				.findFirst()
				.orElse(null);
		return identityRoleValueDto;
	}
}

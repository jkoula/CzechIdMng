package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.api.service.thin.IdmIdentityRoleThinService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConceptRoleRequestRepository;
import eu.bcvsolutions.idm.core.model.service.impl.adapter.DefaultRequestRoleConceptAdapter;
import eu.bcvsolutions.idm.core.model.service.util.MultiSourcePagedResource;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.ADD;
import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.UPDATE;

/**
 * Default implementation of concept role request service
 *
 * @author svandav
 * @author Radek Tomiška
 */
@Service("conceptRoleRequestService")
public class DefaultIdmConceptRoleRequestService extends AbstractConceptRoleRequestService<IdmIdentityRoleDto, IdmConceptRoleRequestDto, IdmConceptRoleRequest, IdmConceptRoleRequestFilter> implements IdmConceptRoleRequestService {


    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmConceptRoleRequestService.class);

    @Autowired
    private IdmIdentityRoleThinService identityRoleThinService;

    @Autowired
    private FormService formService;

    private final IdmRoleService roleService;

    private final IdmIdentityContractService identityContractService;

    private final IdmIdentityRoleService identityRoleService;

    private final LookupService lookupService;

    private final ModelMapper modelMapper;

    private final IdmRoleRequestService roleRequestService;

    private final IdmRoleSystemService roleSystemService;

    private final WorkflowProcessInstanceService workflowProcessInstanceService;

    @Autowired
    public DefaultIdmConceptRoleRequestService(IdmConceptRoleRequestRepository repository, WorkflowProcessInstanceService workflowProcessInstanceService, LookupService lookupService,
            IdmAutomaticRoleRepository automaticRoleRepository, IdmRoleService roleService, IdmIdentityContractService identityContractService, IdmIdentityRoleService identityRoleService,
            IdmRoleCompositionService roleCompositionService, LookupService lookupService1, ModelMapper modelMapper, @Lazy IdmRoleRequestService roleRequestService, IdmRoleSystemService roleSystemService,
            WorkflowProcessInstanceService workflowProcessInstanceService1) {
        super(repository, workflowProcessInstanceService, roleCompositionService, lookupService, automaticRoleRepository, identityRoleService);
        this.roleService = roleService;
        this.identityContractService = identityContractService;
        this.identityRoleService = identityRoleService;
        this.lookupService = lookupService1;
        this.modelMapper = modelMapper;
        this.roleRequestService = roleRequestService;
        this.roleSystemService = roleSystemService;
        this.workflowProcessInstanceService = workflowProcessInstanceService1;
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
    protected IdmIdentityRoleDto extractRoleAssignmentFromConcept(IdmConceptRoleRequestDto concept) {
        IdmIdentityRoleDto identityRole = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityRole.getName(), IdmIdentityRoleDto.class, (IdmIdentityRoleDto) null);
        if (identityRole == null) {
            identityRole = identityRoleThinService.get(concept.getIdentityRole());
        }
        return identityRole;
    }


    @Override
    protected IdmIdentityRoleDto getRoleAssignmentFromConceptInternal(IdmConceptRoleRequestDto conceptRole) {
        if (conceptRole == null) {
            return null;
        }
        IdmIdentityRoleDto identityRoleDto = new IdmIdentityRoleDto();
        identityRoleDto.setIdentityContract(conceptRole.getIdentityContract());
        identityRoleDto.setContractPosition(conceptRole.getContractPosition());
        return identityRoleDto;
    }

    @Override
    protected IdmIdentityRoleDto getRoleAssignmentDtoInternal(IdmConceptRoleRequestDto concept, IdmRoleDto subRole) {
        IdmIdentityContractDto identityContract = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityContract, IdmIdentityContractDto.class, null);
        IdmIdentityRoleDto identityRoleDto = new IdmIdentityRoleDto();
        identityRoleDto.setIdentityContract(concept.getIdentityContract());
        identityRoleDto.setIdentityContractDto(identityContract);
        return identityRoleDto;
    }

    @Override
    protected String cancelInvalidConceptInternal(List<IdmIdentityRoleDto> automaticRoles, IdmConceptRoleRequestDto concept, IdmRoleRequestDto request) {
        if (concept.getIdentityContract() == null) {
            return MessageFormat.format("Request change in concept [{0}], was not executed, because identity contract" + " was deleted before (not from this role request)!", concept.getId());
        } else if (ADD == concept.getOperation() && concept.getAutomaticRole() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(automaticRoles)) {
            // check duplicate assigned automatic role
            IdmIdentityRoleDto assignedRole = automaticRoles.stream().filter(ir -> ir.getAutomaticRole() != null).filter(ir -> com.google.common.base.Objects.equal(ir.getAutomaticRole(),
                    concept.getAutomaticRole())).filter(ir -> com.google.common.base.Objects.equal(ir.getContractPosition(), concept.getContractPosition())).filter(ir -> com.google.common.base.Objects.equal(ir.getIdentityContract(), concept.getIdentityContract())).findFirst().orElse(null);
            if (assignedRole != null) {
                return MessageFormat.format("Request change in concept [{0}], was not executed, because requested " + "automatic role was already assigned (not from this role request)!",
                        concept.getId());
            }
        }
        return null;
    }

    @Override
    protected IdmIdentityRoleDto createAssignmentFromConceptInternal(IdmConceptRoleRequestDto concept) {
        IdmIdentityContractDto identityContract = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityContract, IdmIdentityContractDto.class, null);

        IdmIdentityRoleDto result = new IdmIdentityRoleDto();
        result.setIdentityContract(concept.getIdentityContract());

        if (identityContract != null) {
            // contract cen be deleted in the mean time
            result.setIdentityContractDto(identityContract);
        }

        return result;
    }

    @Override
    protected List<Predicate> toPredicates(Root<IdmConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmConceptRoleRequestFilter filter) {
        List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        if (filter.getIdentityRoleId() != null) {
            predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityRole).get(IdmIdentityRole_.id), filter.getIdentityRoleId()));
        }
        if (filter.getIdentityContractId() != null) {
            predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityContract).get(IdmIdentityContract_.id), filter.getIdentityContractId()));
        }

        if (filter.getIdentity() != null) {
            Subquery<IdmIdentityContract> contractSub = query.subquery(IdmIdentityContract.class);
            final Root<IdmIdentityContract> contractRoot = contractSub.from(IdmIdentityContract.class);
            contractSub.select(contractRoot);

            contractSub.where(
                    builder.and(
                            builder.equal(root.get(IdmConceptRoleRequest_.identityContract), contractRoot),
                            builder.equal(contractRoot.get(IdmIdentityContract_.identity).get(AbstractEntity_.id), filter.getIdentity())
            ));
            predicates.add(builder.and(
                    builder.exists(contractSub)
                    //builder.equal(root.get(IdmConceptRoleRequest_.identityContract), contractSub)
                    ));
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
        IdmIdentityRoleDto identityRoleDto = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityRole, IdmIdentityRoleDto.class, null);
        if (identityRoleDto == null) {
            identityRoleId = concept.getIdentityRole();
        } else {
            identityRoleId = identityRoleDto.getId();
        }
        return identityRoleId;
    }

    @Override
    protected IdmFormInstanceDto getFormInstance(IdmConceptRoleRequestDto dto, IdmFormDefinitionDto formDefinition) {
        IdmIdentityRoleDto identityRoleDto = DtoUtils.getEmbedded(dto, IdmConceptRoleRequest_.identityRole, IdmIdentityRoleDto.class, null);
        if (identityRoleDto == null) {
            identityRoleDto = identityRoleThinService.get(dto.getIdentityRole());
        }
        return formService.getFormInstance(new IdmIdentityRoleDto(identityRoleDto.getId()), formDefinition);

    }

    @Override
    protected boolean shouldProcessChanges(IdmConceptRoleRequestDto dto, ConceptRoleRequestOperation operation) {
        return dto.getIdentityRole() != null && ConceptRoleRequestOperation.UPDATE == operation;
    }

    @Override
    public IdmConceptRoleRequestFilter getFilter() {
        return new IdmConceptRoleRequestFilter();
    }

    @Override
    public Set<String> getTransitivePermissions(IdmConceptRoleRequestDto concept) {
        Set<String> result = new HashSet<>();
        IdmIdentityContractDto contract = lookupService.lookupEmbeddedDto(concept, IdmConceptRoleRequest_.identityContract);
        Set<String> contractPermissions = identityContractService.getPermissions(contract);

        if (PermissionUtils.hasPermission(contractPermissions, ContractBasePermission.CHANGEPERMISSION)) {
            result.add(ContractBasePermission.CHANGEPERMISSION.getName());
        } else {
            // by related role
            IdmRoleDto role = lookupService.lookupEmbeddedDto(concept, AbstractConceptRoleRequest_.role);
            Set<String> rolePermissions = roleService.getPermissions(role);

            if (PermissionUtils.hasPermission(rolePermissions, RoleBasePermission.CHANGEPERMISSION)) {
                result.add(RoleBasePermission.CHANGEPERMISSION.getName());
            }
        }
        return result;
    }

    @Override
    public IdmIdentityRoleDto getEmbeddedAssignment(IdmConceptRoleRequestDto concept) {
        return DtoUtils.getEmbedded(concept,
                IdmConceptRoleRequest_.identityRole, IdmIdentityRoleDto.class,
                (IdmIdentityRoleDto) null);
    }

    @Override
    public IdmIdentityRoleDto fetchAssignment(IdmConceptRoleRequestDto concept) {
        return identityRoleService.get(concept.getIdentityRole());
    }

    @Override
    public ApplicantDto resolveApplicant(IdmRequestIdentityRoleDto dto) {
        final IdmIdentityContractDto contractDto = identityContractService.get(dto.getOwnerUuid());
        ApplicantImplDto result = new ApplicantImplDto();
        result.setConceptOwner(contractDto.getId());
        result.setId(contractDto.getIdentity());
        result.setValidFrom(contractDto.getValidFrom());
        result.setValidTill(contractDto.getValidTill());
        // If owner of the concept is IdmIdentityCOntractDto, then applicant must be IdmIdentityDto
        result.setApplicantType(IdmIdentityDto.class.getCanonicalName());
        return result;
    }

    @Override
    public List<UUID> resolveManagerContractsForApproval(IdmConceptRoleRequestDto conceptRole) {
        return Collections.singletonList(conceptRole.getIdentityContract());
    }

    @Override
    protected IdmConceptRoleRequestDto requestIdentityRoleToConcept(IdmRequestIdentityRoleDto dto) {
        final IdmConceptRoleRequestDto result = modelMapper.map(dto, IdmConceptRoleRequestDto.class);
        if (result.getRole() == null && dto.getRoleAssignmentUuid() != null) {
            result.setRole(identityRoleService.get(dto.getRoleAssignmentUuid()).getRole());
        }

        if (result.getOperation() == null) {
            if (dto.getRoleAssignmentUuid() == null) {
                result.setOperation(ADD);
            } else {
                result.setOperation(UPDATE);
            }
        }
        return result;
    }

    @Override
    protected IdmRequestIdentityRoleDto conceptToRequestIdentityRole(IdmConceptRoleRequestDto save) {
        return modelMapper.map(save, IdmRequestIdentityRoleDto.class);
    }

    @Override
    protected IdmConceptRoleRequestDto createEmptyConceptWithRoleAssignmentData(IdmIdentityRoleDto roleAssignment) {
        IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
        IdmIdentityContractDto contractDto = lookupService.lookupEmbeddedDto(roleAssignment, IdmIdentityRole_.identityContract);
        conceptRoleRequest.setOwnerUuid(roleAssignment.getIdentityContract());

        conceptRoleRequest.setValidFrom(contractDto.getValidFrom());
        conceptRoleRequest.setValidTill(contractDto.getValidTill());
        return conceptRoleRequest;
    }

    @Override
    protected IdmIdentityRoleDto getRoleAssignment(UUID id) {
        return identityRoleService.get(id);
    }

    @Override
    protected IdmFormValueDto getCurrentFormValue(UUID identityRoleId, IdmFormValueDto value, IdmFormAttributeDto formAttributeDto) {
        return formService.getValues(new IdmIdentityRoleDto(identityRoleId), formAttributeDto).stream()
                .filter(identityRoleValue -> identityRoleValue.getSeq() == value.getSeq())
                .findFirst()
                .orElse(null);
    }

    @Override
    public IdmRoleDto determineRoleFromConcept(IdmConceptRoleRequestDto concept) {
        IdmRoleDto role = null;
        if (concept.getRole() != null) {
            role = DtoUtils.getEmbedded(concept, AbstractConceptRoleRequest_.role, IdmRoleDto.class, null);
            if (role == null) {
                role = roleService.get(concept.getRole());
            }
        } else {
            IdmIdentityRoleDto identityRole = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityRole, IdmIdentityRoleDto.class, null);
            if (identityRole == null) {
                identityRole = identityRoleThinService.get(concept.getIdentityRole());
            }
            if (identityRole != null) {
                role = DtoUtils.getEmbedded(concept, AbstractRoleAssignment_.role, IdmRoleDto.class);
            }
        }
        return role;
    }

    @Override
    public boolean validOwnership(IdmConceptRoleRequestDto concept, UUID applicantId) {
        // get contract DTO from embedded map
        IdmIdentityContractDto contract = (IdmIdentityContractDto) concept.getEmbedded().get(IdmConceptRoleRequestService.IDENTITY_CONTRACT_FIELD);
        if (contract == null) {
            contract = identityContractService.get(concept.getIdentityContract());
        }
        Assert.notNull(contract, "Contract cannot be empty!");
        return applicantId.equals(contract.getIdentity());
    }

    @Override
    public List<IdmConceptRoleRequestDto> findAllByRoleRequest(UUID requestId, Pageable pa, IdmBasePermission... permissions) {
        IdmConceptRoleRequestFilter filter = new IdmConceptRoleRequestFilter();
        filter.setRoleRequestId(requestId);
        return find(filter, pa, permissions).getContent();
    }

    @Override
    public IdmConceptRoleRequestDto  createConceptToRemoveIdentityRole(IdmConceptRoleRequestDto concept, IdmIdentityRoleDto identityRoleAssignment) {
        IdmConceptRoleRequestDto removeConcept = new IdmConceptRoleRequestDto();
        removeConcept.setIdentityContract(identityRoleAssignment.getIdentityContract());
        removeConcept.setIdentityRole(identityRoleAssignment.getId());
        removeConcept.setOperation(ConceptRoleRequestOperation.REMOVE);
        if (concept != null) {
            removeConcept.setRoleRequest(concept.getRoleRequest());
        }
        removeConcept = save(removeConcept);
        return removeConcept;
    }

    @Override
    public IdmConceptRoleRequestDto createConceptToRemoveIdentityRole(IdmIdentityRoleDto roleAssignment) {
        IdmConceptRoleRequestDto removeConcept = createEmptyConcept();
        removeConcept.setIdentityContract(roleAssignment.getIdentityContract());
        removeConcept.setIdentityRole(roleAssignment.getId());
        removeConcept.setOperation(ConceptRoleRequestOperation.REMOVE);
        return removeConcept;
    }

    @Override
    public IdmConceptRoleRequestDto createEmptyConcept() {
        return new IdmConceptRoleRequestDto();
    }

    @Override
    public Class<IdmConceptRoleRequestDto> getType() {
        return IdmConceptRoleRequestDto.class;
    }


    @Override
    public <F2 extends BaseFilter> DtoAdapter<IdmConceptRoleRequestDto, IdmRequestIdentityRoleDto> getAdapter(F2 originalFilter) {
        // Need to translate the filter. API is too general here, but
        IdmRequestIdentityRoleFilter translatedFilter = modelMapper.map(originalFilter, IdmRequestIdentityRoleFilter.class);
        return new DefaultRequestRoleConceptAdapter<>(identityRoleService, this, roleRequestService, roleSystemService, translatedFilter, workflowProcessInstanceService, modelMapper);
    }

    @Override
    public Class<?> getOwnerType() {
        return IdmIdentityContractDto.class;
    }
}

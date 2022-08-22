package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractConceptRoleRequestService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("accountConceptRoleService")
public class DefaultAccAccountConceptRoleRequestService extends AbstractConceptRoleRequestService<AccAccountConceptRoleRequestDto, AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFilter> implements AccAccountConceptRoleRequestService {


    private final AccAccountRoleService accRoleAccountService;
    private final FormService formService;

    @Autowired
    public DefaultAccAccountConceptRoleRequestService(AbstractEntityRepository<AccAccountConceptRoleRequest> repository, WorkflowProcessInstanceService workflowProcessInstanceService, LookupService lookupService, IdmAutomaticRoleRepository automaticRoleRepository, AccAccountRoleService accRoleAccountService, FormService formService) {
        super(repository, workflowProcessInstanceService, lookupService, automaticRoleRepository);
        this.accRoleAccountService = accRoleAccountService;
        this.formService = formService;
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountConceptRoleRequestFilter filter) {
        List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        if (filter.getAccountRole() != null) {
            predicates.add(builder.equal(root.get(AccAccountConceptRoleRequest_.accountRole).get(AbstractEntity_.id),
                    filter.getAccountRole()));
        }
        if (filter.getAccountuuid() != null) {
            predicates.add(builder.equal(root.get(AccAccountConceptRoleRequest_.accAccount).get(AbstractEntity_.id),
                    filter.getAccountuuid()));
        }

        Set<UUID> ids = filter.getAccountRoleUuids();
        if (ids != null && !ids.isEmpty()) {
            predicates.add(root.get(AccAccountConceptRoleRequest_.accountRole).get(AbstractEntity_.id).in(ids));
        }

        if (filter.isAccountRoleIsNull()) {
            predicates.add(builder.isNull(root.get(AccAccountConceptRoleRequest_.accountRole)));
        }
        //
        return predicates;
    }

    @Override
    protected IdmFormValueDto getCurrentFormValue(UUID identityRoleId, IdmFormValueDto value, IdmFormAttributeDto formAttributeDto) {
        return null;
    }

    @Override
    protected IdmFormInstanceDto getFormInstance(AccAccountConceptRoleRequestDto dto, IdmFormDefinitionDto definition) {
        AccAccountRoleDto identityRoleDto = DtoUtils.getEmbedded(dto, AccAccountConceptRoleRequest_.accountRole,
                AccAccountRoleDto.class, null);
        if(identityRoleDto == null) {
            identityRoleDto = accRoleAccountService.get(dto.getAccountRole());
        }
        return formService.getFormInstance(
                new IdmIdentityRoleDto(identityRoleDto.getId()),
                definition);
    }

    @Override
    protected boolean shouldProcessChanges(AccAccountConceptRoleRequestDto dto, ConceptRoleRequestOperation operation) {
        return dto.getAccountRole() != null && ConceptRoleRequestOperation.UPDATE == operation;
    }

    @Override
    protected AccAccountConceptRoleRequestFilter getFilter() {
        return new AccAccountConceptRoleRequestFilter();
    }

    @Override
    protected UUID getIdentityRoleId(AccAccountConceptRoleRequestDto concept) {
        UUID accountRoleId = null;
        AccAccountConceptRoleRequestDto accountRoleDto = DtoUtils.getEmbedded(concept, AccAccountConceptRoleRequest_.accountRole,
                AccAccountConceptRoleRequestDto.class, null);
        if(accountRoleDto == null) {
            accountRoleId = concept.getAccountRole();
        } else {
            accountRoleId = accountRoleDto.getId();
        }
        return accountRoleId;
    }
}

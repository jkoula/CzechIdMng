package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractConceptRoleRequestService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
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
public class DefaultAccAccountConceptRoleRequestService extends AbstractConceptRoleRequestService<AccAccountConceptRoleRequestDto, AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFilter> {

    protected DefaultAccAccountConceptRoleRequestService(AbstractEntityRepository<AccAccountConceptRoleRequest> repository, WorkflowProcessInstanceService workflowProcessInstanceService, LookupService lookupService, IdmAutomaticRoleRepository automaticRoleRepository) {
        super(repository, workflowProcessInstanceService, lookupService, automaticRoleRepository);
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountConceptRoleRequestFilter filter) {
        List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //


        //
        return predicates;
    }

    @Override
    protected IdmFormValueDto getCurrentFormValue(UUID identityRoleId, IdmFormValueDto value, IdmFormAttributeDto formAttributeDto) {
        return null;
    }

    @Override
    protected IdmFormInstanceDto getFormInstance(AccAccountConceptRoleRequestDto dto, IdmFormDefinitionDto definition) {
        return null;
    }

    @Override
    protected boolean shouldProcessChanges(AccAccountConceptRoleRequestDto dto, ConceptRoleRequestOperation operation) {
        return false;
    }

    @Override
    protected AccAccountConceptRoleRequestFilter getFilter() {
        return null;
    }

    @Override
    protected UUID getIdentityRoleId(AccAccountConceptRoleRequestDto concept) {
        return null;
    }
}

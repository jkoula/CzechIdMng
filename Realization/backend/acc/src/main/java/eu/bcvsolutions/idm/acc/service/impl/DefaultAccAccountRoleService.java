package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountRole;
import eu.bcvsolutions.idm.acc.repository.AccAccountRoleRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleAssignmentRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractRoleAssignmentService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("accountRoleService")
public class DefaultAccAccountRoleService extends AbstractRoleAssignmentService<AccAccountRoleDto, AccAccountRole, AccAccountRoleFilter> implements AccAccountRoleService {

    private final AccAccountRoleRepository accAccountRoleRepository;

    @Autowired
    public DefaultAccAccountRoleService(AccAccountRoleRepository repository, EntityEventManager entityEventManager,
                                        FormService formService, IdmRoleService roleService, IdmAutomaticRoleRepository automaticRoleRepository,
                                        LookupService lookupService, FilterManager filterManager) {
        super(repository, entityEventManager, formService, roleService, automaticRoleRepository, lookupService, filterManager);
        this.accAccountRoleRepository = repository;
    }

    @Override
    public AuthorizableType getAuthorizableType() {
        //TODO
        return null;
    }

    @Override
    protected AccAccountRoleFilter getFilter() {
        return new AccAccountRoleFilter();
    }

    @Override
    protected LocalDate getDateForValidTill(AccAccountRoleDto one) {
        return one.getValidTill();
    }

    @Override
    public List<AccAccountRoleDto> findByAccountId(UUID id) {
        return accAccountRoleRepository.findByAccount_Id(id);
    }
}

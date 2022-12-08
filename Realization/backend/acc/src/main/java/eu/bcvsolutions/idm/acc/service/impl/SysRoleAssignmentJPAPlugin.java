package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface SysRoleAssignmentJPAPlugin {
    /**
     * This method is used by {@link eu.bcvsolutions.idm.acc.service.impl.IdentityProvisioningExecutor} to fetch overriden attributes
     * for all role assignments of given identity. Since modules can introduce other types of {@link AbstractRoleAssignmentDto}, this method
     * must take all subtypes into consideration.
     * Internally it delegates this responsibility to each particular {@link IdmRoleAssignmentService}
     *
     */
    List<Predicate> getOverridenAttributesPredicates(Root<SysRoleSystemAttribute> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysRoleSystemAttributeFilter filter);
}

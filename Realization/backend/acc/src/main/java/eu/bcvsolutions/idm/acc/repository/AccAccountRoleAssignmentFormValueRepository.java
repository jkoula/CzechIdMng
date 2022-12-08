package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequestFormValue;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignmentFormValue;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;

/**
 * Extended attributes for concept acc role assignment
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public interface AccAccountRoleAssignmentFormValueRepository extends AbstractFormValueRepository<AccAccountRoleAssignment, AccAccountRoleAssignmentFormValue> {
	
}

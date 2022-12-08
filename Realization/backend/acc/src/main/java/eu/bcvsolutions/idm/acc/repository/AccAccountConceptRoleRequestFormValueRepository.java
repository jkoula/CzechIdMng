package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequestFormValue;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmConceptRoleRequestFormValue;

/**
 * Extended attributes for concept acc role request
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public interface AccAccountConceptRoleRequestFormValueRepository extends AbstractFormValueRepository<AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFormValue> {
	
}

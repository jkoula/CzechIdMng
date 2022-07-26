package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.eav.entity.AccAccountFormValue;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;

/**
 * Extended attributes' values for accounts.
 * 
 * @author Tomáš Doischer
 *
 */
public interface AccAccountFormValueRepository  extends AbstractFormValueRepository<AccAccount, AccAccountFormValue> {

}

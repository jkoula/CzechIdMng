package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.eav.entity.AccAccountFormValue;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.service.api.AccAccountFormValueService;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;

public class DefaultAccAccountFormValueService extends AbstractFormValueService<AccAccount, AccAccountFormValue>
	implements AccAccountFormValueService {
	
	public DefaultAccAccountFormValueService(AbstractFormValueRepository<AccAccount, AccAccountFormValue> repository) {
		super(repository);
	}
}

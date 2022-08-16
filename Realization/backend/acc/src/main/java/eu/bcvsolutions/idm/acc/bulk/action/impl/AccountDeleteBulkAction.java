package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

@Component("accountDeleteBulkAction")
@Description("Delete given account")
public class AccountDeleteBulkAction extends AbstractRemoveBulkAction<AccAccountDto, AccAccountFilter> {

	public static final String NAME = "account-delete-bulk-action";
	
	@Autowired
	private AccAccountService accountService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.ACCOUNT_DELETE);
	}
	
	@Override
	public ReadWriteDtoService<AccAccountDto, AccAccountFilter> getService() {
		return accountService;
	}

}

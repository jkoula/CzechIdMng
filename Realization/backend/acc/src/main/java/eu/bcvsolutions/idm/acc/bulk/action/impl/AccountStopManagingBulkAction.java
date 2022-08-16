package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Delete an account in IdM without provisioning.
 * 
 * @author Tomáš Doischer
 *
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(AccountStopManagingBulkAction.NAME)
@Description("Stops managing an account (delete without provisioning)")
public class AccountStopManagingBulkAction extends AbstractBulkAction<AccAccountDto, AccAccountFilter> {

	public static final String NAME = "account-stop-managing-bulk-action";

	@Autowired
	private AccAccountService accountService;

	@Override
	public ReadWriteDtoService<AccAccountDto, AccAccountFilter> getService() {
		return accountService;
	}

	@Override
	protected OperationResult processDto(AccAccountDto dto) {
		accountService.publish(new AccountEvent(AccountEventType.DELETE, dto,
				Map.of(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY, Boolean.FALSE)));
		
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.ACCOUNT_DELETE);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
}

package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioning of account.
 * 
 * @author Tomáš Doischer
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(AccountProvisioningBulkAction.NAME)
@Description("Executes provisioning of account")
public class AccountProvisioningBulkAction extends AbstractBulkAction<AccAccountDto, AccAccountFilter> {
	
	public static final String NAME = "acc-account-provisioning-bulk-action";

	@Autowired
	private AccAccountService accountService;
	@Autowired
	private ProvisioningService provisioningService;

	@Override
	protected OperationResult processDto(AccAccountDto dto) {
		provisioningService.doProvisioning(dto);

		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	public ReadWriteDtoService<AccAccountDto, AccAccountFilter> getService() {
		return accountService;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.ACCOUNT_READ, AccGroupPermission.ACCOUNT_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1600;
	}

}

package eu.bcvsolutions.idm.acc.event.processor;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * EAV account save. After it's saved we will do provisioning
 * 
 * @author Roman Kucera
 */
@Component("accAccountProvisioningProcessorAfterEavSave")
@Description("Do provisioning after eav is saved")
public class AccountProvisioningProcessorAfterEavSave extends CoreEventProcessor<IdmFormInstanceDto> {

	private static final String PROCESSOR_NAME = "account-provisioning-processor-after-eav-save";

	@Autowired
	private AccAccountService accountService;
	@Autowired
	private ProvisioningService provisioningService;

	@Autowired
	public AccountProvisioningProcessorAfterEavSave() {
		super(CoreEventType.UPDATE);
	}

	@Override
	public boolean conditional(EntityEvent<IdmFormInstanceDto> event) {
		return super.conditional(event) && event.getContent().getOwnerType().getCanonicalName().equals(AccAccount.class.getCanonicalName()) &&
				((AccAccount)event.getContent().getOwner()).getSystemEntity() != null;
	}

	@Override
	public EventResult<IdmFormInstanceDto> process(EntityEvent<IdmFormInstanceDto> event) {
		Serializable ownerId = event.getContent().getOwnerId();

		AccAccountDto accountDto = accountService.get(ownerId);
		provisioningService.doProvisioning(accountDto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 500;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}

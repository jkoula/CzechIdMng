package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Provisioning start event processor
 * 
 * @author svandav
 *
 */
@Component
@Description("Starts provisioning process by given account and entity")
public class ProvisioningStartProcessor extends AbstractEntityEventProcessor<AccAccountDto> {

	public static final String PROCESSOR_NAME = "provisioning-start-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningStartProcessor.class);
	private final ProvisioningService provisioningService;

	@Autowired
	public ProvisioningStartProcessor(ProvisioningService provisioningService) {
		super(ProvisioningEventType.START);
		//
		Assert.notNull(provisioningService, "Service is required.");
		//
		this.provisioningService = provisioningService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		AccAccountDto account = event.getContent();
		boolean isDryRun = getBooleanProperty(ProvisioningService.DRY_RUN_PROPERTY_NAME, event.getProperties());
		Assert.notNull(account, "Account is required.");
		LOG.info("Provisioning event start, for account id: [{}], account uid: [{}], real uid [{}], system id: [{}], dryRun: [{}]",
				account.getId(), account.getUid(), account.getRealUid(), account.getSystem(), isDryRun);

		if (account.isInProtection() && !isDryRun) {
			if(!isCanceledProvisioningProtectionBreak(event.getProperties())){
				LOG.info("Account [{}] is in protection. Provisioning is skipped.", account.getUid());
				return new DefaultEventResult<>(event, this);				
			}
			LOG.info("Account [{}] is in protection, but cancel attribute is TRUE. Provisioning is not skipped.", account.getUid());
		}

		SysProvisioningOperationDto provisioningOperation = provisioningService.doInternalProvisioning(
				account,
				(AbstractDto) event.getProperties().get(ProvisioningService.DTO_PROPERTY_NAME),
				isDryRun);
		if (isDryRun) {
			event.getProperties().put(ProvisioningService.DRY_RUN_PROVISIONING_OPERATION_PROPERTY_NAME, provisioningOperation);
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	private boolean isCanceledProvisioningProtectionBreak(Map<String, Serializable> properties) {
		Object breakProvisioning = properties.get(ProvisioningService.CANCEL_PROVISIONING_BREAK_IN_PROTECTION);
		if (breakProvisioning instanceof Boolean && (Boolean) breakProvisioning) {
			return true;
		}
		return false;
	}
}
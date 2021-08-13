package eu.bcvsolutions.idm.acc.event.processor.profile;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ProfileProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.ProfileEvent.ProfileEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioning for identity after profile image is saved or deleted.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(ProfileProvisioningProcessor.PROCESSOR_NAME)
@Description("Executes provisioning for identity after profile image is saved or deleted.")
public class ProfileProvisioningProcessor
		extends CoreEventProcessor<IdmProfileDto> 
		implements ProfileProcessor {

	public static final String PROCESSOR_NAME = "acc-profile-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProfileProvisioningProcessor.class);
	//	
	@Autowired private EntityEventManager entityEventManager;	
	@Autowired private ProvisioningService provisioningService;
	
	public ProfileProvisioningProcessor() {
		super(ProfileEventType.DELETE, ProfileEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmProfileDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		if (this.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE, event.getProperties())) {
			return false;
		}
		IdmProfileDto originalSource = event.getOriginalSource();
		IdmProfileDto profile = event.getContent();
		// profile was added with image is defined
		if (originalSource == null && profile.getImage() != null) {
			return true;
		}
		// profile was deleted with image is defined
		if (event.hasType(ProfileEventType.DELETE) && profile.getImage() != null) {
			return true;
		}
		// image is changed
		if (originalSource != null && !Objects.equals(originalSource.getImage(), profile.getImage())) {
			return true;
		}
		// image is not changed - provisioning is not needed.
		return false;
	}

	@Override
	public EventResult<IdmProfileDto> process(EntityEvent<IdmProfileDto> event) {
		UUID identityId = event.getContent().getIdentity();
		//
		// register change => provisioning will be executed for manager
		if (!event.hasType(ProfileEventType.NOTIFY)) {
			IdmIdentityDto identity = getLookupService().lookupDto(IdmIdentityDto.class, identityId);
			if (identity == null) {
				LOG.debug("Identity [{}] was already deleted, duplicate provisioning will be skipped.", identityId);
			} else {
				// sync
				LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
				provisioningService.doProvisioning(identity);
			}
		} else {
			// async
			LOG.debug("Register change for identity [{}]", identityId);
			entityEventManager.changedEntity(IdmIdentityDto.class, identityId, event);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
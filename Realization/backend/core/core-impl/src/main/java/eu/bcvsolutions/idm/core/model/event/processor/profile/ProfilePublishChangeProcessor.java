package eu.bcvsolutions.idm.core.model.event.processor.profile;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.ProfileEvent.ProfileEventType;

/**
 * Publish profile change event.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component
@Description("Publish profile change event.")
public class ProfilePublishChangeProcessor extends AbstractPublishEntityChangeProcessor<IdmProfileDto> {

	public static final String PROCESSOR_NAME = "profile-publish-change-processor";
	
	public ProfilePublishChangeProcessor() {
		super(ProfileEventType.CREATE, ProfileEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	protected EntityEvent<IdmProfileDto> setAdditionalEventProperties(EntityEvent<IdmProfileDto> event) {
		event = super.setAdditionalEventProperties(event);
		// we need to set super entity owner - identity
		event.setSuperOwnerId(event.getContent().getIdentity());
		//
		return event;
	}
}

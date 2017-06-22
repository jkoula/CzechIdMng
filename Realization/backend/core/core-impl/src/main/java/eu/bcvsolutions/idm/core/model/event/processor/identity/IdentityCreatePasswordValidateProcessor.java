package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Processor for validating password, when identity is created
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Validates identity's password before identity is created.")
public class IdentityCreatePasswordValidateProcessor extends CoreEventProcessor<IdmIdentityDto> {
	
	public static final String PROCESSOR_NAME = "identity-create-validate-password-processor";
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdentityCreatePasswordValidateProcessor(
			IdmPasswordPolicyService passwordPolicyService) {
		super(IdentityEventType.CREATE);
		//
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		GuardedString password = event.getContent().getPassword();
		IdmIdentityDto identity = event.getContent();
		
		// when create identity password can be null
		if (password != null) {
			IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
			passwordValidationDto.setPassword(password);
			passwordValidationDto.setIdentity(identity.getId());
			// validate create new password by default password policy
			this.passwordPolicyService.validate(passwordValidationDto);
		}
		
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before identity is saved
		return -1000;
	}
}

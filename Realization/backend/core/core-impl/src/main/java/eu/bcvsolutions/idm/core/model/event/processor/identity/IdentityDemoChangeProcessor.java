package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitAdminIdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitMonitoringProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DemoAdminMonitoringEvaluator;

/**
 * Demo admin identity is changed - enable / disable / username or password is changed.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(IdentityDemoChangeProcessor.PROCESSOR_NAME)
@Description("Demo admin identity is changed - enable / disable / username or password is changed.")
public class IdentityDemoChangeProcessor 
		extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "core-identity-demo-change-processor";
	//
	@Autowired @Qualifier(InitMonitoringProcessor.PROCESSOR_NAME) 
	private InitMonitoringProcessor initMonitoringProcessor;
	@Autowired private DemoAdminMonitoringEvaluator demoAdminMonitoringEvaluator;
	@Autowired private MonitoringManager manager;

	public IdentityDemoChangeProcessor() {
		super(IdentityEventType.CREATE, IdentityEventType.UPDATE, IdentityEventType.DELETE, IdentityEventType.PASSWORD);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		//
		IdmIdentityDto identity = event.getContent();
		IdmIdentityDto originalSource = event.getOriginalSource();
		// demo admin identity is changed => process
		if (InitAdminIdentityProcessor.ADMIN_USERNAME.equals(identity.getUsername())) {
			return true;
		}
		// username was changed => process
		if (originalSource != null && InitAdminIdentityProcessor.ADMIN_USERNAME.equals(originalSource.getUsername())) {
			return true;
		}
		//
		return false;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		String evaluatorType = AutowireHelper.getTargetType(demoAdminMonitoringEvaluator);
		IdmMonitoringDto monitoring = initMonitoringProcessor.findMonitoring(evaluatorType, null, null);
		// evaluate monitoring, if registered and enabled
		if (monitoring != null && !monitoring.isDisabled()) {
			manager.execute(monitoring);
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return 210; // ~ after password is persisted
	}
}

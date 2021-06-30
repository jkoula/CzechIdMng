package eu.bcvsolutions.idm.vs.event.processor.module;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitMonitoringProcessor;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Init product provided monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(VsInitMonitoringProcessor.PROCESSOR_NAME)
@Description("Init product provided monitoring evaluators.")
public class VsInitMonitoringProcessor extends InitMonitoringProcessor {

	public static final String PROCESSOR_NAME = "vs-init-monitoring-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// init database table monitoring evaluators
		initDatabaseTableMonitoring(VsRequestService.class);
		initDatabaseTableMonitoring(VsAccountService.class);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 11020;
	}
}

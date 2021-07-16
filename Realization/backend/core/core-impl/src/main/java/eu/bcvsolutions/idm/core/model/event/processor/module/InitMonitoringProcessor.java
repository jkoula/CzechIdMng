package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DatabaseTableMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DemoAdminMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;

/**
 * Init product provided monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(InitMonitoringProcessor.PROCESSOR_NAME)
@Description("Init product provided monitoring evaluators.")
public class InitMonitoringProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitMonitoringProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-monitoring-processor";
	public static final String PRODUCT_PROVIDED_MONITORING_DESCRIPTION = "Product provided monitoring. Monitoring configured automatically.";
	//
	@Autowired private ApplicationContext applicationContext;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private DatabaseTableMonitoringEvaluator databaseTableMonitoringEvaluator;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	@Autowired private DemoAdminMonitoringEvaluator demoAdminMonitoringEvaluator;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// h2 database warning
		initH2DatabaseMonitoring();
		//
		// demo admin warning
		initDemoAdminMonitoring();
		//
		// init database table monitoring evaluators
		initDatabaseTableMonitoring(WorkflowHistoricProcessInstanceService.class);
		initDatabaseTableMonitoring(IdmEntityStateService.class);
		initDatabaseTableMonitoring(IdmEntityEventService.class);
		initDatabaseTableMonitoring(IdmRoleRequestService.class);
		initDatabaseTableMonitoring(IdmIdentityRoleService.class);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Find already configured monitoring evaluator.
	 * 
	 * @param evaluatorType required
	 * @param instanceId optional
	 * @param evaluatorProperties optional
	 * @return configured monitoring, if exists.
	 */
	public IdmMonitoringDto findMonitoring(String evaluatorType, String instanceId, ConfigurationMap evaluatorProperties) {
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		filter.setInstanceId(instanceId);
		//
		return monitoringService
				.find(filter, null)
				.stream()
				.filter(monitoring -> {
					if (evaluatorProperties == null) {
						return true;
					}
					
					for (String propertyName : evaluatorProperties.getKeys()) {
						if (!monitoring.getEvaluatorProperties().containsKey(propertyName)) {
							return false;
						}
						// toString conversion ~ properties from FE
						if (!monitoring.getEvaluatorProperties().get(propertyName).toString().equals(
								evaluatorProperties.get(propertyName).toString())) {
							return false;
						}
					}
					return true;
				})
				.findFirst()
				.orElse(null);
	}
	
	protected IdmMonitoringDto initH2DatabaseMonitoring() {
		String instanceId = configurationService.getInstanceId();
		String evaluatorType = AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator);
		IdmMonitoringDto monitoring = findMonitoring(evaluatorType, instanceId, null);
		if (monitoring == null) {
			monitoring = new IdmMonitoringDto();
			monitoring.setEvaluatorType(evaluatorType);
			monitoring.setInstanceId(instanceId);
			monitoring.setCheckPeriod(0L); // ~ application start only
			monitoring.setSeq((short) 0); // ~ quick
			monitoring.setDescription(PRODUCT_PROVIDED_MONITORING_DESCRIPTION);
			//
			monitoring = monitoringService.save(monitoring);
			LOG.info("H2 database monitoring for instance id [{}] configured automatically.", instanceId);
		}
		//
		return monitoring;
	}
	
	protected IdmMonitoringDto initDemoAdminMonitoring() {
		String evaluatorType = AutowireHelper.getTargetType(demoAdminMonitoringEvaluator);
		IdmMonitoringDto monitoring = findMonitoring(evaluatorType, null, null);
		if (monitoring == null) {
			monitoring = new IdmMonitoringDto();
			monitoring.setEvaluatorType(evaluatorType);
			monitoring.setInstanceId(configurationService.getInstanceId());
			monitoring.setCheckPeriod(0L); // ~ application start only
			monitoring.setSeq((short) 0); // ~ quick
			monitoring.setDescription(PRODUCT_PROVIDED_MONITORING_DESCRIPTION);
			//
			monitoring = monitoringService.save(monitoring);
			LOG.info("Demo admin monitoring configured automatically.");
		}
		//
		return monitoring;
	}
	
	protected IdmMonitoringDto initDatabaseTableMonitoring(Class<? extends ReadDtoService<?, ?>> readDtoService) {
		String evaluatorType = AutowireHelper.getTargetType(databaseTableMonitoringEvaluator);
		String serviceBeanName = applicationContext.getBeanNamesForType(readDtoService)[0];
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(
				DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, 
				serviceBeanName
		);
		IdmMonitoringDto monitoring = findMonitoring(evaluatorType, null, properties);
		if (monitoring == null) {
			monitoring = new IdmMonitoringDto();
			monitoring.setEvaluatorType(evaluatorType);
			monitoring.setInstanceId(configurationService.getInstanceId());
			monitoring.setCheckPeriod(3600L); // ~ per hour
			// FIXME: dto mapper to get AvailableServiceDto with table => #978
			monitoring.setDescription(
					String.format("%s for service [%s].", StringUtils.substring(PRODUCT_PROVIDED_MONITORING_DESCRIPTION, 0, -1), serviceBeanName)
			);
			ConfigurationMap evaluatorProperties = new ConfigurationMap();
			evaluatorProperties.put(
					DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, 
					serviceBeanName
			);
			evaluatorProperties.put(
					DatabaseTableMonitoringEvaluator.PARAMETER_THRESHOLD, 
					DatabaseTableMonitoringEvaluator.DEFAULT_THRESHOLD
			);
			monitoring.setEvaluatorProperties(evaluatorProperties);
			monitoring.setSeq((short) 0); // ~ quick
			//
			monitoring = monitoringService.save(monitoring);
			LOG.info("Databate table monitoring for service bean name [{}] configured automatically.", serviceBeanName);
		}
		//
		return monitoring;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 11000;
	}
}

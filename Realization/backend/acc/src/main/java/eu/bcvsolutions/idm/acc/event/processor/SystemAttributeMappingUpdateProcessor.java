package eu.bcvsolutions.idm.acc.event.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.event.SystemAttributeMappingEvent.SystemAttributeMappingEventType;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SystemAttributeMappingStrategyRecalculationTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Processor for save {@link SysSystemAttributeMappingDto}
 *
 * @author Roman Kucera
 * @since 13.0.0
 */

@Component("accSystemAttributeMappingUpdateProcessor")
@Description("Update strategy for attribute in roles where is overloaded")
public class SystemAttributeMappingUpdateProcessor extends CoreEventProcessor<SysSystemAttributeMappingDto> {

	private static final String PROCESSOR_NAME = "system-attribute-mapping-update-processor";

	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final LongRunningTaskManager longRunningTaskManager;
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;

	@Override
	public boolean conditional(EntityEvent<SysSystemAttributeMappingDto> event) {
		SysSystemAttributeMappingDto originalSource = event.getOriginalSource();
		SysSystemAttributeMappingDto content = event.getContent();

		return super.conditional(event) && originalSource.getStrategyType() != content.getStrategyType();
	}

	@Autowired
	public SystemAttributeMappingUpdateProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService, LongRunningTaskManager longRunningTaskManager,
			SysSystemMappingService systemMappingService, SysSchemaObjectClassService schemaObjectClassService) {
		super(SystemAttributeMappingEventType.UPDATE);

		Assert.notNull(systemAttributeMappingService, "Service is required.");
		Assert.notNull(longRunningTaskManager, "Service is required.");
		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(schemaObjectClassService, "Service is required.");

		this.systemAttributeMappingService = systemAttributeMappingService;
		this.longRunningTaskManager = longRunningTaskManager;
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
	}

	@Override
	public EventResult<SysSystemAttributeMappingDto> process(EntityEvent<SysSystemAttributeMappingDto> event) {
		SystemAttributeMappingStrategyRecalculationTaskExecutor recalculationTaskExecutor = AutowireHelper.createBean(SystemAttributeMappingStrategyRecalculationTaskExecutor.class);

		SysSystemAttributeMappingDto content = event.getContent();
		SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(content, SysSystemAttributeMapping_.systemMapping, SysSystemMappingDto.class, null);
		if (systemMappingDto == null) {
			systemMappingDto = systemMappingService.get(content.getSystemMapping());
		}
		SysSchemaObjectClassDto schemaObjectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class, null);
		if (schemaObjectClassDto == null) {
			schemaObjectClassDto = schemaObjectClassService.get(systemMappingDto.getObjectClass());
		}
		Map<String, Object> properties = new HashMap<>();
		properties.put(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES, "{ \"system\": \"" + schemaObjectClassDto.getSystem() +
				"\", \"systemMapping\": \"" + content.getSystemMapping() +
				"\", \"mappingAttributes\": [ \"" + content.getId() + "\" ] }");
		recalculationTaskExecutor.init(properties);
		longRunningTaskManager.execute(recalculationTaskExecutor);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		// after save
		return CoreEvent.DEFAULT_ORDER + 100;
	}

	@Override
	public boolean isDisableable() {
		return true;
	}
}

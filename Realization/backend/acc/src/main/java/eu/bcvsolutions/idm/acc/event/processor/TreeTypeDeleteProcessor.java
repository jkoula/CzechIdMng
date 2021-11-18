package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent.TreeTypeEventType;

/**
 * Before tree type delete - check connection on systems mapping.
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Component(TreeTypeDeleteProcessor.PROCESSOR_NAME)
@Description("Ensures referential integrity. Cannot be disabled.")
public class TreeTypeDeleteProcessor extends AbstractEntityEventProcessor<IdmTreeTypeDto> {

	public static final String PROCESSOR_NAME = "acc-tree-type-delete-processor";
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private SysSyncConfigRepository syncConfigRepository;
	@Autowired
	private SysSyncConfigService syncConfigService;

	public TreeTypeDeleteProcessor() {
		super(TreeTypeEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTreeTypeDto> process(EntityEvent<IdmTreeTypeDto> event) {
		IdmTreeTypeDto treeType = event.getContent();
		UUID treeTypeId = treeType.getId();
		Asserts.notNull(treeTypeId, "Tree type identifier is required.");
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		//
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setTreeTypeId(treeTypeId);
		List<SysSystemMappingDto> mappings = systemMappingService.find(filter, null).getContent();
		if (!forceDelete) {
			long count = mappings.size();
			if (count > 0) {
				SysSystemDto systemDto = systemService
						.get(schemaObjectClassService.get(mappings.get(0).getObjectClass()).getSystem());
				throw new TreeTypeException(AccResultCode.SYSTEM_MAPPING_TREE_TYPE_DELETE_FAILED,
						ImmutableMap.of("treeType", treeType.getCode(), "system", systemDto.getCode()));
			}
		} else {
			mappings.forEach(mapping -> {
				SystemMappingEvent mappingEvent = new SystemMappingEvent(SystemMappingEventType.DELETE, mapping);
				//
				systemMappingService.publish(mappingEvent, event);
			});
		}

		// Delete link to sync contract configuration.
		syncConfigRepository
			.findByDefaultTreeType(treeTypeId)
			.forEach(config -> {
				SysSyncContractConfigDto configDto = (SysSyncContractConfigDto) syncConfigService.get(config.getId());
				configDto.setDefaultTreeType(null);
				syncConfigService.save(configDto);
			});

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
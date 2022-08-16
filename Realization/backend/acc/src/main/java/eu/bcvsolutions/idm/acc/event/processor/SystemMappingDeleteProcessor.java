package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.event.SyncConfigEvent;
import eu.bcvsolutions.idm.acc.event.SyncConfigEvent.SyncConfigEventType;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Processor remove all handled attributes for {@link SysSystemMappingDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component("accSystemMappingDeleteProcessor")
@Description("Remove all handled attributes. Ensures referential integrity. Cannot be disabled.")
public class SystemMappingDeleteProcessor extends CoreEventProcessor<SysSystemMappingDto> {

	private static final String PROCESSOR_NAME = "system-mapping-delete-processor";
	
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysRoleSystemService roleSystemService;
	private final SysSystemMappingService systemMappingService;
	private final SysSyncConfigService syncConfigService;
	
	@Autowired
	public SystemMappingDeleteProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysRoleSystemService roleSystemService,
			SysSystemMappingService systemMappingService,
			SysSyncConfigService configService) {
		super(SystemMappingEventType.DELETE);
		//
		Assert.notNull(roleSystemService, "Service is required.");
		Assert.notNull(systemAttributeMappingService, "Service is required.");
		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(configService, "Service is required.");
		//
		this.roleSystemService = roleSystemService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.systemMappingService = systemMappingService;
		this.syncConfigService = configService;
	}

	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto systemMapping = event.getContent();
		UUID systemMappingId = systemMapping.getId();
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		//
		List<AbstractSysSyncConfigDto> syncConfigs = syncConfigService.findRoleConfigBySystemMapping(systemMappingId);
		if (!forceDelete) {
			if (syncConfigService.countBySystemMapping(systemMapping) > 0) {
				SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMapping, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
				SysSystemDto systemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
				
				throw new ResultCodeException(AccResultCode.SYSTEM_MAPPING_DELETE_FAILED_USED_IN_SYNC,
						ImmutableMap.of("mapping", systemMapping.getName(),"system", systemDto.getName()));
			}
			if (syncConfigs.size() > 0){
				SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(syncConfigs.get(0), SysSyncRoleConfig_.systemMapping, SysSystemMappingDto.class);
				SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
				SysSystemDto systemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
	
				throw new ResultCodeException(AccResultCode.SYSTEM_MAPPING_DELETE_FAILED_USED_IN_SYNC,
						ImmutableMap.of("mapping", systemMapping.getName(), "system", systemDto.getName()));
			}
		} else {
			SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
			syncFilter.setSystemMappingId(systemMappingId);
			syncConfigService.find(syncFilter, null).forEach(syncConfig -> {
				SyncConfigEvent syncConfigEvent = new SyncConfigEvent(SyncConfigEventType.DELETE, syncConfig);
				//
				syncConfigService.publish(syncConfigEvent, event);
			});
			//
			syncConfigs.forEach(syncConfig -> {
				SyncConfigEvent syncConfigEvent = new SyncConfigEvent(SyncConfigEventType.DELETE, syncConfig);
				//
				syncConfigService.publish(syncConfigEvent, event);
			});
		}
		//
		// remove all handled attributes
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping.getId());
		systemAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systemAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		// delete mapped roles
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setSystemMappingId(systemMapping.getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		// remove relation from connected mapping
		if (systemMapping.getConnectedSystemMappingId() != null) {
			SysSystemMappingDto sysSystemMappingDto = systemMappingService.get(systemMapping.getConnectedSystemMappingId());
			sysSystemMappingDto.setConnectedSystemMappingId(null);
			systemMappingService.saveInternal(sysSystemMappingDto);
		}
		//
		systemMappingService.deleteInternal(systemMapping);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}

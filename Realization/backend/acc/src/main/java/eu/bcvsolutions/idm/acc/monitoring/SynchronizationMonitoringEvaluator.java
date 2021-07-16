package eu.bcvsolutions.idm.acc.monitoring;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Synchronization monitoring.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(SynchronizationMonitoringEvaluator.NAME)
@Description("Synchronization monitoring.")
public class SynchronizationMonitoringEvaluator extends AbstractMonitoringEvaluator {
	
	public static final String NAME = "acc-synchronization-monitoring-evaluator";
	public static final String PARAMETER_SYNCHRONIZATION = "synchronization";
	//
	@Autowired @Lazy private SysSyncConfigService syncConfigService;
	@Autowired @Lazy private SysSystemService systemService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		UUID synchronizationId = getParameterConverter().toUuid(monitoring.getEvaluatorProperties(), PARAMETER_SYNCHRONIZATION);
		AbstractSysSyncConfigDto sync = syncConfigService.get(synchronizationId, context);
		if (sync == null) {
			throw new EntityNotFoundException(SysSyncConfig.class, synchronizationId);
		}
		//
		SysSystemMappingDto mapping = DtoUtils.getEmbedded(sync,
				SysSyncConfig_.systemMapping.getName(),
				SysSystemMappingDto.class);
		SysSchemaObjectClassDto schema = DtoUtils.getEmbedded(mapping,
				SysSystemMapping_.objectClass.getName(),
				SysSchemaObjectClassDto.class);
		SysSystemDto system = systemService.get(schema.getSystem());
		//
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setOwnerId(getLookupService().getOwnerId(sync));
		result.setOwnerType(getLookupService().getOwnerType(SysSyncConfig.class));
		ResultModel resultModel;
		SysSyncLogDto lastSyncLog = sync.getLastSyncLog();
		if (!sync.isEnabled()) {
			resultModel = new DefaultResultModel(
					AccResultCode.MONITORING_SYNCHRONIZATION_DISABLED,
					ImmutableMap.of(
							"synchronizationName", sync.getName(),
							"systemName", system.getName()
					)
			);
		} else if(lastSyncLog != null) {
			result.setOwnerId(getLookupService().getOwnerId(lastSyncLog));
			result.setOwnerType(getLookupService().getOwnerType(lastSyncLog));
			//
			// count error and other (~success) operations
			int errorCounter = 0;
			int otherCounter = 0;
			for (SysSyncActionLogDto action : lastSyncLog.getSyncActionLogs()) {
				if (action.getOperationResult() == OperationResultType.ERROR) {
					errorCounter += action.getOperationCount();
				} else {
					otherCounter += action.getOperationCount();
				}
			}
			//
			if (sync.getLastSyncLog().isContainsError() || errorCounter > 0) {
				result.setValue(String.valueOf(errorCounter));
				resultModel = new DefaultResultModel(
						AccResultCode.MONITORING_SYNCHRONIZATION_CONTAINS_ERROR,
						ImmutableMap.of(
								"synchronizationName", sync.getName(),
								"systemName", system.getName(),
								"count", errorCounter
						)
				);
			} else {
				result.setValue(String.valueOf(otherCounter));
				resultModel = new DefaultResultModel(
						AccResultCode.MONITORING_SYNCHRONIZATION_OK,
						ImmutableMap.of(
								"synchronizationName", sync.getName(),
								"systemName", system.getName(),
								"count", otherCounter
						)
				);
			}
		} else {
			resultModel = new DefaultResultModel(
					AccResultCode.MONITORING_SYNCHRONIZATION_NOT_EXECUTED,
					ImmutableMap.of(
							"synchronizationName", sync.getName(),
							"systemName", system.getName()
					)
			);
		}
		//
		result.setResult(
				new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build()
		);
		//
		return result;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_SYNCHRONIZATION);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return Lists.newArrayList(
				getSynchronizationAttribute()
		);
	}
	
	private IdmFormAttributeDto getSynchronizationAttribute() {
		IdmFormAttributeDto synchronization = new IdmFormAttributeDto(PARAMETER_SYNCHRONIZATION, PARAMETER_SYNCHRONIZATION, PersistentType.UUID);
		synchronization.setFaceType(AccFaceType.SYNCHRONIZATION_CONFIG_SELECT);
		synchronization.setRequired(true);
		//
		return synchronization;
	}
	
	@Override
	public IdmFormInstanceDto getFormInstance(ConfigurationMap evaluatorProperties) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(getFormDefinition());
		//
		UUID synchronizationId = getParameterConverter().toUuid(evaluatorProperties, PARAMETER_SYNCHRONIZATION);
		if (synchronizationId == null) {
			return null;
		}
		IdmFormValueDto value = new IdmFormValueDto(getSynchronizationAttribute());
		value.setUuidValue(synchronizationId);
		//
		AbstractSysSyncConfigDto sync = syncConfigService.get(synchronizationId);
		if (sync == null) {
			// id only => prevent to load on UI
			// TODO: load from audit => #978 required
			sync = new SysSyncConfigDto(synchronizationId);
		}
		value.getEmbedded().put(IdmFormValueDto.PROPERTY_UUID_VALUE, sync);
		formInstance.getValues().add(value);
		//
		return formInstance;
	}
}

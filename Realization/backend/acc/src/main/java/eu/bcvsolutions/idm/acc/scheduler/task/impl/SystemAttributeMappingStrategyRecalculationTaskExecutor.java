package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for setting strategy type based on attribute mapping into all
 * overloaded places (roles)
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
@Component(SystemAttributeMappingStrategyRecalculationTaskExecutor.TASK_NAME)
public class SystemAttributeMappingStrategyRecalculationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	public static final String TASK_NAME = "acc-system-attribute-strategy-recalculation-long-running-task";

	private static final Logger LOG = LoggerFactory.getLogger(SystemAttributeMappingStrategyRecalculationTaskExecutor.class);

	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;

	private UUID systemId;
	private UUID mappingId;
	private List<UUID> attributeIds;

	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);

		MultiValueMap<String, UUID> params = getParameterConverter().toSystemMappingAttribute(properties, CoreResultCode.LONG_RUNNING_TASK_WRONG_CONFIGURATION);
		systemId = params.getFirst(ParameterConverter.PARAMETER_SYSTEM);
		mappingId = params.getFirst(ParameterConverter.PARAMETER_SYSTEM_MAPPING);
		attributeIds = params.get(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES);
	}

	@Override
	public Boolean process() {
		this.counter = 0L;
		this.count = 0L;
		for (UUID uuid : attributeIds) {
			SysSystemAttributeMappingDto systemAttributeMappingDto = systemAttributeMappingService.get(uuid);

			SysRoleSystemAttributeFilter roleSystemAttributeFilter = new SysRoleSystemAttributeFilter();
			roleSystemAttributeFilter.setSystemId(systemId);
			roleSystemAttributeFilter.setSystemMappingId(mappingId);
			roleSystemAttributeFilter.setSystemAttributeMappingId(uuid);
			List<SysRoleSystemAttributeDto> roleSystemAttributeDtos = roleSystemAttributeService.find(roleSystemAttributeFilter, null).getContent();

			this.count += roleSystemAttributeDtos.size();

			for (SysRoleSystemAttributeDto roleSystemAttributeDto : roleSystemAttributeDtos) {
				SysRoleSystemDto roleSystemDto = DtoUtils.getEmbedded(roleSystemAttributeDto, SysRoleSystemAttribute_.roleSystem, SysRoleSystemDto.class);

				AttributeMappingStrategyType roleAttributeStrategy = roleSystemAttributeDto.getStrategyType();
				AttributeMappingStrategyType systemAttributeMappingStrategy = systemAttributeMappingDto.getStrategyType();

				if (!roleAttributeStrategy.equals(systemAttributeMappingStrategy)) {
					roleSystemAttributeDto.setStrategyType(systemAttributeMappingStrategy);
					roleSystemAttributeService.save(roleSystemAttributeDto);

					DefaultResultModel model = new DefaultResultModel(AccResultCode.SYSTEM_ATTRIBUTE_MAPPING_RECALCULATION_EXECUTED,
							String.format("Strategy type for role %s, for attribute %s changed from %s to %s", roleSystemDto.getRole(), roleSystemAttributeDto.getName(),
									roleAttributeStrategy, systemAttributeMappingStrategy));
					logItemProcessed(roleSystemAttributeDto, new OperationResult.Builder(OperationState.EXECUTED).setModel(model).build());
				} else {
					DefaultResultModel model = new DefaultResultModel(AccResultCode.SYSTEM_ATTRIBUTE_MAPPING_RECALCULATION_NOT_EXECUTED,
							String.format("Strategy type for role %s, for attribute %s is same as the one in system mapping", roleSystemDto.getRole(), roleSystemAttributeDto.getName()));
					logItemProcessed(roleSystemAttributeDto, new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(model).build());
				}
				this.counter++;
				if (stopLrt()) {
					break;
				}
			}
			if (stopLrt()) {
				break;
			}
		}
		return Boolean.TRUE;
	}

	private boolean stopLrt() {
		boolean canContinue = updateState();
		return !canContinue;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();

		IdmFormAttributeDto mappingAttribute = new IdmFormAttributeDto(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES, ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES,
				PersistentType.TEXT);
		mappingAttribute.setRequired(true);
		mappingAttribute.setFaceType(AccFaceType.SYSTEM_MAPPING_ATTRIBUTE_FILTERED_SELECT);
		formAttributes.add(mappingAttribute);

		return formAttributes;
	}

	@Override
	public boolean isRecoverable() {
		return true;
	}

}

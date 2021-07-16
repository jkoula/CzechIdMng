package eu.bcvsolutions.idm.acc.monitoring;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Monitoring evaluator tests.
 * 
 * @author Radek Tomiška
 *
 */
public class SynchronizationMonitoringEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Mock private SysSyncConfigService syncConfigService;
	@Mock private SysSystemService systemService;
	@Mock private LookupService lookupService;
	//
	@InjectMocks private SynchronizationMonitoringEvaluator evaluator;
	
	@Test
	public void testEvaluatorProperties() {
		Assert.assertNotNull(evaluator.getName());
		Assert.assertFalse(evaluator.getPropertyNames().isEmpty());
		Assert.assertFalse(evaluator.getFormAttributes().isEmpty());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testSynchronizationNotFound() {
		UUID syncId = UUID.randomUUID();
		Mockito.when(syncConfigService.get(syncId, new SysSyncConfigFilter())).thenReturn(null);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		evaluator.evaluate(monitoring);
	}

	@Test
	public void testSynchronizationNotRun() {
		UUID syncId = UUID.randomUUID();
		SysSyncConfigDto config = new SysSyncConfigDto();
		config.setName("mock");
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		config.getEmbedded().put(SysSyncConfig_.systemMapping.getName(), mapping);
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setSystem(syncId);
		mapping.getEmbedded().put(SysSystemMapping_.objectClass.getName(), schema);
		SysSystemDto system = new SysSystemDto();
		system.setName("mock");
		//
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		Mockito.when(syncConfigService.get(ArgumentMatchers.any(), (SysSyncConfigFilter) ArgumentMatchers.any())).thenReturn(config);
		Mockito.when(systemService.get(syncId)).thenReturn(system);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_SYNCHRONIZATION_NOT_EXECUTED.getCode(), result.getResult().getCode());
	}
	
	@Test
	public void testSynchronizationNotEnabled() {
		UUID syncId = UUID.randomUUID();
		SysSyncConfigDto config = new SysSyncConfigDto();
		config.setEnabled(false);
		config.setName("mock");
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		config.getEmbedded().put(SysSyncConfig_.systemMapping.getName(), mapping);
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setSystem(syncId);
		mapping.getEmbedded().put(SysSystemMapping_.objectClass.getName(), schema);
		SysSystemDto system = new SysSystemDto();
		system.setName("mock");
		//
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		Mockito.when(syncConfigService.get(ArgumentMatchers.any(), (SysSyncConfigFilter) ArgumentMatchers.any())).thenReturn(config);
		Mockito.when(systemService.get(syncId)).thenReturn(system);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_SYNCHRONIZATION_DISABLED.getCode(), result.getResult().getCode());
	}
	
	@Test
	public void testSynchronizationOk() {
		UUID syncId = UUID.randomUUID();
		SysSyncConfigDto config = new SysSyncConfigDto();
		config.setName("mock");
		config.setLastSyncLog(new SysSyncLogDto());
		SysSyncActionLogDto action = new SysSyncActionLogDto();
		action.setOperationResult(OperationResultType.SUCCESS);
		action.setOperationCount(2);
		config.getLastSyncLog().getSyncActionLogs().add(action);
		
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		config.getEmbedded().put(SysSyncConfig_.systemMapping.getName(), mapping);
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setSystem(syncId);
		mapping.getEmbedded().put(SysSystemMapping_.objectClass.getName(), schema);
		SysSystemDto system = new SysSystemDto();
		system.setName("mock");
		//
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		Mockito.when(syncConfigService.get(ArgumentMatchers.any(), (SysSyncConfigFilter) ArgumentMatchers.any())).thenReturn(config);
		Mockito.when(systemService.get(syncId)).thenReturn(system);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_SYNCHRONIZATION_OK.getCode(), result.getResult().getCode());
		Assert.assertEquals(String.valueOf(2), result.getValue());
	}
	
	@Test
	public void testSynchronizationNotOk() {
		UUID syncId = UUID.randomUUID();
		SysSyncConfigDto config = new SysSyncConfigDto();
		config.setName("mock");
		config.setLastSyncLog(new SysSyncLogDto());
		SysSyncActionLogDto action = new SysSyncActionLogDto();
		action.setOperationResult(OperationResultType.ERROR);
		action.setOperationCount(2);
		config.getLastSyncLog().getSyncActionLogs().add(action);
		
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		config.getEmbedded().put(SysSyncConfig_.systemMapping.getName(), mapping);
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setSystem(syncId);
		mapping.getEmbedded().put(SysSystemMapping_.objectClass.getName(), schema);
		SysSystemDto system = new SysSystemDto();
		system.setName("mock");
		//
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		Mockito.when(syncConfigService.get(ArgumentMatchers.any(), (SysSyncConfigFilter) ArgumentMatchers.any())).thenReturn(config);
		Mockito.when(systemService.get(syncId)).thenReturn(system);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_SYNCHRONIZATION_CONTAINS_ERROR.getCode(), result.getResult().getCode());
		Assert.assertEquals(String.valueOf(2), result.getValue());
	}
	
	@Test
	public void testSynchronizationContainsError() {
		UUID syncId = UUID.randomUUID();
		SysSyncConfigDto config = new SysSyncConfigDto();
		config.setName("mock");
		config.setLastSyncLog(new SysSyncLogDto());
		SysSyncActionLogDto action = new SysSyncActionLogDto();
		action.setOperationResult(OperationResultType.SUCCESS);
		action.setOperationCount(2);
		config.getLastSyncLog().getSyncActionLogs().add(action);
		config.getLastSyncLog().setContainsError(true);
		
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		config.getEmbedded().put(SysSyncConfig_.systemMapping.getName(), mapping);
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setSystem(syncId);
		mapping.getEmbedded().put(SysSystemMapping_.objectClass.getName(), schema);
		SysSystemDto system = new SysSystemDto();
		system.setName("mock");
		//
		SysSyncConfigFilter context = new SysSyncConfigFilter();
		context.setIncludeLastLog(Boolean.TRUE);
		Mockito.when(syncConfigService.get(ArgumentMatchers.any(), (SysSyncConfigFilter) ArgumentMatchers.any())).thenReturn(config);
		Mockito.when(systemService.get(syncId)).thenReturn(system);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, syncId);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_SYNCHRONIZATION_CONTAINS_ERROR.getCode(), result.getResult().getCode());
		Assert.assertEquals(String.valueOf(0), result.getValue()); // ~ flag only
	}
	
	@Test
	public void testFormInstance() {
		ConfigurationMap properties = new ConfigurationMap();
		//
		Assert.assertNull(evaluator.getFormInstance(properties));
		//
		UUID synchronizationId = UUID.randomUUID();
		properties.put(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, synchronizationId);
		//
		IdmFormInstanceDto formInstance = evaluator.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(synchronizationId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
		//
		Mockito.when(syncConfigService.get(synchronizationId)).thenReturn(new SysSyncConfigDto(synchronizationId));
		formInstance = evaluator.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(synchronizationId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
	}
}

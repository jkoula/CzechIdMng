package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.MonitoringEvaluatorDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent.MonitoringEventType;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult_;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Default monitoring manager.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Service("monitoringManager")
public class DefaultMonitoringManager implements MonitoringManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultMonitoringManager.class);
	//
	@Autowired private ApplicationContext context;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private IdmMonitoringResultService monitoringResultService;
	@Autowired private IdmCacheManager cacheManager;
	@Autowired private ConfigurationService configurationService;
	@Autowired private EnabledEvaluator enabledEvaluator;
	//
	// evaluators cache
	private final Map<String, MonitoringEvaluator> evaluators = new HashMap<>();
	private static boolean firstExecuted = false;
	
	/**
	 * Init last result cache, when instance is started
	 */
	@Override
	public void init() {
		String instanceId = configurationService.getInstanceId();
		int counter = 0;
		//
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setDisabled(Boolean.FALSE);
		filter.setInstanceId(instanceId);
		for (IdmMonitoringDto monitoring : monitoringService.find(filter, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmMonitoring_.seq.getName())))) {
			if (getLastResult(monitoring.getId()) != null) {
				counter++;
			}
		}
		//
		LOG.info("Last monitoring results [{}] on instance [{}] loaded into cache.", counter, instanceId);
	}
	
	/**
	 * Spring schedule run all registered monitoring evaluators
	 */
	@Scheduled(fixedDelay = 10000)
	public void scheduleExecute() {
		String instanceId = configurationService.getInstanceId();
		LOG.debug("Processing monitoring evaluators on instance id [{}]", instanceId);
		//
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setDisabled(Boolean.FALSE);
		filter.setInstanceId(instanceId);
		//
		List<IdmMonitoringDto> monitorings = monitoringService
				.find(filter, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmMonitoring_.seq.getName())))
				.getContent();
		//
		for (IdmMonitoringDto monitoring : monitorings) {
			UUID monitoringId = monitoring.getId();
			//
			// check execute date
			ZonedDateTime executeDate = monitoring.getExecuteDate();
			if (executeDate != null && ZonedDateTime.now().isBefore(executeDate)) {
				LOG.trace("Monitoring evaluator [{}] will be evaluted after [{}].", monitoringId, executeDate);
				//
				continue; 
			}
			//
			// check last result is in period
			Long checkPeriod = monitoring.getCheckPeriod(); // seconds
			if (checkPeriod == null) {
				checkPeriod = 0L;
			}
			//
			IdmMonitoringResultDto lastResult = getLastResult(monitoringId);
			if (lastResult != null) {
				if (checkPeriod == 0 && firstExecuted) {
					LOG.trace("Monitoring evaluator [{}] was already executed.", monitoringId);
					//
					continue; 
				}
				//
				if (lastResult.getCreated().isAfter(ZonedDateTime.now().minusSeconds(checkPeriod))) {
					LOG.trace("Monitoring evaluator [{}] was already executed.", monitoringId);
					//
					continue;
				}
			}
			//
			execute(monitoring);
		}
		//
		firstExecuted = true;
	}
	
	@Override
	public IdmMonitoringResultDto execute(IdmMonitoringDto monitoring, BasePermission... permission) {
		Assert.notNull(monitoring, "Monitoring is required.");
		//
		EventContext<IdmMonitoringDto> context = monitoringService.publish(new MonitoringEvent(MonitoringEventType.EXECUTE, monitoring), permission);
		// with result
		for (EventResult<IdmMonitoringDto> eventResult : context.getResults()) {
			for (OperationResult operationResult : eventResult.getResults()) {
				ResultModel model = operationResult.getModel();
				if (model != null 
						&& CoreResultCode.MONITORING_RESULT.getCode().equals(model.getStatusEnum())
						&& model.getParameters().containsKey(EventResult.EVENT_PROPERTY_RESULT)) {
					return (IdmMonitoringResultDto) model.getParameters().get(EventResult.EVENT_PROPERTY_RESULT);
				}
			}
		}
		// without result (just for sure - e.g. wrong exception handling in custom monitoring evaluator)
		return null;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		Assert.notNull(monitoring, "Monitoring is required.");
		UUID monitoringId = monitoring.getId();
		Assert.notNull(monitoringId, "Persisted monitoring is required.");
		//
		MonitoringEvaluator evaluator = getEvaluator(monitoring);
		if (evaluator == null) {
			LOG.warn("Monitoring evaluator for motitoring configuration [{}] not exists.", monitoringId);
			return null;
		}
		//
		ZonedDateTime monitoringStarted = ZonedDateTime.now();
		IdmMonitoringResultDto result = null;
		try {
			result = evaluator.evaluate(monitoring);
		} catch (AcceptedException ex) {
			result = new IdmMonitoringResultDto(
					new OperationResultDto.Builder(OperationState.RUNNING).setException(ex).build()
			);
			result.setLevel(NotificationLevel.INFO);
		} catch (ResultCodeException ex) {
			result = new IdmMonitoringResultDto(
					new OperationResultDto.Builder(OperationState.EXCEPTION).setException(ex).build()
			);
			result.setLevel(NotificationLevel.ERROR);
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				result = new IdmMonitoringResultDto(new OperationResultDto.Builder(OperationState.EXCEPTION) //
						.setException((ResultCodeException) resolvedException) //
						.build()
				);
				result.setLevel(NotificationLevel.ERROR);
			} else {
				result = new IdmMonitoringResultDto(
						new OperationResultDto.Builder(OperationState.EXCEPTION).setCause(ex).build()
				);
			}
		}
		//
		if (result != null) {
			result.setMonitoring(monitoringId);
			result.setEvaluatorType(monitoring.getEvaluatorType());
			result.setProcessedOrder(monitoring.getSeq());
			result.setEvaluatorProperties(monitoring.getEvaluatorProperties());
			result.setInstanceId(monitoring.getInstanceId());
			// default result state
			if (result.getResult() == null) {
				result.setResult(new OperationResultDto(OperationState.EXECUTED));
			}
			// default level by result model
			if (result.getLevel() == null) {
				ResultModel resultModel = result.getResult().getModel();
				if (resultModel != null) {
					result.setLevel(resultModel.getLevel());
				}
			}
			//
			result.setMonitoringStarted(monitoringStarted);
			result.setMonitoringEnded(ZonedDateTime.now());
			result.setLastResult(true);
			// reset last result flag
			monitoringResultService.resetLastResult(monitoringId);
			// save new last result
			result = monitoringResultService.save(result);
			//
			cacheManager.cacheValue(LAST_RESULT_CACHE_NAME, monitoringId, result);
		} else {
			LOG.debug("Monitoring [{}] ended without result - result will not be persisted.", monitoringId);
		}
		//
		return result;
	}
	
	@Override
	public List<MonitoringEvaluatorDto> getSupportedEvaluators() {
		// TODO: sort + cache
		return context
			.getBeansOfType(MonitoringEvaluator.class)
			.values()
			.stream()
			.filter(enabledEvaluator::isEnabled)
			.map(evaluator -> {
				MonitoringEvaluatorDto evaluatorDto = new MonitoringEvaluatorDto();
				evaluatorDto.setId(evaluator.getId());
				evaluatorDto.setName(evaluator.getName());
				evaluatorDto.setEvaluatorType(AutowireHelper.getTargetType(evaluator));
				evaluatorDto.setModule(evaluator.getModule());
				evaluatorDto.setDescription(evaluator.getDescription());
				evaluatorDto.setFormDefinition(evaluator.getFormDefinition());
				evaluatorDto.setDisabled(evaluator.isDisabled()); // TODO: disabled evaluator
				//
				return evaluatorDto;
			})
			.collect(Collectors.toList());
	}
	
	@Override
	public Page<IdmMonitoringDto> findMonitorings(IdmMonitoringFilter filter, Pageable pageable, BasePermission... permission) {
		return monitoringService.find(filter, pageable, permission);
	}
	
	@Override
	public Page<IdmMonitoringResultDto> getLastResults(IdmMonitoringResultFilter filter, Pageable pageable, BasePermission... permission) {
		// all instances => last results should be visible on each instance
		IdmMonitoringFilter monitoringFilter = new IdmMonitoringFilter();
		if (filter != null) {
			monitoringFilter.setId(filter.getMonitoring());
		}
		monitoringFilter.setDisabled(Boolean.FALSE);
		//
		List<IdmMonitoringDto> monitorings = monitoringService
			.find(
					monitoringFilter,
					PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmMonitoring_.seq.getName())),
					PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.AUTOCOMPLETE
			)
			.getContent();
		//
		// last results sorted by monitoring order
		List<IdmMonitoringResultDto> results = new ArrayList<>(monitorings.size());
		for (IdmMonitoringDto monitoring : monitorings) {
			MonitoringEvaluator evaluator = getEvaluator(monitoring);
			if (evaluator == null) {
				LOG.debug("Monitoring evaluator for motitoring configuration [{}] not exists.", monitoring.getId());
				continue;
			}
			
			IdmMonitoringResultDto lastResult = getLastResult(monitoring.getId(), permission);
			if (lastResult == null) {
				continue;
			}
			// filter by level 
			NotificationLevel lastResultLevel = lastResult.getLevel();
			List<NotificationLevel> levels = filter == null ? null : filter.getLevels();
			if (CollectionUtils.isNotEmpty(levels)  && !levels.contains(lastResultLevel)) {
				continue;
			}
			lastResult.setTrimmed(true);
			results.add(lastResult);
		}
		//
		// pageable is required internally
		Pageable internalPageable;
		if (pageable == null) {
			internalPageable = PageRequest.of(0, Integer.MAX_VALUE);
		} else {
			internalPageable = pageable;
		}
		//
		// Sort by level desc
		results.sort((r1, r2) -> { return ObjectUtils.compare(r2.getLevel(), r1.getLevel()); });
		//
		// "naive" pagination
		int first = internalPageable.getPageNumber() * internalPageable.getPageSize();
		int last = internalPageable.getPageSize() + first;
		List<IdmMonitoringResultDto> page = results.subList(
				first < results.size() ? first : results.size() > 0 ? results.size() - 1 : 0, 
				last < results.size() ? last : results.size()
		);
		//
		return new PageImpl<>(page, internalPageable, results.size());
	}
	
	@Override
	public IdmFormInstanceDto getEvaluatorFormInstance(IdmMonitoringResultDto monitoringResult) {
		MonitoringEvaluator evaluator = getEvaluator(monitoringResult.getEvaluatorType());
		if (evaluator == null) {
			return null;
		}
		IdmFormInstanceDto formInstance = evaluator.getFormInstance(monitoringResult.getEvaluatorProperties());
		if (formInstance == null) {
			return null;
		}
		//
		formInstance.setOwnerId(monitoringResult.getId());
		formInstance.setOwnerType(monitoringResult.getClass());
		//
		return formInstance;
	}
	
	@Override
	public IdmFormInstanceDto getEvaluatorFormInstance(IdmMonitoringDto monitoring) {
		MonitoringEvaluator evaluator = getEvaluator(monitoring.getEvaluatorType());
		if (evaluator == null) {
			return null;
		}
		IdmFormInstanceDto formInstance = evaluator.getFormInstance(monitoring.getEvaluatorProperties());
		if (formInstance == null) {
			return null;
		}
		//
		formInstance.setOwnerId(monitoring.getId());
		formInstance.setOwnerType(monitoring.getClass());
		//
		return formInstance;
	}

	private MonitoringEvaluator getEvaluator(IdmMonitoringDto monitoring) {
		String evaluatorType = monitoring.getEvaluatorType();
		MonitoringEvaluator evaluator = getEvaluator(evaluatorType);
		//
		if (evaluator == null) {
			LOG.info("Evaluator type [{}] for monitoring [{}] not found - monitoring will not be evaluated.", evaluatorType, monitoring.getId());
		}
		//
		return evaluator;
	}
	
	private MonitoringEvaluator getEvaluator(String evaluatorType) {
		if (!evaluators.containsKey(evaluatorType)) {
			try {
				evaluators.put(evaluatorType, (MonitoringEvaluator) context.getBean(Class.forName(evaluatorType)));
			} catch (ClassNotFoundException | NoSuchBeanDefinitionException ex) {
				// disable or removed evaluator classes
				LOG.warn("Evaluator type [{}] not found. Monitoring is ignored but should be disabled or removed.", evaluatorType);
				return null;
			}
		}
		//
		MonitoringEvaluator evaluator = evaluators.get(evaluatorType);
		//
		if (evaluator.isDisabled() || !enabledEvaluator.isEnabled(evaluator)) {
			LOG.info("Evaluator type [{}] is disabled - monitoring will not be evaluated.", evaluatorType);
			return null;
		}
		//
		return evaluator;
	}
	
	/**
	 * Find last monitoring result.
	 * 
	 * @param monitoringId monitoring evaluator identifier
	 * @return last result
	 */
	private IdmMonitoringResultDto getLastResult(UUID monitoringId, BasePermission... permission) {
		// try to get cached value
		ValueWrapper value = cacheManager.getValue(LAST_RESULT_CACHE_NAME, monitoringId);
		if (value != null) {
			return monitoringResultService.checkAccess((IdmMonitoringResultDto) value.get(), permission);
		}
		// or load from database
		IdmMonitoringResultFilter resultFilter = new IdmMonitoringResultFilter();
		resultFilter.setMonitoring(monitoringId);
		List<IdmMonitoringResultDto> monitoringResults = monitoringResultService
			.find(resultFilter, PageRequest.of(0, 1, Sort.by(Direction.DESC, IdmMonitoringResult_.created.getName())), permission)
			.getContent();
		if (monitoringResults.isEmpty()) {
			cacheManager.cacheValue(LAST_RESULT_CACHE_NAME, monitoringId, null); // null => prevent to load again
			//
			return null;
		}
		//
		IdmMonitoringResultDto lastResult = monitoringResults.get(0);
		cacheManager.cacheValue(LAST_RESULT_CACHE_NAME, monitoringId, lastResult);
		//
		return lastResult;
	}
}

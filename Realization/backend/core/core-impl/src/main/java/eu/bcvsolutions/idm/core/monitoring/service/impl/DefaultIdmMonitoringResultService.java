package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult_;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring_;
import eu.bcvsolutions.idm.core.monitoring.repository.IdmMonitoringResultRepository;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * CRUD for monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Service("monitoringResultService")
public class DefaultIdmMonitoringResultService 
		extends AbstractEventableDtoService<IdmMonitoringResultDto, IdmMonitoringResult, IdmMonitoringResultFilter> 
		implements IdmMonitoringResultService {
	
	@Autowired
	public DefaultIdmMonitoringResultService(
			IdmMonitoringResultRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(MonitoringGroupPermission.MONITORINGRESULT, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmMonitoringResult> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmMonitoringResultFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmMonitoringResult_.monitoring).get(IdmMonitoring_.evaluatorType)), "%" + text + "%"),
					builder.like(builder.lower(root.get(IdmMonitoringResult_.monitoring).get(IdmMonitoring_.description)), "%" + text + "%")
			));
		}
		//
		UUID monitoring = filter.getMonitoring();
		if (monitoring != null) {
			predicates.add(builder.equal(root.get(IdmMonitoringResult_.monitoring).get(IdmMonitoring_.id), monitoring));
		}
		List<NotificationLevel> levels = filter.getLevels();
		if (CollectionUtils.isNotEmpty(levels)) {
			predicates.add(root.get(IdmMonitoringResult_.level).in(levels));
		}
		//
		return predicates;
	}
}

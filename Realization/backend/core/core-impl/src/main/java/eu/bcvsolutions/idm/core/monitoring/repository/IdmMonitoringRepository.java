package eu.bcvsolutions.idm.core.monitoring.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring;

/**
 * Repository for configured monitoring evaluators.
 * 
 * @author Radek Tomiška 
 * @since 11.1.0
 */
public interface IdmMonitoringRepository extends AbstractEntityRepository<IdmMonitoring> {

	/**
	 * Find monitoring evaluator by its code.
	 * 
	 * @param code unique code
	 * @return monitoring evaluator
	 * @since 11.2.0
	 */
	@Query(value = "select e from #{#entityName} e where e.code = :code")
	IdmMonitoring findOneByCode(@Param("code") String code);
}

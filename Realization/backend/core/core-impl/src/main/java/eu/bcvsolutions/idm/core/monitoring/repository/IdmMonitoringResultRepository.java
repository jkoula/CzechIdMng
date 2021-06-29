package eu.bcvsolutions.idm.core.monitoring.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult;

/**
 * Repository for monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface IdmMonitoringResultRepository extends AbstractEntityRepository<IdmMonitoringResult> {
	
	/**
	 * Delete results by monitoring evaluator.
	 * 
	 * @param monitoringId monitoring evaluator identifier
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.monitoring.id = :monitoringId")
	int deleteByMonitoring(@Param("monitoringId") UUID monitoringId);
}

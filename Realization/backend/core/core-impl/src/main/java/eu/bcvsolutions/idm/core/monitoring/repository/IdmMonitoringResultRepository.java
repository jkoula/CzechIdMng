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
	 * @return count of deleted records
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.monitoring.id = :monitoringId")
	int deleteByMonitoring(@Param("monitoringId") UUID monitoringId);
	
	/**
	 * Reset last monitoring result flag to given value.
	 * 
	 * @param monitoringId monitoring evaluator identifier
	 * @param lastResult last result value - mainly false
	 * @return count of updated records
	 * @since 11.2.0
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update #{#entityName} e set e.lastResult = :lastResult where e.monitoring.id = :monitoringId")
	int resetLastResult(@Param("monitoringId") UUID monitoringId, @Param("lastResult") boolean lastResult);
}

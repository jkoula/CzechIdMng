package eu.bcvsolutions.idm.core.monitoring.service.impl;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayMigrationStrategy;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;

/**
 * Warning about H2 database usage => H2 database is not supposed to be used for production environment.
 * 
 * Production - error level
 * Other - warning level
 * Value - currently used database
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(H2DatabaseMonitoringEvaluator.NAME)
@Description("Warning about H2 database usage => H2 database is not supposed to be used for production environment.")
public class H2DatabaseMonitoringEvaluator extends AbstractMonitoringEvaluator {

	public static final String NAME = "core-h-2-database-monitoring-evaluator";
	@Autowired private DataSource dataSource;
	@Autowired private IdmFlywayMigrationStrategy flywayMigrationStrategy;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		ResultModel resultModel;
		//
		String resolvedDbName = flywayMigrationStrategy.resolveDbName(dataSource);
		if (IdmFlywayMigrationStrategy.H2_DBNAME.equals(resolvedDbName)) {
			// TODO: ApplicationConfiguration - stage development
			String stage = configurationService.getValue("idm.pub.app.stage");
			// stage = "production";
			if (StringUtils.isBlank(stage) || "production".equalsIgnoreCase(stage)) {
				resultModel = new DefaultResultModel(
						CoreResultCode.MONITORING_H2_DATABASE_ERROR,
						ImmutableMap.of(
								"instanceId", monitoring.getInstanceId()
						)
				);
			} else {
				resultModel = new DefaultResultModel(
						CoreResultCode.MONITORING_H2_DATABASE_WARNING,
						ImmutableMap.of(
								"instanceId", monitoring.getInstanceId(),
								"stage", stage
						)
				);
			}
		} else {
			resultModel = new DefaultResultModel(
					CoreResultCode.MONITORING_H2_DATABASE_SUCCESS,
					ImmutableMap.of(
							"instanceId", monitoring.getInstanceId(),
							"databaseName", resolvedDbName
					)
			);
		}
		//
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setValue(resolvedDbName);
		result.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build());
		//
		return result;
	}
}

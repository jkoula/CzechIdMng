package eu.bcvsolutions.idm.core.monitoring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitAdminIdentityProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Warning about demo admin credentials are used.
 * 
 * Development - warning level
 * Other - error level
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(DemoAdminMonitoringEvaluator.NAME)
@Description("Warning about demo admin credentials are used.")
public class DemoAdminMonitoringEvaluator extends AbstractMonitoringEvaluator {

	public static final String NAME = "core-demo-admin-monitoring-evaluator";
	//
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public String getName() {
		return NAME;
	}
		
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		ResultModel resultModel;
		//
		IdmIdentityDto adminIdentity = getLookupService().lookupDto(IdmIdentityDto.class, InitAdminIdentityProcessor.ADMIN_USERNAME);
		if (adminIdentity == null) {
			resultModel = new DefaultResultModel(CoreResultCode.MONITORING_DEMO_ADMIN_NOT_FOUND);
		} else {
			result.setOwnerId(getLookupService().getOwnerId(adminIdentity));
			result.setOwnerType(getLookupService().getOwnerType(adminIdentity));
			//
			LoginDto loginDto = new LoginDto();
			loginDto.setUsername(adminIdentity.getUsername());
			loginDto.setPassword(new GuardedString(InitAdminIdentityProcessor.ADMIN_PASSWORD));
			//
			if (authenticationManager.validate(loginDto)) {
				resultModel = new DefaultResultModel(CoreResultCode.MONITORING_DEMO_ADMIN_WARNING);
				// TODO: ApplicationConfiguration - stage development
				String stage = configurationService.getValue("idm.pub.app.stage");
				if (!"development".equalsIgnoreCase(stage)) {
					result.setLevel(NotificationLevel.ERROR);
				}
			} else {
				resultModel = new DefaultResultModel(CoreResultCode.OK);
			}
		}
		//
		result.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build());
		//
		return result;
	}
}

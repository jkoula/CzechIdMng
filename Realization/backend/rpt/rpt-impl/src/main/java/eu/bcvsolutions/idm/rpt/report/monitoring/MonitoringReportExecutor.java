package eu.bcvsolutions.idm.rpt.report.monitoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;

import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptMonitoringResultDto;

/**
 * Report with last monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Enabled(RptModuleDescriptor.MODULE_ID)
@Component(value = MonitoringReportExecutor.REPORT_NAME)
@Description("Report with last monitoring results.")
public class MonitoringReportExecutor extends AbstractReportExecutor {

	public static final String REPORT_NAME = "core-monitoring-report"; // report ~ executor name
	//
	@Autowired @Lazy private MonitoringManager monitoringManager;

	@Override
	public String getName() {
		return REPORT_NAME;
	}
	
	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		// prepare temp file for json stream
		File temp = getAttachmentManager().createTempFile();
		//
		try (FileOutputStream outputStream = new FileOutputStream(temp);) {
	        // write into json stream
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				// json will be array of identities
				jGenerator.writeStartArray();
				// 
				List<IdmMonitoringResultDto> lastResults = monitoringManager
						.getLastResults(null, null, IdmBasePermission.READ)
						.getContent();
				counter = 0L;
				count = Long.valueOf(lastResults.size());
				boolean canContinue = true;
				for (IdmMonitoringResultDto result : lastResults) {
					IdmMonitoringDto monitoring = getLookupService().lookupEmbeddedDto(result, IdmMonitoringResult_.monitoring);
					//
					RptMonitoringResultDto resultDto = new RptMonitoringResultDto(result);
					resultDto.setResultMessage(result.getResult().getModel() == null ? null : result.getResult().getModel().getMessage());
					resultDto.setOwnerType(result.getOwnerType());
					resultDto.setOwnerId(result.getOwnerId());
					resultDto.setValue(result.getValue());
					resultDto.setLevel(result.getLevel() == null ? NotificationLevel.SUCCESS : result.getLevel());
					resultDto.setEvaluatorType(result.getEvaluatorType());
					resultDto.setEvaluatorDescription(monitoring.getDescription());
					resultDto.setInstanceId(result.getInstanceId());
					//
					// write dto into json
					getMapper().writeValue(jGenerator, resultDto);
					//
					// supports cancel report generating (report extends long running task)
					++counter;
					canContinue = updateState();
					if (!canContinue) {
						break;
					}
				}
				//
				// close array of identities
				jGenerator.writeEndArray();
			} finally {
				// close json stream
				jGenerator.close();
			}
			// save create temp file with array of identities in json as attachment
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}
}

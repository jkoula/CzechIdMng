package eu.bcvsolutions.idm.rpt.event.processor;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent.ReportEventType;
import eu.bcvsolutions.idm.rpt.api.event.processor.ReportProcessor;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.renderer.DefaultJsonRenderer;

/**
 * Render after port is generated.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ReportGenerateEndRenderProcessor.PROCESSOR_NAME)
@Description("Render after port is generated.")
public class ReportGenerateEndRenderProcessor 
		extends CoreEventProcessor<RptReportDto> 
		implements ReportProcessor {
	
	public static final String PROCESSOR_NAME = "report-generate-end-render-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReportGenerateEndRenderProcessor.class);
	//
	@Autowired private ReportManager reportManager;
	@Autowired private AttachmentManager attachmentManager;
	
	public ReportGenerateEndRenderProcessor() {
		super(ReportEventType.GENERATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<RptReportDto> process(EntityEvent<RptReportDto> event) {
		RptReportDto report = event.getContent();
		//
		if (report.getResult() == null || report.getResult().getState() != OperationState.EXECUTED) {
			return new DefaultEventResult<>(event, this);
		}
		//
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		// render report as attachments
		reportManager
			.getRenderers(report.getExecutorName())
			.stream()
			.filter(renderer -> !renderer.getName().equals(DefaultJsonRenderer.RENDERER_NAME)) // default json will be ignored
			.forEach(renderer -> {
				try {
					RptRenderedReportDto result = reportManager.render(report, renderer.getName());
					//
					// save rendered report as attachment
					IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
					attachmentDto.setDescription(getDescription());
					String reportName = String.format(
							"%s-%s.%s", 
							SpinalCase.format(report.getExecutorName()),
							report.getCreated().format(formatter),
							renderer.getExtension()
					);
					attachmentDto.setName(reportName);
					attachmentDto.setAttachmentType(renderer.getName());
					attachmentDto.setMimetype(renderer.getFormat().toString());
					attachmentDto.setInputData(result.getRenderedReport());
					//
					attachmentManager.saveAttachment(report, attachmentDto);
				} catch (Exception ex) {
					ResultModel resultModel = new DefaultResultModel(
							RptResultCode.REPORT_RENDER_FAILED,
							ImmutableMap.of("reportName", report.getName())
					);
					ExceptionUtils.log(LOG, resultModel, ex);
				}
			});
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 900;
	}

}

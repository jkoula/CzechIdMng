package eu.bcvsolutions.idm.rpt.report.monitoring;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;
import eu.bcvsolutions.idm.rpt.dto.RptMonitoringResultDto;

/**
 * Render monitoring report.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(MonitoringReportXlsxRenderer.RENDERER_NAME)
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class MonitoringReportXlsxRenderer extends AbstractXlsxRenderer implements RendererRegistrar {
	
	public static final String RENDERER_NAME = "core-monitoring-report-xlsx-renderer";
	//
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public String getName() {
		return RENDERER_NAME;
	}
	
	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
			XSSFSheet sheet = workbook.createSheet("Report");
			sheet.setDefaultColumnWidth(15);
			// header
			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 15);
			//
			Row row = sheet.createRow(2);
			row.setHeightInPoints((short) 20);
			Cell cell = row.createCell(0);
			XSSFRichTextString headerColumn = new XSSFRichTextString();
			headerColumn.append("Detail", headerFont);
			cell.setCellValue(headerColumn);
			cell = row.createCell(1);
			headerColumn = new XSSFRichTextString();
			headerColumn.append("Level", headerFont);
			cell.setCellValue(headerColumn);
			cell = row.createCell(2);
			headerColumn = new XSSFRichTextString();
			headerColumn.append("Result message", headerFont);
			cell.setCellValue(headerColumn);
			cell = row.createCell(3);
			headerColumn = new XSSFRichTextString();
			headerColumn.append("Value", headerFont);
			cell.setCellValue(headerColumn);
			cell = row.createCell(4);
			headerColumn = new XSSFRichTextString();
			headerColumn.append("Monitoring type", headerFont);
			cell.setCellValue(headerColumn);
			//
			int rowNum = 3;
			NotificationLevel summaryLevel = NotificationLevel.SUCCESS;
			//
			XSSFCellStyle errorStyle = workbook.createCellStyle();
			errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			//
			XSSFCellStyle warningStyle = workbook.createCellStyle();
			warningStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			//
			XSSFCellStyle defaultStyle = workbook.createCellStyle();
			//
			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					RptMonitoringResultDto monitoringResult = getMapper().readValue(jParser, RptMonitoringResultDto.class);
					String[] monitoringEvaluatorType = StringUtils.split(monitoringResult.getEvaluatorType(), '.');
					String frontendUrl = configurationService.getFrontendUrl(String.format("monitoring/monitoring-results/%s", monitoringResult.getId()));
					NotificationLevel level = monitoringResult.getLevel();
					//
					XSSFCellStyle cellStyle = null;
					if (level == NotificationLevel.ERROR) {
						cellStyle = errorStyle;
					} else if (level == NotificationLevel.WARNING) {
						cellStyle = warningStyle;
					} else {
						cellStyle = defaultStyle;
					}
					//
					if (summaryLevel.ordinal() < level.ordinal()) {
						summaryLevel = level;
					}
					//
					row = sheet.createRow(rowNum++);	
					cell = row.createCell(0);
					cell.setCellValue(frontendUrl);
					Hyperlink link = createHelper.createHyperlink(XSSFHyperlink.LINK_URL);
		            link.setAddress(frontendUrl);
		            cell.setHyperlink(link);
					cell.setCellValue("Show detail");
					cell = row.createCell(1);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(level.name());
					cell = row.createCell(2);
					cell.setCellValue(monitoringResult.getResultMessage());
					cell = row.createCell(3);
					cell.setCellValue(monitoringResult.getValue());
					cell = row.createCell(4);
					cell.setCellValue(
							String.format("%s - %s",
									monitoringEvaluatorType[monitoringEvaluatorType.length - 1],
									monitoringResult.getEvaluatorDescription()
							)
					);
				}
			}
			// set auto size column
			for (int index = 0; index <= 4; index++) {
				sheet.autoSizeColumn(index);
			}
			//
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
			row = sheet.createRow(0);
			row.setHeightInPoints((short) 20);
			cell = row.createCell(0);
			headerColumn = new XSSFRichTextString();
			if (summaryLevel == NotificationLevel.ERROR) {
				headerColumn.append("Status CzechIdM - monitoring contains errors", headerFont);
			} else if (summaryLevel == NotificationLevel.WARNING) {
				headerColumn.append("Status CzechIdM - monitoring contains warnings", headerFont);
			} else {
				headerColumn.append("Status CzechIdM - monitoring is ok", headerFont);
			}
			cell.setCellValue(headerColumn);
			//
			// close json stream
			jParser.close();
			//
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}
	
	/**
	 * Register renderer to report
	 */
	@Override
	public String[] register(String reportName) {
		if (MonitoringReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[] { getName() };
		}
		return new String[] {};
	}

}
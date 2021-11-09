package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import eu.bcvsolutions.idm.acc.domain.SysValueChangeType;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceValueDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;
import eu.bcvsolutions.idm.rpt.dto.RptChangesOnSystemDataDto;
import eu.bcvsolutions.idm.rpt.dto.RptChangesOnSystemRecordDto;
import eu.bcvsolutions.idm.rpt.dto.RptChangesOnSystemState;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityIncompatibleRoleDto;

/**
 * The report renderer of changes on the system.
 * 
 * @author Ondrej Husnik
 * @since 11.3.0
 *
 */
@Component("changes-on-system-report-xlsx-renderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class ChangesOnSystemReportXlsxRenderer extends AbstractXlsxRenderer implements RendererRegistrar {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ChangesOnSystemReportXlsxRenderer.class);
	
	final private String reportSheetName = "Report";
	final private String statusColumnName = "Status";
	final private String keyColumnName = "Username (uid)";
	
	final private String NEW_LINE = System.lineSeparator();
	final private String IDM_VALUE = "IdM:";
	final private String SYSTEM_VALUE = "Sys:";
	
	
	@Override
	public InputStream render(RptReportDto report) {
		try (JsonParser jParser = getMapper().getFactory().createParser(getReportData(report))) {
			
			List<String> headerNames = new ArrayList<String>();
			JsonToken token = null;

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(reportSheetName);
			sheet.setDefaultColumnWidth(15);
			
			// parse and render attributes' name
			token = getJsonNodeByName(jParser, ChangesOnSystemReportExecutor.ATTRIBUTE_NAME_JSON_KEY);
			if (token != null) {
				headerNames = getMapper().readValue(jParser, new TypeReference<List<String>>() {});
			}
			Map<String, Integer> attributeOrder = renderHeader(sheet, headerNames);
			// parse and render attribute record
			token = getJsonNodeByName(jParser, ChangesOnSystemReportExecutor.RECORDS_JSON_KEY);
			int recordIdx = 1;
			do {
				if (token != null) {
					RptChangesOnSystemRecordDto record = getMapper().readValue(jParser, RptChangesOnSystemRecordDto.class);
					renderRecord(recordIdx, sheet, record, attributeOrder);
					token = jParser.nextValue();
					recordIdx++;
				} else {
					break;
				}
			} while(token != JsonToken.END_ARRAY && !jParser.isClosed());
			
			// cell autofitting
			for (int index = 0; index < attributeOrder.size()+1; index++) {
				sheet.autoSizeColumn(index);
			}
			
			// close and return input stream
			return getInputStream(workbook);
		} catch (Exception ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}
	
	/**
	 * Register renderer to report
	 */
	@Override
	public String[] register(String reportName) {
		if (ChangesOnSystemReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[] { getName() };
		}
		return new String[] {};
	}
		
	/**
	 * Method renders file header with the attribute names and prepares the map of attribute positions in the table.
	 *   
	 * @param sheet
	 * @param headerNames
	 * @return
	 */
	private Map<String, Integer> renderHeader(Sheet sheet, List<String> headerNames) {
		Map<String, Integer> attributeOrder = new HashMap<String, Integer>();
		XSSFFont headerFont = ((XSSFWorkbook)sheet.getWorkbook()).createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short)15);
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		XSSFRichTextString headerColumn = new XSSFRichTextString();
		headerColumn.append(statusColumnName, headerFont);
		cell.setCellValue(headerColumn);
		attributeOrder.put(statusColumnName, 0);
		
		cell = row.createCell(1);
		XSSFRichTextString keyColumn = new XSSFRichTextString();
		keyColumn.append(keyColumnName, headerFont);
		cell.setCellValue(keyColumn);
		attributeOrder.put(keyColumnName, 1);
		
		if (CollectionUtils.isEmpty(headerNames)) {
			return attributeOrder; 
		}
		int cellIdx = 2;
		for (String name : headerNames) {
			cell = row.createCell(cellIdx);
			XSSFRichTextString column = new XSSFRichTextString();
			column.append(name, headerFont);
			cell.setCellValue(column);
			attributeOrder.put(name, cellIdx);
			cellIdx++;
		}
		return attributeOrder;
	}
	
	/**
	 * Methods renders single record 
	 */
	private void renderRecord(int lineIdx, Sheet sheet, RptChangesOnSystemRecordDto record, Map<String, Integer> attributeOrder) {
		if (record == null) {
			LOG.warn("Trying to render null record!");
			return;
		}
		
		Row row = sheet.createRow(lineIdx);
		row.setHeight((short)-1);
		int cellIdx;
		
		// render status
		XSSFRichTextString content = new XSSFRichTextString();
		RptChangesOnSystemState state = record.getState();
		cellIdx = attributeOrder.get(statusColumnName);
		Cell cell = row.createCell(cellIdx);
		content.append(state.toString());
		cell.setCellValue(content);
		cell.setCellStyle(getStatusCellStyle(sheet, state));
		
		// render record key/identifier
		XSSFFont boldFont = ((XSSFWorkbook)sheet.getWorkbook()).createFont();
		boldFont.setBold(true);
		String key = record.getIdentifier();
		content = new XSSFRichTextString(); 
		cellIdx = attributeOrder.get(keyColumnName);
		cell = row.createCell(cellIdx);
		content.append(Objects.toString(key, ""), boldFont);
		cell.setCellValue(content);
		
		if (state == RptChangesOnSystemState.FAILED) {
			cellIdx++;
			cell = row.createCell(cellIdx);
			content = new XSSFRichTextString();
			XSSFFont font = getTextFont(cell, SysValueChangeType.REMOVED);
			content.append(Objects.toString(record.getError(), ""), font);
			cell.setCellValue(content);
			return;
		}
		
		List<SysAttributeDifferenceDto> differences = record.getAttributeDifferences();
		if (CollectionUtils.isEmpty(differences)) {
			return;
		}
		for (SysAttributeDifferenceDto diff : differences) {
			Integer colIdx = attributeOrder.get(diff.getName());
			if (colIdx==null) {
				continue;
			}
			cell = row.createCell(colIdx);
			if(diff.isMultivalue()) {
				renderMultiValueCell(cell, diff);
			} else {
				renderSingleValueCell(cell, diff, state);
			}
		}
	}
	
	/**
	 * Single value cell renderer 
	 */
	private Cell renderSingleValueCell(Cell cell, SysAttributeDifferenceDto attribute, RptChangesOnSystemState recordState) {
		boolean isCreated = RptChangesOnSystemState.ADDED==recordState;
		XSSFRichTextString content = new XSSFRichTextString();
		SysAttributeDifferenceValueDto value = attribute.getValue();
		if (attribute.isChanged()) {
			XSSFFont systemFont = getTextFont(cell, SysValueChangeType.REMOVED);
			XSSFFont idmFont = getTextFont(cell, SysValueChangeType.ADDED);
			if (!isCreated) {
				content.append(SYSTEM_VALUE, systemFont);
				content.append(Objects.toString(value.getOldValue()), systemFont);
				content.append(NEW_LINE);
			}
			content.append(IDM_VALUE, idmFont);
			content.append(Objects.toString(value.getValue()), idmFont);
			cell.getCellStyle().setWrapText(true);
			cell.setCellValue(content);
			
		} else {
			content.append(Objects.toString(value.getValue()), getTextFont(cell, null));
			cell.setCellValue(content);
		}
		return cell;
	}
	
	/**
	 * Multi value cell renderer 
	 */
	private Cell renderMultiValueCell(Cell cell, SysAttributeDifferenceDto attribute) {
		XSSFRichTextString content = new XSSFRichTextString();
		SysValueChangeType[] changeTypes = {SysValueChangeType.ADDED, SysValueChangeType.REMOVED, null}; // also defines the order of change types 
		List<List<SysAttributeDifferenceValueDto>> valueList = new ArrayList<List<SysAttributeDifferenceValueDto>>();
		for (SysValueChangeType type : changeTypes) {
			List<SysAttributeDifferenceValueDto> values = attribute
					.getValues()
					.stream()
					.filter(val -> type==val.getChange())
					.collect(Collectors.toList());
			valueList.add(values);
		}
		
		for (int i = 0; i < changeTypes.length; i++) {
			SysValueChangeType changeType = changeTypes[i];
			List<SysAttributeDifferenceValueDto> values = valueList.get(i);
			for (int j = 0;  j < values.size(); ++j) {
				SysAttributeDifferenceValueDto value = values.get(j);
				content.append(Objects.toString(value.getValue()), getTextFont(cell, changeType));
				if (j >= values.size() - 1 && i >= changeTypes.length - 1) {
					continue;
				}
				content.append(NEW_LINE);
			}
		}
		cell.setCellValue(content);
		return cell;
	}
	
	/**
	 *  Factory method for sheet text fonts.
	 */
	private XSSFFont getTextFont(Cell cell, SysValueChangeType changeType) {
		XSSFWorkbook workbook = (XSSFWorkbook)cell.getSheet().getWorkbook();
		return  getTextFont(workbook, changeType);
	}
	
	/**
	 * Factory method for sheet text fonts.
	 */
	private XSSFFont getTextFont(XSSFWorkbook workbook, SysValueChangeType changeType) {
		XSSFFont font = workbook.createFont();
		if (changeType==null) {
			font.setColor(HSSFColor.LIGHT_BLUE.index);
			return font;
		}
		
		switch (changeType) {
		case ADDED:
			font.setColor(HSSFColor.GREEN.index);
			return font;
		case REMOVED:
			font.setColor(HSSFColor.RED.index);
			return font;
		default:
			font.setColor(HSSFColor.LIGHT_BLUE.index);
			return font;
		}
	}
	
	/**
	 * Factory method for cell style. 
	 */
	private XSSFCellStyle getStatusCellStyle(Sheet sheet, RptChangesOnSystemState state) {
		XSSFCellStyle style = ((XSSFWorkbook)sheet.getWorkbook()).createCellStyle();
		if (state==null) {
			return style;
		}
		
		switch (state) {
		case ADDED:
			style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case CHANGED:
			style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case NO_CHANGE:
			style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case FAILED:
			style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			break;
		case NO_ACCOUNT_FOR_ENTITY: // new in idm
			style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		case NO_ENTITY_FOR_ACCOUNT:
			style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		default:
			break;
		}
		return style;
	}
	
	
	/**
	 * Methods is intended to read JSON values of the renderer source file specified by its name. 
	 * This method searches for a field name and returns token with its value.
	 * The parser is able to go through the input stream one way only.
	 * Field names HAVE TO be searched in the same order they were written.
	 * 
	 */
	JsonToken getJsonNodeByName(JsonParser jParser, String propertyName) {
		JsonToken token = null;
		try {
			do {
				String name = jParser.currentName();
				if (StringUtils.equals(name, ChangesOnSystemReportExecutor.ATTRIBUTE_NAME_JSON_KEY) &&
						StringUtils.equals(propertyName, ChangesOnSystemReportExecutor.ATTRIBUTE_NAME_JSON_KEY)) {
					token = jParser.nextToken();
					if (token == JsonToken.START_ARRAY) {
						return token;
					} else {
						return null;
					}
				} else if (StringUtils.equals(name, ChangesOnSystemReportExecutor.RECORDS_JSON_KEY) &&
						StringUtils.equals(propertyName, ChangesOnSystemReportExecutor.RECORDS_JSON_KEY)) {
					token = jParser.nextToken(); // skip to the start of array
					do {
						if (token == JsonToken.START_OBJECT) {
							return token;
						}
						token = jParser.nextToken();
					} while (token != JsonToken.END_ARRAY);
					return null;
				} else if (name != null) { 
					token = jParser.nextToken();
					if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
						jParser.skipChildren();
					}
				}
				token = jParser.nextToken();
			} while(!jParser.isClosed());
		} catch (Exception e) {
			return null;
		}
		return token;
	}
}
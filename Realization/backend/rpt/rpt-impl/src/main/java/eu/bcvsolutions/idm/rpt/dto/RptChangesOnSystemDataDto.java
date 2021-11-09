package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;
import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;

public class RptChangesOnSystemDataDto implements Serializable {
	private static final long serialVersionUID = 1L;

	
	private SysSystemDto system;
	private SysSystemMappingDto systemMapping;
	private List<SysSystemAttributeMappingDto> attributes;
	private List<String> selectedAttributeNames;
	private List<RptChangesOnSystemRecordDto> records;

	public SysSystemDto getSystem() {
		return system;
	}

	public void setSystem(SysSystemDto system) {
		this.system = system;
	}

	public SysSystemMappingDto getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(SysSystemMappingDto systemMapping) {
		this.systemMapping = systemMapping;
	}

	public List<SysSystemAttributeMappingDto> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<SysSystemAttributeMappingDto> attributes) {
		this.attributes = attributes;
	}

	public List<RptChangesOnSystemRecordDto> getRecords() {
		return records;
	}

	public void setRecords(List<RptChangesOnSystemRecordDto> records) {
		this.records = records;
	}

	public List<String> getSelectedAttributeNames() {
		return selectedAttributeNames;
	}

	public void setSelectedAttributeNames(List<String> selectedAttributeNames) {
		this.selectedAttributeNames = selectedAttributeNames;
	}
	
	
}

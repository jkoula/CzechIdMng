package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;
import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;

/**
 * DTO for report for comparison values in IdM and system.
 *
 * @author Ondrej Husnik
 * @since 12.0.0
 */
public class RptChangesOnSystemRecordDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//
	private String identifier;
	private RptChangesOnSystemState state;
	private List<SysAttributeDifferenceDto> attributeDifferences;
	private String error;
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public RptChangesOnSystemState getState() {
		return state;
	}

	public void setState(RptChangesOnSystemState state) {
		this.state = state;
	}

	public List<SysAttributeDifferenceDto> getAttributeDifferences() {
		return attributeDifferences;
	}
	
	public void setAttributeDifferences(List<SysAttributeDifferenceDto> attributeDifferences) {
		this.attributeDifferences = attributeDifferences;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}

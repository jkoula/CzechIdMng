package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Codeable;

/**
 * Panel configuration.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
public class PanelDto implements BaseDto, Codeable {

	private static final long serialVersionUID = 1L;
	//
	@JsonDeserialize(as = String.class)
	private String id; // ~ uiKey from frontend
	private Boolean collapsed;
	private Boolean closed;
	
	public PanelDto() {
	}
	
	public PanelDto(Serializable id) {
		this.id = id == null ? null : id.toString();
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id = id == null ? null : id.toString();
	}
	
	@Override
	public String getCode() {
		return id;
	}
	
	/**
	 * Panel is collapsed or expanded.
	 * 
	 * @return true - collapsed, false - expanded
	 */
	public Boolean getCollapsed() {
		return collapsed;
	}
	
	/**
	 * Panel is collapsed or expanded.
	 * 
	 * @param collapsed true - collapsed, false - expanded, null - default (not controlled by profile)
	 */
	public void setCollapsed(Boolean collapsed) {
		this.collapsed = collapsed;
	}
	
	/**
	 * Panel is closed.
	 * 
	 * @return true - closed, false - opened, null - default (not controlled by profile)
	 */
	public Boolean getClosed() {
		return closed;
	}
	
	/**
	 * Panel is closed.
	 * 
	 * @param closed true - closed, false - opened, null - default (not controlled by profile)
	 */
	public void setClosed(Boolean closed) {
		this.closed = closed;
	}
}

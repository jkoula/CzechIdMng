package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Baskgroud colors.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
@JsonInclude(Include.NON_NULL)
public class BackgroundDto implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	@JsonProperty(value = "default")
	private String defaultColor;
	@JsonProperty(value = "paper")
	private String paperColor;
	
	public String getDefaultColor() {
		return defaultColor;
	}
	
	public void setDefaultColor(String defaultColor) {
		this.defaultColor = defaultColor;
	}
	
	public String getPaperColor() {
		return paperColor;
	}
	
	public void setPaperColor(String paperColor) {
		this.paperColor = paperColor;
	}
}

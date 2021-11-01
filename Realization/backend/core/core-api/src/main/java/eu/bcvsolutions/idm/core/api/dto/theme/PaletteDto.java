package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Theme palette - configured colors.
 * 
 * @author Radek Tomiška
 *
 */
@JsonInclude(Include.NON_NULL)
public class PaletteDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String type = "light";
	private PaletteColorDto primary;
	private PaletteColorDto secondary;
	private PaletteColorDto success;
	private PaletteColorDto info;
	private PaletteColorDto warning;
	private PaletteColorDto error;
	private ActionDto action;
	private BackgroundDto background;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public PaletteColorDto getPrimary() {
		return primary;
	}
	
	public void setPrimary(PaletteColorDto primary) {
		this.primary = primary;
	}
	
	public PaletteColorDto getSecondary() {
		return secondary;
	}
	
	public void setSecondary(PaletteColorDto secondary) {
		this.secondary = secondary;
	}
	
	public PaletteColorDto getSuccess() {
		return success;
	}
	
	public void setSuccess(PaletteColorDto success) {
		this.success = success;
	}
	
	public PaletteColorDto getInfo() {
		return info;
	}
	
	public void setInfo(PaletteColorDto info) {
		this.info = info;
	}
	
	public PaletteColorDto getWarning() {
		return warning;
	}
	
	public void setWarning(PaletteColorDto warning) {
		this.warning = warning;
	}
	
	public PaletteColorDto getError() {
		return error;
	}
	
	public void setError(PaletteColorDto error) {
		this.error = error;
	}
	
	public ActionDto getAction() {
		return action;
	}
	
	public void setAction(ActionDto action) {
		this.action = action;
	}
	
	public BackgroundDto getBackground() {
		return background;
	}
	
	public void setBackground(BackgroundDto background) {
		this.background = background;
	}
}

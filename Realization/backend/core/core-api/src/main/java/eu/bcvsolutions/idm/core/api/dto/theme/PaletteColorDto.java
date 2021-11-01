package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Theme color.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
@JsonInclude(Include.NON_NULL)
public class PaletteColorDto implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	private String main; // required
	private String light; // optional - computed from main color otherwise
	private String dark; // optional - computed from main color otherwise
	private String contrastText; // optional - computed from main color otherwise
	
	public PaletteColorDto() {
	}
	
	public PaletteColorDto(String main) {
		this.main = main;
	}
	
	public String getMain() {
		return main;
	}
	
	public void setMain(String main) {
		this.main = main;
	}
	
	public String getLight() {
		return light;
	}
	
	public void setLight(String light) {
		this.light = light;
	}
	
	public String getDark() {
		return dark;
	}
	
	public void setDark(String dark) {
		this.dark = dark;
	}
	
	public String getContrastText() {
		return contrastText;
	}
	
	public void setContrastText(String contrastText) {
		this.contrastText = contrastText;
	}
}

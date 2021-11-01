package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON theme.
 * 
 * https://v4.mui.com/customization/default-theme/#explore
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
@JsonInclude(Include.NON_NULL)
public class ThemeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	private PaletteDto palette;
	private ShapeDto shape;
	
	public PaletteDto getPalette() {
		return palette;
	}
	
	public void setPalette(PaletteDto palette) {
		this.palette = palette;
	}
	
	public ShapeDto getShape() {
		return shape;
	}
	
	public void setShape(ShapeDto shape) {
		this.shape = shape;
	}
}

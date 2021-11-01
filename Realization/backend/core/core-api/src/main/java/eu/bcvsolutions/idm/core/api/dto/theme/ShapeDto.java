package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

/**
 * Theme Shapes.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
public class ShapeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int borderRadius = 4;
	
	public int getBorderRadius() {
		return borderRadius;
	}
	
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
	}
}

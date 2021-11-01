package eu.bcvsolutions.idm.core.api.dto.theme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Action colors.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
@JsonInclude(Include.NON_NULL)
public class ActionDto implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	private String loading;
	
	public String getLoading() {
		return loading;
	}
	
	public void setLoading(String loading) {
		this.loading = loading;
	}
}

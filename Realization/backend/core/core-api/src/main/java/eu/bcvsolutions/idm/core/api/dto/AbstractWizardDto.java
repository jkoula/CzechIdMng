package eu.bcvsolutions.idm.core.api.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * Abstract representation of wizard dto
 * @author Roman Kucera
 */
public class AbstractWizardDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;

	private String wizardStepName;

	private Map<String, String> metadata;

	private boolean reopened = false;

	private int order;

	@JsonProperty(value = EmbeddedDto.PROPERTY_EMBEDDED, access = JsonProperty.Access.READ_ONLY)
	@ApiModelProperty(accessMode = ApiModelProperty.AccessMode.READ_ONLY)
	private Map<String, BaseDto> embedded;

	public String getWizardStepName() {
		return wizardStepName;
	}

	public void setWizardStepName(String wizardStepName) {
		this.wizardStepName = wizardStepName;
	}

	public Map<String, String> getMetadata() {
		if (metadata == null) {
			metadata = new HashMap<>();
		}
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public boolean isReopened() {
		return reopened;
	}

	public void setReopened(boolean reopened) {
		this.reopened = reopened;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Map<String, BaseDto> getEmbedded() {
		if(embedded == null){
			embedded = new HashMap<>();
		}
		return embedded;
	}

	public void setEmbedded(Map<String, BaseDto> embedded) {
		this.embedded = embedded;
	}
}

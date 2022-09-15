package eu.bcvsolutions.idm.core.api.dto;

import java.util.HashMap;
import java.util.Map;

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
}

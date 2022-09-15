package eu.bcvsolutions.idm.acc.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.EmbeddedDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;


/**
 * Connector DTO extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Relation(collectionRelation = "connectorTypes")
public class ConnectorTypeDto extends AbstractWizardDto {

	private String iconKey;
	private String connectorName;
	@JsonProperty(value = EmbeddedDto.PROPERTY_EMBEDDED, access = Access.READ_ONLY)
	@ApiModelProperty(accessMode = AccessMode.READ_ONLY)
	private Map<String, BaseDto> embedded;
	// Version of current found connector.
	private String version;
	// I current found connector local?
	private boolean local = true;
	private boolean hideParentConnector;
	// Defines if that wizard is opened from existed system.
	private UUID remoteServer;

	public String getIconKey() {
		return iconKey;
	}

	public void setIconKey(String iconKey) {
		this.iconKey = iconKey;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isHideParentConnector() {
		return hideParentConnector;
	}

	public void setHideParentConnector(boolean hideParentConnector) {
		this.hideParentConnector = hideParentConnector;
	}

	/**
	 * Remote server.
	 * 
	 * @return remote server identifier
	 * @since 10.8.0
	 */
	public UUID getRemoteServer() {
		return remoteServer;
	}
	
	/**
	 * Remote server.
	 * 
	 * @param remoteServer remote server identifier
	 * @since 10.8.0
	 */
	public void setRemoteServer(UUID remoteServer) {
		this.remoteServer = remoteServer;
	}
}

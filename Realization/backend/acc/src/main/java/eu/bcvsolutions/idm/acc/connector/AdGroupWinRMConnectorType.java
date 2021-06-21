package eu.bcvsolutions.idm.acc.connector;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * AD+WinRM wizard for users.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Component(AdGroupWinRMConnectorType.NAME)
public class AdGroupWinRMConnectorType extends AdGroupConnectorType {

	public static final String NAME = "ad-group-winrm-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.cmd.CmdConnector";
	}

	@Override
	public String getIconKey() {
		return "ad-group-connector-icon";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MS AD+WinRM - Groups", 1));
		return metadata;
	}

	@Override
	protected void initDefaultConnectorSettings(SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		super.initDefaultConnectorSettings(systemDto, connectorFormDef);
		// Additional connector default connector settings for WinRM connector.
		this.setValueToConnectorInstance("testViaAd", Boolean.TRUE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("searchViaAd", Boolean.TRUE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("deleteViaAd", Boolean.TRUE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("updateViaAd", Boolean.TRUE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("createViaAd", Boolean.TRUE, systemDto, connectorFormDef);
	}

	@Override
	public int getOrder() {
		return 195;
	}

}

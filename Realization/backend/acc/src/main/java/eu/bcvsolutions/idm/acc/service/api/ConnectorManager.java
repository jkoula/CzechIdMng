package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.service.WizardManager;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;

/**
 * Connector manager controls connector types, which extends standard IC connectors for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public interface ConnectorManager extends WizardManager<ConnectorTypeDto, ConnectorType> {

	/**
	 * Converts InfoConnectorInfo to the ConnectorTypeDto.
	 */
	ConnectorTypeDto convertIcConnectorInfoToDto(IcConnectorInfo info);

	/**
	 * Find local connector key by connector name.
	 * 
	 * @see #findConnectorKey(ConnectorTypeDto)
	 */
	IcConnectorKey findConnectorKey(String connectorName);
	
	/**
	 * Find connector key by connector type. Supports remote server connectors.
	 * 
	 * @param connectorType configured connector type
	 * @return connector key or null if not found
	 * @since 10.8.0
	 */
	IcConnectorKey findConnectorKey(ConnectorTypeDto connectorType);

	/**
	 * Find connector type by system.
	 */
	ConnectorType findConnectorTypeBySystem(SysSystemDto systemDto);
}

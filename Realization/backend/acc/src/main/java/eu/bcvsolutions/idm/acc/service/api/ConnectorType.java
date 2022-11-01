package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.core.api.service.WizardService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import java.util.List;

/**
 * Connector type extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public interface ConnectorType extends WizardService<ConnectorTypeDto> {

	String CREATES_ROLE_WITH_SYSTEM = "createRoleWithSystem";
	// For hide the dialog for create new role-system on summary step.
	String SKIP_CREATES_ROLE_WITH_SYSTEM = "skipCreateRoleWithSystem";
	String MAPPING_ID = "mappingId";
	String SCHEMA_ID = "schemaId";
	String NEW_ROLE_WITH_SYSTEM_CODE = "newRoleWithSystemCode";
	String ROLE_SYSTEM_ID = "roleSystemId";
	String SYSTEM_DTO_KEY = "system";
	String STEP_MAPPING = "mapping";
	String ENTITY_TYPE = "entityType";
	String TREE_TYPE_ID = "treeTypeId";
	String OPERATION_TYPE = "operationType";
	String MAPPING_DTO_KEY = "mapping";
	String SYNC_DTO_KEY = "sync";
	String ALERT_MORE_MAPPINGS = "alertMoreMappings";

	/**
	 * Defines for which connector could be used.
	 *
	 * @return
	 */
	String getConnectorName();

	/**
	 * Name of component of the FE, keeps main connector image.
	 *
	 * @return
	 */
	default String getIconKey() {return "default-connector";}

	/**
	 * Defines if original IC connector should be hidden in the UI.
	 *
	 * @return
	 */
	default boolean hideParentConnector() {return true;}

	/**
	 * Returns true if this connector type should be use for open given system.
	 */
	boolean supportsSystem(SysSystemDto systemDto);

	/**
	 * Returns values for given attribute and connector object by default.
	 * Can be used for more sophisticated searching with using system-group (see Cross-domains in AD connector type).
	 */
	List<Object> getConnectorValuesByAttribute(String uid,
											   IcObjectClass objectClass,
											   String schemaAttributeName,
											   SysSystemDto system,
											   IcConnectorObject existsConnectorObject,
											   SysSystemGroupSystemDto systemGroupSystem);

	/**
	 * Add given attribute with updated value to the update connector object.
	 */
	void addUpdatedAttribute(SysSchemaAttributeDto schemaAttribute, IcAttribute updatedAttribute, IcConnectorObject updateConnectorObject, IcConnectorObject existsConnectorObject);

	/**
	 * Get connector configuration for given system.
	 */
	IcConnectorConfiguration getConnectorConfiguration(SysSystemDto system);

	/**
	 * Get connector object from a connector.
	 */
	IcConnectorObject readConnectorObject(SysSystemDto system, String uid, IcObjectClass objectClass);
}

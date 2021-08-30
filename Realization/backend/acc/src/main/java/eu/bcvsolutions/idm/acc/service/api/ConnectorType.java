package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import java.util.List;
import java.util.Map;
import org.springframework.core.Ordered;

/**
 * Connector type extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public interface ConnectorType extends Ordered {

	String STEP_FINISH = "finish";
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
	 * Bean name / unique identifier (spring bean name).
	 *
	 * @return
	 */
	String getId();

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
	 * Returns module
	 *
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	/**
	 * Defines if original IC connector should be hidden in the UI.
	 *
	 * @return
	 */
	default boolean hideParentConnector() {return true;}


	/**
	 * Order of connectors.
	 *
	 * @return
	 */
	@Override
	int getOrder();

	/**
	 * If false, then connector type will be not visible to a user.
	 *
	 * @return
	 */
	boolean supports();


	/**
	 * Specific data for a connector type (attributes).
	 */
	Map<String, String> getMetadata();

	/**
	 * Execute connector type -> execute some wizard step.
	 *
	 */
	default ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		return connectorType;
	}

	/**
	 * Load data for specific wizard/step (for open existing system in the wizard).
	 */
	default ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		return connectorType;
	}

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

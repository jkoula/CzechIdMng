package eu.bcvsolutions.idm.core.api.domain;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Interface for records that own server instance identifier (server instance related records).
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface InstanceIdentifiable {

	String PROPERTY_INSTANCE_ID = ConfigurationService.PROPERTY_INSTANCE_ID;
	
	/**
	 * Get server instance identifier.
	 *
	 * @return server instance identifier
	 */
	String getInstanceId();
	
	/**
	 * Set server instance identifier.
	 * 
	 * @param instanceId instance identifier
	 */
	void setInstanceId(String instanceId);
}

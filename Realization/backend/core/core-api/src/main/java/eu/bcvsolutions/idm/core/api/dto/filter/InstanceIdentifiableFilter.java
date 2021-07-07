package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;

/**
 * Filter records that own server instance identifier (server instance related records).
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface InstanceIdentifiableFilter extends InstanceIdentifiable, BaseDataFilter {

	@Override
	default public String getInstanceId() {
		return getParameterConverter().toString(getData(), PROPERTY_INSTANCE_ID);
	}
	
	@Override
	default public void setInstanceId(String instanceId) {
		set(PROPERTY_INSTANCE_ID, instanceId);
	}
}

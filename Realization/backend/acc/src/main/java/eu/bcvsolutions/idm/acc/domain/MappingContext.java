package eu.bcvsolutions.idm.acc.domain;

import com.google.common.collect.Maps;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import java.util.List;
import java.util.Map;

/**
 * Mapping context - for provisioning now. Its filled on system mapping and should keeps often using data in more attributes (contracts, identity-roles ...);
 *
 * This context should be not persisted (can be big and can contains not serialized data ... password).
 *
 * @author Vít Švanda
 * @see ProvisioningContext for persisting some information to the provisioning archvie.
 * @since 10.5.0
 */
public class MappingContext {

	private List<IdmIdentityContractDto> contracts;
	private List<AbstractRoleAssignmentDto> identityRoles;
	private List<AbstractRoleAssignmentDto> identityRolesForSystem;
	private IcConnectorObject connectorObject;

	private Map<String, Object> context;

	public MappingContext() {
		this.context = Maps.newHashMap();
	}

	public Map<String, Object> getContext() {
		return context;
	}

	Object get(String key) {
		return context.get(key);
	}

	public void put(String key, Object value) {
		context.put(key, value);
	}

	public List<IdmIdentityContractDto> getContracts() {
		return contracts;
	}

	public void setContracts(List<IdmIdentityContractDto> contracts) {
		this.contracts = contracts;
	}

	public List<AbstractRoleAssignmentDto> getIdentityRoles() {
		return identityRoles;
	}

	public void setIdentityRoles(List<AbstractRoleAssignmentDto> identityRoles) {
		this.identityRoles = identityRoles;
	}

	public void setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
	}

	public IcConnectorObject getConnectorObject() {
		return connectorObject;
	}

	public List<AbstractRoleAssignmentDto> getIdentityRolesForSystem() {
		return identityRolesForSystem;
	}

	public void setIdentityRolesForSystem(List<AbstractRoleAssignmentDto> identityRolesForSystem) {
		this.identityRolesForSystem = identityRolesForSystem;
	}

	@Override
	public String toString() {
		return "MappingContext{" +
				"contracts=" + contracts +
				", identityRoles=" + identityRoles +
				", identityRolesForSystem=" + identityRolesForSystem +
				", connectorObject=" + connectorObject +
				", context=" + context +
				'}';
	}
}

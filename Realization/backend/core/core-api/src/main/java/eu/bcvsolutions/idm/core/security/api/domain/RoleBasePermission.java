package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Role added base permissions.
 * 
 * @author Radek Tomiška
 *
 */
public enum RoleBasePermission implements BasePermission {
	
	CANBEREQUESTED, // role can be requested.
	CHANGEPERMISSION; // @since 11.1.0 - create role request for changing identity permissions on related role.
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getModule() {
		// common base permission without module
		return null;
	}
}

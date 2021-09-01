package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Contract added base permissions.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public enum ContractBasePermission implements BasePermission {
	
	CANBEREQUESTED, // @since 11.1.0 create role request for changing (ADD only) identity permissions on related contract.
	CHANGEPERMISSION; // create role request for changing (UD only) identity permissions on related contract.
	
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

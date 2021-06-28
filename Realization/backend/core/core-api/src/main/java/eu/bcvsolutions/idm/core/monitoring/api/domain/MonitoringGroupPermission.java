package eu.bcvsolutions.idm.core.monitoring.api.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Monitoring permissions.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public enum MonitoringGroupPermission implements GroupPermission {
	
	MONITORING(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE,
			IdmBasePermission.EXECUTE),
	MONITORINGRESULT(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String MONITORING_COUNT = "MONITORING" + BasePermission.SEPARATOR + "COUNT";
	public static final String MONITORING_AUTOCOMPLETE = "MONITORING" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String MONITORING_READ = "MONITORING" + BasePermission.SEPARATOR + "READ";
	public static final String MONITORING_CREATE = "MONITORING" + BasePermission.SEPARATOR + "CREATE";
	public static final String MONITORING_UPDATE = "MONITORING" + BasePermission.SEPARATOR + "UPDATE";
	public static final String MONITORING_DELETE = "MONITORING" + BasePermission.SEPARATOR + "DELETE";
	public static final String MONITORING_EXECUTE = "MONITORING" + BasePermission.SEPARATOR + "EXECUTE";
	//
	public static final String MONITORINGRESULT_COUNT = "MONITORINGRESULT" + BasePermission.SEPARATOR + "COUNT";
	public static final String MONITORINGRESULT_AUTOCOMPLETE = "MONITORINGRESULT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String MONITORINGRESULT_READ = "MONITORINGRESULT" + BasePermission.SEPARATOR + "READ";
	public static final String MONITORINGRESULT_CREATE = "MONITORINGRESULT" + BasePermission.SEPARATOR + "CREATE";
	public static final String MONITORINGRESULT_UPDATE = "MONITORINGRESULT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String MONITORINGRESULT_DELETE = "MONITORINGRESULT" + BasePermission.SEPARATOR + "DELETE";
	public static final String MONITORINGRESULT_EXECUTE = "MONITORINGRESULT" + BasePermission.SEPARATOR + "EXECUTE";
	
	private final List<BasePermission> permissions;

	private MonitoringGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {		
		return permissions;
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getModule() {
		return CoreModule.MODULE_ID;
	}
}

package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for entity on target system.
 * 
 * @author Radek Tomiška
 *
 */
public class SysSystemEntityFilter extends DataFilter {
	
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_UID = "uid";
	public static final String PARAMETER_ENTITY_TYPE = "entityType";
	
	public SysSystemEntityFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemEntityFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSystemEntityFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemEntityDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}

	public SystemEntityType getEntityType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_ENTITY_TYPE, SystemEntityType.class);
	}

	public void setEntityType(SystemEntityType entityType) {
		set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public String getUid() {
		return getParameterConverter().toString(getData(), PARAMETER_UID);
	}

	public void setUid(String uid) {
		set(PARAMETER_UID, uid);
	}
}

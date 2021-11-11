package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for system entity handling.
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
public class SysSystemMappingFilter extends DataFilter {
	
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_OBJECT_CLASS_ID = "objectClassId";
	public static final String PARAMETER_OPERATION_TYPE = "operationType";
	public static final String PARAMETER_ENTITY_TYPE = "entityType";
	public static final String PARAMETER_TREE_TYPE_ID = "treeTypeId";

	public SysSystemMappingFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemMappingFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSystemMappingFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemMappingDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}

	public SystemOperationType getOperationType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_OPERATION_TYPE, SystemOperationType.class);
	}

	public void setOperationType(SystemOperationType operationType) {
		set(PARAMETER_OPERATION_TYPE, operationType);
	}

	public SystemEntityType getEntityType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_ENTITY_TYPE, SystemEntityType.class);
	}

	public void setEntityType(SystemEntityType entityType) {
		set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public void setObjectClassId(UUID objectClassId) {
		set(PARAMETER_OBJECT_CLASS_ID, objectClassId);
	}
	
	public UUID getObjectClassId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OBJECT_CLASS_ID);
	}

	public UUID getTreeTypeId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_TREE_TYPE_ID);
	}

	public void setTreeTypeId(UUID treeTypeId) {
		set(PARAMETER_TREE_TYPE_ID, treeTypeId);
	}
}

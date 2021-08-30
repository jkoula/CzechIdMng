package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import java.util.UUID;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *  System groups system (cross-domain) filter.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class SysSystemGroupSystemFilter extends DataFilter {

	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_OTHERS_SYSTEMS_IN_GROUP_SYSTEM_ID = "othersSystemsInGroupSystemId";
	public static final String PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_SYSTEM_ID = "crossDomainsGroupsForRoleSystemId";
	public static final String PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_ID = "crossDomainsGroupsForRoleId";
	public static final String PARAMETER_SYSTEM_GROUP_ID = "systemGroupId";
	public static final String PARAMETER_GROUP_TYPE = "groupType";
	public static final String PARAMETER_MERGE_ATTRIBUTE_CODE = "mergeAttributeCode";
	public static final String PARAMETER_MERGE_MAPPING_ATTRIBUTE_ID = "mergeMappingAttributeId";
	public static final String PARAMETER_DISABLED = Disableable.PROPERTY_DISABLED;

	public SysSystemGroupSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public SysSystemGroupSystemFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}

	public SysSystemGroupSystemFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemGroupSystemDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}
	
	public UUID getMergeMappingAttributeId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_MERGE_MAPPING_ATTRIBUTE_ID);
	}

	public void setMergeMappingAttributeId(UUID attributeId) {
		set(PARAMETER_MERGE_MAPPING_ATTRIBUTE_ID, attributeId);
	}
	
	public UUID getOthersSystemsInGroupSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OTHERS_SYSTEMS_IN_GROUP_SYSTEM_ID);
	}

	public void setOthersSystemsInGroupSystemId(UUID systemId) {
		set(PARAMETER_OTHERS_SYSTEMS_IN_GROUP_SYSTEM_ID, systemId);
	}
	
	public UUID getCrossDomainsGroupsForRoleSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_SYSTEM_ID);
	}

	public void setCrossDomainsGroupsForRoleSystemId(UUID systemId) {
		set(PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_SYSTEM_ID, systemId);
	}
	
	public UUID getCrossDomainsGroupsForRoleId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_ID);
	}

	public void setCrossDomainsGroupsForRoleId(UUID systemId) {
		set(PARAMETER_CROSS_DOMAINS_GROUPS_FOR_ROLE_ID, systemId);
	}

	public UUID getSystemGroupId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_GROUP_ID);
	}

	public void setSystemGroupId(UUID systemId) {
		set(PARAMETER_SYSTEM_GROUP_ID, systemId);
	}

	public SystemGroupType getGroupType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_GROUP_TYPE, SystemGroupType.class);
	}

	public void setGroupType(SystemGroupType type) {
		set(PARAMETER_GROUP_TYPE, type);
	}


	/**
	 * Filter disabled system-group.
	 */
	public Boolean getDisabled() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_DISABLED);
	}

	/**
	 * Filter disabled system-group.
	 */
	public void setDisabled(Boolean disabled) {
		set(PARAMETER_DISABLED, disabled);
	}

	public String getMergeAttributeCode() {
		return getParameterConverter().toString(getData(), PARAMETER_MERGE_ATTRIBUTE_CODE);
	}
	
	public void setMergeAttributeCode(String mergeAttributeCode) {
		set(PARAMETER_MERGE_ATTRIBUTE_CODE, mergeAttributeCode);
	}

}

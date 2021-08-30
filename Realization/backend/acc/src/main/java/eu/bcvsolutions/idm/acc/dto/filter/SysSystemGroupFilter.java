package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import java.util.UUID;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *  System groups (cross-domain) filter.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class SysSystemGroupFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_GROUP_TYPE = "groupType";

	public SysSystemGroupFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public SysSystemGroupFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}

	public SysSystemGroupFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemGroupDto.class, data, parameterConverter);
	}

	public SystemGroupType getGroupType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_GROUP_TYPE, SystemGroupType.class);
	}

	public void setGroupType(SystemGroupType type) {
		set(PARAMETER_GROUP_TYPE, type);
	}

}

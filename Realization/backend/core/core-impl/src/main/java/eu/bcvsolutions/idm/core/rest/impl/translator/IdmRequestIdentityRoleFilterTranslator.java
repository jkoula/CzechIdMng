package eu.bcvsolutions.idm.core.rest.impl.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.OwnerTypeFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractPluggableFilterTranslator;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class IdmRequestIdentityRoleFilterTranslator extends AbstractPluggableFilterTranslator<IdmRequestIdentityRoleFilter> {

    @Autowired
    public IdmRequestIdentityRoleFilterTranslator(LookupService lookupService, ObjectMapper objectMapper) {
        super(lookupService, objectMapper);
    }

    @Override
    public IdmRequestIdentityRoleFilter transform(IdmRequestIdentityRoleFilter filter, MultiValueMap<String, Object> parameters) {
        filter.setIdentity(getParameterConverter().toEntityUuid(parameters, "identityId", IdmIdentityDto.class));
        filter.setIdentityContractId(getParameterConverter().toUuid(parameters, "identityContractId"));
        filter.setRoleId(getParameterConverter().toUuid(parameters, BaseRoleAssignmentFilter.PARAMETER_ROLE_ID));
        filter.setRoleText(getParameterConverter().toString(parameters, BaseRoleAssignmentFilter.PARAMETER_ROLE_TEXT));
        filter.setRoleEnvironments(getParameterConverter().toStrings(parameters, BaseRoleAssignmentFilter.PARAMETER_ROLE_ENVIRONMENT));
        filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
        filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
        filter.setOnlyChanges(getParameterConverter().toBoolean(parameters, "onlyChanges", false));
        filter.setIncludeCandidates(getParameterConverter().toBoolean(parameters, "includeCandidates", false));
        filter.setIncludeCrossDomainsSystemsCount(getParameterConverter().toBoolean(parameters, "includeCrossDomainsSystemsCount", false));
        filter.setOwnerType(getParameterConverter().toClass(parameters, OwnerTypeFilter.OWNER_TYPE));
        filter.set("accountId", getParameterConverter().toUuid(parameters, "accountId"));
        filter.setDirectRole(getParameterConverter().toBoolean(parameters, "directRole", false));
        filter.setAddEavMetadata(getParameterConverter().toBoolean(parameters, FormableFilter.PARAMETER_ADD_EAV_METADATA, false));
        return filter;
    }
}

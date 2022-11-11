package eu.bcvsolutions.idm.core.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * @author Roman Kucera
 */
public interface ApplicantService<DTO extends BaseDto, F extends BaseFilter> extends EventableDtoService<DTO, F>{

	/**
	 * Method finds all identity's managers by identity contract and return managers
	 *
	 * @param forIdentity
	 * @return identity managers
	 */
	default List<IdmIdentityDto> findAllManagers(UUID forIdentity) {
		return new ArrayList<>();
	}

	default List<? extends AbstractRoleAssignmentDto> getAllRolesForApplicant(UUID applicant, IdmBasePermission[] permissions) {
		return new ArrayList<>();
	}
}

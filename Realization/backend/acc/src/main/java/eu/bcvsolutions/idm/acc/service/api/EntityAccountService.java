package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

public interface EntityAccountService<DTO extends EntityAccountDto, F extends BaseFilter> extends ReadWriteDtoService<DTO, F>  {

	/**
	 * Delete entity node account
	 * @param entity
	 * @param deleteAccount  If true, then the account on the target system will be deleted (call provisioning).
	 */
	void delete(DTO entity, boolean deleteAccount, BasePermission... permission);

	default AbstractDto getOwner(DTO entity) {
		return null;
	}

	default AccAccountDto getAccount(DTO entity) {
		return null;
	}

}
package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Archived provisioning operation.
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningArchiveService extends 
		ReadWriteDtoService<SysProvisioningArchiveDto, SysProvisioningOperationFilter>,
		AuthorizableService<SysProvisioningArchiveDto>,
		ScriptEnabled {

	/**
	 * Archives provisioning operation
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	SysProvisioningArchiveDto archive(SysProvisioningOperationDto provisioningOperation);

	/**
	 * Optimize - system can be pre-loaded in DTO.
	 * 
	 * @param archive
	 * @return
	 */
	SysSystemDto getSystem(SysProvisioningArchiveDto archive);

	/**
	 * Creates differences of attributes with evaluated type of change.
	 * 
	 * @param currentObject
	 * @param changedObject
	 * @return
	 */
	List<SysAttributeDifferenceDto> evaluateProvisioningDifferences(IcConnectorObject currentObject, IcConnectorObject changedObject);
}

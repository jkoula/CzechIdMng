package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface EntityAccountManagementService<T> {
    /**
     * Return UID for this dto and roleSystem. First, the transform script
     * from the roleSystem attribute is found and used. If UID attribute
     * for the roleSystem is not defined, then default UID attribute handling
     * will be used.
     *
     * @param dto
     * @param roleSystem
     * @return
     */
    String generateUID(AbstractDto dto, SysRoleSystemDto roleSystem);
}

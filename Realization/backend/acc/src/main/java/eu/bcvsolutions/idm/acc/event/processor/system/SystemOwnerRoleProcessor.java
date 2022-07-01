package eu.bcvsolutions.idm.acc.event.processor.system;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * System owner by role processors should implement this interface.
 * 
 * @author Roman Kucera
 * @since 12.3.0
 */
public interface SystemOwnerRoleProcessor extends EntityEventProcessor<SysSystemOwnerRoleDto> {
	
}

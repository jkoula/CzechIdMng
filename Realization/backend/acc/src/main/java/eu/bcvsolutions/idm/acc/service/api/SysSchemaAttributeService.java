package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Schema attribute service
 * @author svandav
 *
 */
public interface SysSchemaAttributeService extends ReadWriteEntityService<SysSchemaAttribute, SchemaAttributeFilter>, CloneableService<SysSchemaAttribute> {

}

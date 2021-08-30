package eu.bcvsolutions.idm.acc.bulk.action.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * System group delete bulk action.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component(SystemGroupDeleteBulkAction.NAME)
@Description("System group delete bulk action.")
public class SystemGroupDeleteBulkAction extends AbstractRemoveBulkAction<SysSystemGroupDto, SysSystemGroupFilter> {

	public static final String NAME = "sys-system-group-delete-bulk-action";

	@Autowired private SysSystemGroupService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.SYSTEM_GROUP_DELETE);
	}

	@Override
	public ReadWriteDtoService<SysSystemGroupDto, SysSystemGroupFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}

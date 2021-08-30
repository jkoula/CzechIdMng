package eu.bcvsolutions.idm.acc.bulk.action.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * System group-system relation delete bulk action.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component(SystemGroupSystemDeleteBulkAction.NAME)
@Description("System group-system relation delete bulk action.")
public class SystemGroupSystemDeleteBulkAction extends AbstractRemoveBulkAction<SysSystemGroupSystemDto, SysSystemGroupSystemFilter> {

	public static final String NAME = "sys-system-group-system-delete-bulk-action";

	@Autowired
	private SysSystemGroupSystemService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.SYSTEM_GROUP_DELETE);
	}

	@Override
	public ReadWriteDtoService<SysSystemGroupSystemDto, SysSystemGroupSystemFilter> getService() {
		return service;
	}

	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}

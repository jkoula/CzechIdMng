package eu.bcvsolutions.idm.test.api;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;

/**
 * Test for bulk actions which created monitoring ignored flag.
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractMonitoringIgnoredBulkActionIntegrationTest<DTO extends AbstractDto> extends AbstractBulkActionTest {

	@Autowired private BulkActionManager bulkActionManager;
	@Autowired private EntityStateManager entityStateManager;
	
	/**
	 * Tested bulk action.
	 * 
	 * @return bulk action component
	 */
	protected abstract AbstractBulkAction<DTO, ?> getBulkAction();
	
	/**
	 * Create record to test.
	 * 
	 * @return persisted dto
	 */
	protected abstract DTO createDto();
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testCreateMonitoringIgnoredState() {
		DTO dto = createDto();
		
		IdmBulkActionDto bulkAction = bulkActionManager.toDto(getBulkAction());
		
		Set<UUID> ids = Sets.newHashSet(dto.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setOwnerId(dto.getId());
		filter.setResultCode(CoreResultCode.MONITORING_IGNORED.getCode());
		//
		Assert.assertFalse(entityStateManager.findStates(filter, null).isEmpty());
	}
}

package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Delete role catalogue test.
 *
 * @author Radek Tomiška
 */
public class RoleCatalogueDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmRoleCatalogueDto> roleCatalogues = this.createRoleCatalogues(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(roleCatalogues);
		bulkAction.setIdentifiers(this.getIdFromList(roleCatalogues));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmRoleCatalogueDto roleCatalogueDto = roleCatalogueService.get(id);
			Assert.assertNull(roleCatalogueDto);
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for delete role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		List<IdmRoleCatalogueDto> roleCatalogues = this.createRoleCatalogues(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(roleCatalogues);
		bulkAction.setIdentifiers(this.getIdFromList(roleCatalogues));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmRoleCatalogueDto roleCatalogueDto = roleCatalogueService.get(id);
			Assert.assertNotNull(roleCatalogueDto);
		}
	}
	
	@Test
	public void testPrevalidateWithSubItems() {
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();
		//
		// empty
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(roleCatalogue.getId()));
		bulkAction.getFormAttributes().stream().allMatch(a -> !a.getCode().equals(EntityEventProcessor.PROPERTY_FORCE_DELETE));
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		Assert.assertTrue(resultModels.getInfos().isEmpty());
		//
		// create sub item
		getHelper().createRoleCatalogue(null, roleCatalogue.getId());
		//
		// ~ force is not avaliable
		logout();
		loginWithout(TestHelper.ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.ROLECATALOGUE_ADMIN); // 
		bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(roleCatalogue.getId()));
		bulkAction.getFormAttributes().stream().allMatch(a -> !a.getCode().equals(EntityEventProcessor.PROPERTY_FORCE_DELETE));
		resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> infos = resultModels //
				.getInfos() //
				.stream() //
				.filter(info -> {
					return CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN.getCode()
							.equals(info.getStatusEnum());
				}).collect(Collectors.toList());
		Assert.assertEquals(1, infos.size());
		//
		// force is available
		logout();
		loginAsAdmin();
		bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(roleCatalogue.getId()));
		bulkAction.getFormAttributes().stream().anyMatch(a -> a.getCode().equals(EntityEventProcessor.PROPERTY_FORCE_DELETE));
		resultModels = bulkActionManager.prevalidate(bulkAction);
		infos = resultModels //
				.getInfos() //
				.stream() //
				.filter(info -> {
					return CoreResultCode.ROLE_CATALOGUE_FORCE_DELETE_HAS_CHILDREN.getCode()
							.equals(info.getStatusEnum());
				}).collect(Collectors.toList());
		Assert.assertEquals(1, infos.size());
	}
	
	@Test
	public void testForceDelete() {
		logout();
		loginAsAdmin();
		// create sub catalogues
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();
		IdmRoleCatalogueDto subRoleCatalogue = getHelper().createRoleCatalogue(null, roleCatalogue.getId());
		IdmRoleCatalogueDto subSubRoleCatalogue = getHelper().createRoleCatalogue(null, subRoleCatalogue.getId());
		IdmRoleCatalogueDto otherRoleCatalogue = getHelper().createRoleCatalogue();
		//
		// remove role catalogue	
		Map<String, Object> properties = new HashMap<>();
		properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
		// delete by bulk action
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleCatalogue.class, RoleCatalogueDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(roleCatalogue.getId()));
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, 0l, 0l);
		//
		Assert.assertNull(roleCatalogueService.get(roleCatalogue));
		Assert.assertNull(roleCatalogueService.get(subRoleCatalogue));
		Assert.assertNull(roleCatalogueService.get(subSubRoleCatalogue));
		Assert.assertNotNull(roleCatalogueService.get(otherRoleCatalogue));
	}

	private List<IdmRoleCatalogueDto> createRoleCatalogues(int count) {
		List<IdmRoleCatalogueDto> roles = new ArrayList<>(count);
		//
		for (int index = 0; index < count; index++) {
			roles.add(getHelper().createRoleCatalogue());
		}
		//
		return roles;
	}
}

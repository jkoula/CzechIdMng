package eu.bcvsolutions.idm.core.bulk.action.impl.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete tree type test.
 *
 * @author Radek Tomiška
 */
public class TreeTypeDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	
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
		List<IdmTreeTypeDto> treeTypes = this.createTreeTypes(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeType.class, TreeTypeDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(treeTypes);
		bulkAction.setIdentifiers(this.getIdFromList(treeTypes));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmTreeTypeDto treeType = treeTypeService.get(id);
			Assert.assertNull(treeType);
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for delete role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		List<IdmTreeTypeDto> treeTypes = this.createTreeTypes(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeType.class, TreeTypeDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(treeTypes);
		bulkAction.setIdentifiers(this.getIdFromList(treeTypes));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmTreeTypeDto treeType = treeTypeService.get(id);
			Assert.assertNotNull(treeType);
		}
	}
	
	@Test
	public void testForceDelete() {
		logout();
		loginAsAdmin();
		// create sub tree nodes, automatic roles, contract, contract positions
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode(treeType, null, null);
		IdmTreeNodeDto subTreeNode = getHelper().createTreeNode(treeType, (String) null, treeNode);
		IdmTreeNodeDto subSubTreeNode = getHelper().createTreeNode(treeType, (String) null, subTreeNode);
		IdmTreeNodeDto otherTreeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, subTreeNode);
		IdmContractPositionDto contractPosition = getHelper().createContractPosition(contract, subSubTreeNode);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityRoleDto assignedRoleOne = getHelper().createIdentityRole(contract, role);
		IdmIdentityRoleDto assignedRoleTwo = getHelper().createIdentityRole(contractPosition, role);
		IdmIdentityRoleDto assignedRoleOther = getHelper().createIdentityRole(getHelper().getPrimeContract(identity), role);
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, treeNode, RecursionType.DOWN, false);
		//
		Assert.assertEquals(5, identityRoleService.findAllByIdentity(identity.getId()).size()); // 3 manual, 2 automatic
		//
		// remove tree type	
		Map<String, Object> properties = new HashMap<>();
		properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
		// delete by bulk action
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeType.class, TreeTypeDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(treeType.getId()));
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, 0l, 0l);
		//
		Assert.assertNull(treeTypeService.get(treeType));
		Assert.assertNull(treeNodeService.get(treeNode));
		Assert.assertNull(treeNodeService.get(subTreeNode));
		Assert.assertNull(treeNodeService.get(subSubTreeNode));
		Assert.assertNull(treeNodeService.get(subSubTreeNode));
		Assert.assertNull(identityRoleService.get(assignedRoleOne));
		Assert.assertNull(identityRoleService.get(assignedRoleTwo));
		Assert.assertNull(identityContractService.get(contract));
		Assert.assertNull(contractPositionService.get(contractPosition));
		Assert.assertNull(roleTreeNodeService.get(automaticRole));
		//
		Assert.assertNotNull(treeNodeService.get(otherTreeNode));
		Assert.assertNotNull(getHelper().getPrimeContract(identity));
		Assert.assertNotNull(identityRoleService.get(assignedRoleOther));
	}

	private List<IdmTreeTypeDto> createTreeTypes(int count) {
		List<IdmTreeTypeDto> treeTypes = new ArrayList<>(count);
		//
		for (int index = 0; index < count; index++) {
			treeTypes.add(getHelper().createTreeType());
		}
		//
		return treeTypes;
	}
}

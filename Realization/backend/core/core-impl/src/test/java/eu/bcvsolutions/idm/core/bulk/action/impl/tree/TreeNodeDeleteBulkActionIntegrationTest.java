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
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete tree node test.
 *
 * @author Radek Tomiška
 */
public class TreeNodeDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmTreeNodeService treeNodeService;
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
		List<IdmTreeNodeDto> treeNodes = this.createTreeNodes(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeNode.class, TreeNodeDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(treeNodes);
		bulkAction.setIdentifiers(this.getIdFromList(treeNodes));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmTreeNodeDto treeNode = treeNodeService.get(id);
			Assert.assertNull(treeNode);
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for delete role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		List<IdmTreeNodeDto> treeNodes = this.createTreeNodes(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeNode.class, TreeNodeDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(treeNodes);
		bulkAction.setIdentifiers(this.getIdFromList(treeNodes));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmTreeNodeDto treeNode = treeNodeService.get(id);
			Assert.assertNotNull(treeNode);
		}
	}
	
	@Test
	public void testForceDelete() {
		logout();
		loginAsAdmin();
		// create sub tree nodes, automatic roles, contract, contract positions
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmTreeNodeDto subTreeNode = getHelper().createTreeNode((String) null, treeNode);
		IdmTreeNodeDto subSubTreeNode = getHelper().createTreeNode((String) null, subTreeNode);
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
		// remove tree node	
		Map<String, Object> properties = new HashMap<>();
		properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
		// delete by bulk action
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmTreeNode.class, TreeNodeDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(treeNode.getId()));
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, 0l, 0l);
		//
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

	private List<IdmTreeNodeDto> createTreeNodes(int count) {
		List<IdmTreeNodeDto> treeNodes = new ArrayList<>(count);
		//
		for (int index = 0; index < count; index++) {
			treeNodes.add(getHelper().createTreeNode());
		}
		//
		return treeNodes;
	}
}

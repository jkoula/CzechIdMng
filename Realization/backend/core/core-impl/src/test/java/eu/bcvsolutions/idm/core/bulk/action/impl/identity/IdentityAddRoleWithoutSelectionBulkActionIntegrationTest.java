package eu.bcvsolutions.idm.core.bulk.action.impl.identity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Assing role tests.
 *
 * @author Radek Tomiška
 *
 */
public class IdentityAddRoleWithoutSelectionBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.READ, ContractBasePermission.CHANGEPERMISSION);
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.ROLEREQUEST, IdmRoleRequest.class, IdmBasePermission.ADMIN);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(5);
		IdmRoleDto createRole = getHelper().createRole();
		IdmRoleDto createRole2 = getHelper().createRole();
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAddRoleWithoutSelectionBulkAction.NAME);
		
		LocalDate validTill = LocalDate.now().minusDays(5);
		LocalDate validFrom = LocalDate.now().plusDays(60);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_ROLE, Lists.newArrayList(createRole.getId().toString(), createRole2.getId().toString()) );
		properties.put(
				IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_IDENTITY, 
				identities.stream().map(IdmIdentityDto::getId).collect(Collectors.toList())
		);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_VALID_FROM, validFrom);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_VALID_TILL, validTill);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_PRIME_CONTRACT, Boolean.TRUE);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_APPROVE, Boolean.FALSE);
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 10l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			identity = identityService.get(identity);
			Assert.assertNotNull(identity);
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			Assert.assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			Assert.assertEquals(2, identityRoles.size());
			
			for (IdmIdentityRoleDto identityRole : identityRoles) {
				Assert.assertEquals(identityRole.getValidFrom(), validFrom);
				Assert.assertEquals(identityRole.getValidTill(), validTill);
				
				boolean existsRole = false;
				if (identityRole.getRole().equals(createRole.getId()) || identityRole.getRole().equals(createRole2.getId())) {
					existsRole = true;
				}
				Assert.assertTrue(existsRole);
			}
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for change permission identity
		IdmIdentityDto identityForLogin = getHelper().createIdentity();
		
		IdmRoleDto permissionRole = getHelper().createRole();
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ);
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.IDENTITYCONTRACT, IdmIdentityContract.class, IdmBasePermission.AUTOCOMPLETE);
		
		getHelper().createIdentityRole(identityForLogin, permissionRole);
		loginAsNoAdmin(identityForLogin.getUsername());
		
		IdmRoleDto createRole = getHelper().createRole();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAddRoleWithoutSelectionBulkAction.NAME);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_ROLE, Lists.newArrayList(createRole.getId().toString()));
		properties.put(
				IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_IDENTITY, 
				identities.stream().map(IdmIdentityDto::getId).collect(Collectors.toList())
		);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_PRIME_CONTRACT, Boolean.TRUE);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_APPROVE, Boolean.FALSE);
		bulkAction.setProperties(properties);
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (IdmIdentityDto identity : identities) {
			identity = identityService.get(identity);
			
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			Assert.assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			Assert.assertTrue(identityRoles.isEmpty());
		}
	}
	
	@Test
	public void processBulkActionWithoutContracts() {
		IdmRoleDto createRole = getHelper().createRole();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			for (IdmIdentityContractDto contract : identityContractService.findAllByIdentity(identity.getId())) {
				identityContractService.delete(contract);
			}
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAddRoleWithoutSelectionBulkAction.NAME);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_ROLE, Lists.newArrayList(createRole.getId().toString()));
		properties.put(
				IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_IDENTITY, 
				identities.stream().map(IdmIdentityDto::getId).collect(Collectors.toList())
		);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_PRIME_CONTRACT, Boolean.FALSE);
		properties.put(IdentityAddRoleWithoutSelectionBulkAction.PROPERTY_APPROVE, Boolean.FALSE);
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 0l);
		
		for (IdmIdentityDto identity : identities) {
			identity = identityService.get(identity);
			
			List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
			Assert.assertEquals(0, contracts.size());
		}
	}
}

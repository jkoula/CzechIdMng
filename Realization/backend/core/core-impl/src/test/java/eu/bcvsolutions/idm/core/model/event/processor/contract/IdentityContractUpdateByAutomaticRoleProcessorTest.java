package eu.bcvsolutions.idm.core.model.event.processor.contract;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAllAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class IdentityContractUpdateByAutomaticRoleProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	
	@MockBean
	private Clock clock;
	
	@Test
	public void testRoleValidityChange() {
		ProcessAllAutomaticRoleByAttributeTaskExecutor autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		contract.setValidFrom(LocalDate.now().minusDays(5));
		contract.setValidTill(LocalDate.now().plusDays(5));
		contract = identityContractService.save(contract);
		//
		IdmAutomaticRoleAttributeDto automaticRole = this.getHelper().createAutomaticRole(this.getHelper().createRole().getId());
		this.getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		List<IdmIdentityRoleDto> findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidFrom(), findAllByIdentity.get(0).getValidFrom());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
		//
		contract.setValidFrom(LocalDate.now().minusDays(7));
		contract.setValidTill(LocalDate.now().plusDays(7));
		contract = identityContractService.save(contract);
		//
		findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidFrom(), findAllByIdentity.get(0).getValidFrom());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
	}
	
	@Test
	public void testRoleValidityChangeLastDay() {
		ProcessAllAutomaticRoleByAttributeTaskExecutor autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		contract.setValidFrom(LocalDate.now().minusDays(5));
		contract.setValidTill(LocalDate.now().plusDays(5));
		contract = identityContractService.save(contract);
		//
		IdmAutomaticRoleAttributeDto automaticRole = this.getHelper().createAutomaticRole(this.getHelper().createRole().getId());
		this.getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		List<IdmIdentityRoleDto> findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidFrom(), findAllByIdentity.get(0).getValidFrom());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
		//
		contract.setValidFrom(LocalDate.now().minusDays(7));
		contract.setValidTill(LocalDate.now().plusDays(7));
		contract = identityContractService.save(contract);
		//
		findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidFrom(), findAllByIdentity.get(0).getValidFrom());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
	}
}

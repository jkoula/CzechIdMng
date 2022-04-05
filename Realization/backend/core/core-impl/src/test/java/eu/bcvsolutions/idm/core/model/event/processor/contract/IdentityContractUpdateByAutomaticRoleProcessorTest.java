package eu.bcvsolutions.idm.core.model.event.processor.contract;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAllAutomaticRoleByAttributeTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessSkippedAutomaticRoleByTreeForContractTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class IdentityContractUpdateByAutomaticRoleProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private EntityStateManager entityStateManager;
	
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
		contract.setValidFrom(LocalDate.now());
		contract.setValidTill(LocalDate.now().plusDays(5L));
		contract = identityContractService.save(contract);
		//
		IdmAutomaticRoleAttributeDto automaticRole = this.getHelper().createAutomaticRole(this.getHelper().createRole().getId());
		this.getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, identity.getUsername());
		//
		executeAutomaticRoleRecalculation();
		//
		List<IdmIdentityRoleDto> findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
		//
		contract.setValidTill(LocalDate.now());
		contract = saveContractAsInSynchronization(contract);
		executeAutomaticRoleRecalculation();
		findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
		//
		contract.setValidTill(LocalDate.now().minusDays(1L));
		contract = identityContractService.saveInternal(contract); // skip all processors
		//
		contract.setValidTill(LocalDate.now().plusDays(5));
		contract = saveContractAsInSynchronization(contract);
		executeAutomaticRoleRecalculation();
		//
		findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, findAllByIdentity.size());
		assertEquals(contract.getValidTill(), findAllByIdentity.get(0).getValidTill());
	}
	
	private IdmIdentityContractDto saveContractAsInSynchronization(IdmIdentityContractDto entity) {
		EntityEvent<IdmIdentityContractDto> event = new IdentityContractEvent(
				IdentityContractEventType.UPDATE,
				entity);
		// We do not want execute HR processes for every contract. We need start
		// them for every identity only once.
		// For this we skip them now. HR processes must be start after whole
		// sync finished (by using dependent scheduled task)!
		event.getProperties().put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
		//
		// We don't want recalculate automatic role by attribute recalculation for every
		// contract.
		// Recalculation will be started only once.
		event.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);

		EventContext<IdmIdentityContractDto> publishContext = identityContractService.publish(event);
		IdmIdentityContractDto contract = publishContext.getContent();
		
		// We need to flag recalculation for contract immediately to prevent synchronization ends before flag is created by NOTIFY event asynchronously.
		Map<String, Serializable> properties = new HashMap<>();
		EventResult<IdmIdentityContractDto> lastResult = publishContext.getLastResult();
		if (lastResult != null) {
			// original contract as property
			properties.put(EntityEvent.EVENT_PROPERTY_ORIGINAL_SOURCE, lastResult.getEvent().getOriginalSource());
		}
		if (contract.isValidNowOrInFuture()) {
			entityStateManager.createState(contract, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, properties);
		} else {
			entityStateManager.createState(contract, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED_INVALID_CONTRACT, properties);
		}
		//
		return contract;
	}
	
	private void executeAutomaticRoleRecalculation() {
		longRunningTaskManager.executeSync(new ProcessAllAutomaticRoleByAttributeTaskExecutor());
		longRunningTaskManager.executeSync(new ProcessSkippedAutomaticRoleByTreeForContractTaskExecutor());;
	}
}

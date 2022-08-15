package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for Entity filter.
 * 
 * TODO: move to rest test
 * 
 * @author Patrik Stloukal
 *
 */
@Transactional
public class DefaultSysSystemEntityServiceFilterTest extends AbstractIntegrationTest {

	@Autowired private SysSystemEntityService entityService;
	@Autowired private TestHelper helper;

	@Test
	public void testSystemId() {
		SysSystemDto system1 = helper.createTestResourceSystem(false);
		SysSystemDto system2 = helper.createTestResourceSystem(false);
		createEntitySystem("test1-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system1.getId(),
				UUID.randomUUID());
		SysSystemEntityDto entity2 = createEntitySystem("test2-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE,
				system2.getId(), UUID.randomUUID());
		createEntitySystem("test3-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system2.getId(),
				UUID.randomUUID());
		//
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();
		testFilter.setSystemId(system2.getId());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(2, pages.getTotalElements());
		
		SysSystemEntityDto foundedSystem = pages.getContent().stream().filter(sys -> sys.getId().equals(entity2.getId())).findAny().get();
		assertNotNull(foundedSystem);
	}

	@Test
	public void testUid() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		SysSystemEntityDto entity1 = createEntitySystem("test1-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE,
				system.getId(), UUID.randomUUID());
		createEntitySystem("test2-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		createEntitySystem("test3-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		//
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();
		testFilter.setUid(entity1.getUid());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(entity1.getId(), pages.getContent().get(0).getId());
	}
	
	@Test
	public void testFindByText() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		SysSystemEntityDto entityOne = createEntitySystem(getHelper().createName(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE,
				system.getId(), UUID.randomUUID());
		createEntitySystem(getHelper().createName(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		createEntitySystem(getHelper().createName(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		//
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();
		testFilter.setText(entityOne.getUid());
		List<SysSystemEntityDto> results = entityService.find(testFilter, null).getContent();
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(entityOne.getId(), results.get(0).getId());
	}

	/**
	 * Id in {@link SysSystemEntityFilter} not currently implemented. Use get by id.
	 */
	@Test
	public void testId() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		createEntitySystem("test1-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		createEntitySystem("test2-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		SysSystemEntityDto entity3 = createEntitySystem("test3-" + System.currentTimeMillis(), ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE,
				system.getId(), UUID.randomUUID());
		//
		SysSystemEntityDto foundedEntity = entityService.get(entity3.getId());
		assertEquals(entity3.getId(), foundedEntity.getId());
	}

	@Test
	public void testEntityType() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		createEntitySystem("test1-" + System.currentTimeMillis(), RoleSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		SysSystemEntityDto entity2 = createEntitySystem("test2-" + System.currentTimeMillis(), TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE,
				system.getId(), UUID.randomUUID());
		createEntitySystem("tes3t-" + System.currentTimeMillis(), IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getId(),
				UUID.randomUUID());
		//
		SysSystemEntityFilter testFilter = new SysSystemEntityFilter();
		testFilter.setEntityType(entity2.getEntityType());
		testFilter.setUid(entity2.getUid());
		Page<SysSystemEntityDto> pages = entityService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(entity2.getId(), pages.getContent().get(0).getId());
	}

	/**
	 * Create {@link SysSystemEntityDto}
	 * @param uid
	 * @param type
	 * @param systemId
	 * @param id
	 * @return
	 */
	private SysSystemEntityDto createEntitySystem(String uid, String type, UUID systemId, UUID id) {
		SysSystemEntityDto entity = new SysSystemEntityDto();
		entity.setUid(uid);
		entity.setEntityType(type);
		entity.setSystem(systemId);
		entity.setId(id);
		entityService.save(entity);
		return entity;
	}
}

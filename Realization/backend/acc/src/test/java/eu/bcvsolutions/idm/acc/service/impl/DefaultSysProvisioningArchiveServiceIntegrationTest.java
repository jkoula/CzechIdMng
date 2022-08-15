package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SysValueChangeType;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceValueDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters.
 * 
 * TODO: move filters into rest test.
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
public class DefaultSysProvisioningArchiveServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningOperationService operationService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysProvisioningAttributeRepository provisioningAttributeRepository;
	//
	@Autowired private SysProvisioningArchiveService service;
	
	@Test
	@Transactional
	public void testReferentiralIntegrity() {
		SysSystemDto system = createSystem();
		SysProvisioningArchiveDto archiveOne = createProvisioningArchive(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto archiveTwo = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningAttribute attributeOne = new SysProvisioningAttribute(archiveOne.getId(), getHelper().createName());
		attributeOne = provisioningAttributeRepository.save(attributeOne);
		SysProvisioningAttribute attributeTwo = new SysProvisioningAttribute(archiveTwo.getId(), getHelper().createName());
		attributeTwo = provisioningAttributeRepository.save(attributeTwo);
		//
		service.delete(archiveOne);
		//
		Assert.assertFalse(provisioningAttributeRepository.existsById(attributeOne.getId()));
		Assert.assertTrue(provisioningAttributeRepository.existsById(attributeTwo.getId()));
	}

	@Test
	@Transactional
	public void typeFilterTest() {
		SysSystemDto system = createSystem();

		SysProvisioningArchiveDto provisioningOperation1 = createProvisioningArchive(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto provisioningOperation2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto provisioningOperation3 = createProvisioningArchive(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertTrue(result.getContent().contains(provisioningOperation3));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	@Transactional
	public void operationTypeFilterTest() {
		SysSystemDto system = createSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningArchive2.setOperationType(ProvisioningEventType.UPDATE);
		service.save(provisioningArchive2);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningArchive3.setOperationType(ProvisioningEventType.UPDATE);
		service.save(provisioningArchive3);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setOperationType(ProvisioningEventType.UPDATE);
		filter.setSystemId(system.getId());

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertTrue(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void systemIdFilterTest() {
		SysSystemDto system1 = createSystem();
		SysSystemDto system2 = createSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system1);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system1.getId());

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void systemEntityUidFilterTest() {
		SysSystemDto system = createSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntityUid(provisioningArchive1.getSystemEntityUid());

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void entityIdentifierFilterTest() {
		SysSystemDto system = createSystem();

		createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningArchive1.setEntityIdentifier(UUID.randomUUID());
		provisioningArchive1 = service.save(provisioningArchive1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(provisioningArchive1.getEntityIdentifier());

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
	}

	@Test
	@Transactional
	public void resultStateFilterTest() {
		SysSystemDto system = createSystem();

		OperationResult resultState = new OperationResult();
		resultState.setState(OperationState.CREATED);

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningArchive2.setResult(resultState);
		service.save(provisioningArchive2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setResultState(OperationState.CREATED);

		Page<SysProvisioningArchiveDto> result = service.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
	}
	
	@Test
	public void testOperationArchivate() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		SysProvisioningOperationDto operation = new SysProvisioningOperationDto();
		UUID mockIdentityId = UUID.randomUUID();
		operation.setSystem(system.getId());
		operation.setEntityIdentifier(UUID.randomUUID());
		operation.setOperationType(ProvisioningEventType.CANCEL);
		operation.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		operation.setProvisioningContext(new ProvisioningContext());
		operation.setResult(new OperationResult(OperationState.CANCELED));
		operation.setSystemEntity(getHelper().createSystemEntity(system).getId());
		operation.setCreator(mockIdentityId.toString());
		operation.setCreatorId(mockIdentityId);
		operation.setOriginalCreator(mockIdentityId.toString());
		operation.setOriginalCreatorId(mockIdentityId);
		//
		operation = operationService.save(operation);
		//
		Assert.assertNotNull(operation.getSystem());
		Assert.assertNotNull(operation.getCreated());
		Assert.assertEquals(mockIdentityId.toString(), operation.getCreator());
		Assert.assertEquals(mockIdentityId, operation.getCreatorId());
		Assert.assertEquals(mockIdentityId.toString(), operation.getOriginalCreator());
		Assert.assertEquals(mockIdentityId, operation.getOriginalCreatorId());
		//
		getHelper().waitForResult(null, 30, 1);
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		Assert.assertEquals(operation.getCreated(), archive.getCreated());
		Assert.assertNotNull(archive.getModified());
		Assert.assertNotEquals(operation.getCreated(), archive.getModified());
		Assert.assertEquals(mockIdentityId.toString(), archive.getCreator());
		Assert.assertEquals(mockIdentityId.toString(), archive.getCreator());
		Assert.assertEquals(mockIdentityId, archive.getCreatorId());
		Assert.assertEquals(mockIdentityId.toString(), archive.getOriginalCreator());
		Assert.assertEquals(mockIdentityId, archive.getOriginalCreatorId());
	}
	
	@Test
	public void testOperationArchivateWithSingleUpdatedAttribute() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), "valueOne");
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertFalse(attributes.get(0).isRemoved());
	}
	
	@Test
	public void testOperationArchivateWithMultiUpdatedAttribute() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), Lists.newArrayList("valueOne", "valueTwo"));
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertFalse(attributes.get(0).isRemoved());
	}
	
	@Test
	public void testOperationArchivateWithMultiRemovedAttribute() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), Lists.newArrayList());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertTrue(attributes.get(0).isRemoved());
	}
	
	@Test
	public void testOperationArchivateWithMultiRemovedAttributeWithEmptyValue() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), Lists.newArrayList(""));
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertTrue(attributes.get(0).isRemoved());
	}
	
	@Test
	public void testOperationArchivateWithMultiRemovedAttributeWithNullValue() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), Lists.newArrayList((String)null));
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertTrue(attributes.get(0).isRemoved());
	}
	
	@Test
	public void testOperationArchivateWithSingleRemovedAttributeWithNullValue() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		//
		IcAttribute icAttributeOne = new IcAttributeImpl(getHelper().createName(), null);
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		//
		SysProvisioningOperationDto operation = prepareProvisioningOperation(system);
		operation.getProvisioningContext().setConnectorObject(connectorObject);
		//
		SysProvisioningArchiveDto archive = service.archive(operation);
		//
		List<SysProvisioningAttribute> attributes = provisioningAttributeRepository.findAllByProvisioningId(archive.getId());
		//
		Assert.assertEquals(1, attributes.size());
		Assert.assertEquals(icAttributeOne.getName(), attributes.get(0).getName());
		Assert.assertTrue(attributes.get(0).isRemoved());
	}

	@Test
	public void differenceObjectAddSingleTest () {
		String attrName = getHelper().createName();
		IcAttributeImpl icAttributeOne = new IcAttributeImpl(attrName, "TEST1");
		IcConnectorObject connObjectNew = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		
		IcConnectorObject connObjectOld = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of());
				
		
		List<SysAttributeDifferenceDto> diffs = service.evaluateProvisioningDifferences(connObjectOld, connObjectNew);
		Assert.assertEquals(1, diffs.size());
		Assert.assertFalse(diffs.get(0).isMultivalue());
		Assert.assertEquals(SysValueChangeType.ADDED, diffs.get(0).getValue().getChange());
		Assert.assertEquals(null, diffs.get(0).getValue().getOldValue());
		Assert.assertEquals("TEST1", diffs.get(0).getValue().getValue());
	}

	@Test
	public void differenceObjectUpdateSingleTest () {
		String attrName = getHelper().createName();
		IcAttributeImpl icAttributeOne = new IcAttributeImpl(attrName, "TEST1");
		IcConnectorObject connObjectOld = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		
		IcAttributeImpl icAttributeTwo = new IcAttributeImpl(attrName, "TEST2");
		IcConnectorObject connObjectNew = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeTwo));
		
		List<SysAttributeDifferenceDto> diffs = service.evaluateProvisioningDifferences(connObjectOld, connObjectNew);
		Assert.assertEquals(1, diffs.size());
		Assert.assertFalse(diffs.get(0).isMultivalue());
		Assert.assertEquals(SysValueChangeType.UPDATED, diffs.get(0).getValue().getChange());
		Assert.assertEquals("TEST1", diffs.get(0).getValue().getOldValue());
		Assert.assertEquals("TEST2", diffs.get(0).getValue().getValue());
	}

	@Test
	public void differenceObjectMultipleTest () {
		String attrName = getHelper().createName();
		IcAttributeImpl icAttributeOne = new IcAttributeImpl(attrName, Lists.newArrayList("TESTExisting", "TESTRemoved"));
		IcConnectorObject connObjectOld = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeOne));
		
		IcAttributeImpl icAttributeTwo = new IcAttributeImpl(attrName, Lists.newArrayList("TESTExisting", "TESTNew"));
		IcConnectorObject connObjectNew = new IcConnectorObjectImpl(getHelper().createName(),
				new IcObjectClassImpl("__mock__"), ImmutableList.of(icAttributeTwo));
		
		List<SysAttributeDifferenceDto> diffs = service.evaluateProvisioningDifferences(connObjectOld, connObjectNew);
		List<SysAttributeDifferenceValueDto> values = diffs.get(0).getValues();
		Assert.assertEquals(1, diffs.size());
		Assert.assertTrue(diffs.get(0).isMultivalue());
		Assert.assertTrue(diffs.get(0).isChanged());
		Assert.assertNotNull(values);
		Assert.assertEquals(3, values.size());
		
		SysAttributeDifferenceValueDto value;
		value = values.stream().filter(item-> item.getChange() == null).findFirst().orElse(null);
		Assert.assertNotNull(value);
		Assert.assertEquals("TESTExisting", value.getValue());
		Assert.assertEquals("TESTExisting", value.getOldValue());
		
		value = values.stream().filter(item-> item.getChange() == SysValueChangeType.ADDED).findFirst().orElse(null);
		Assert.assertNotNull(value);
		Assert.assertEquals("TESTNew", value.getValue());
		Assert.assertEquals(null, value.getOldValue());
		
		value = values.stream().filter(item-> item.getChange() == SysValueChangeType.REMOVED).findFirst().orElse(null);
		Assert.assertNotNull(value);
		Assert.assertEquals("TESTRemoved", value.getValue());
		Assert.assertEquals("TESTRemoved", value.getOldValue());
	}

	private SysSystemDto createSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName(getHelper().createName());
		//
		return systemService.save(system);
	}

	private SysProvisioningArchiveDto createProvisioningArchive(String type, SysSystemDto system) {
		SysProvisioningArchiveDto provisioningArchive = new SysProvisioningArchiveDto();
		provisioningArchive.setEntityType(type);
		provisioningArchive.setOperationType(ProvisioningEventType.CREATE);
		provisioningArchive.setProvisioningContext(new ProvisioningContext());
		provisioningArchive.setSystem(system.getId());
		provisioningArchive.setSystemEntityUid("SomeEntityUID" + UUID.randomUUID());

		OperationResult result = new OperationResult();
		result.setState(OperationState.RUNNING);
		provisioningArchive.setResult(result);

		return service.save(provisioningArchive);
	}
	
	private SysProvisioningOperationDto prepareProvisioningOperation(SysSystemDto system) {
		SysProvisioningOperationDto operation = new SysProvisioningOperationDto();
		operation.setSystem(system.getId());
		operation.setEntityIdentifier(UUID.randomUUID());
		operation.setOperationType(ProvisioningEventType.CANCEL);
		operation.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		operation.setProvisioningContext(new ProvisioningContext());
		operation.setResult(new OperationResult(OperationState.CANCELED));
		operation.setSystemEntity(getHelper().createSystemEntity(system).getId());
		//
		return operation;
	}
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}

}

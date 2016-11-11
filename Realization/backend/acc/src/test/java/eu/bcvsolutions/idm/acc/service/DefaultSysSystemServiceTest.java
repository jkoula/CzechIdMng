package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttributeDefinition;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSysSystemServiceTest extends AbstractIntegrationTest {
	
	private static final String SYSTEM_NAME = "test_system_" + System.currentTimeMillis();
	
	@Autowired
	private SysSystemService sysSystemService;
	
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	
	@Autowired
	private IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository;

	/**
	 * Test add and delete extended attributes
	 */
	@Test
	public void testFromAttributes() {
		SysSystem system = new SysSystem();
		system.setName(SYSTEM_NAME);
		
		sysSystemService.save(system);
		
		SysSystem savedSystem = sysSystemService.getByName(SYSTEM_NAME);
		
		assertEquals(SYSTEM_NAME, savedSystem.getName());
		
		IdmFormDefinition formDefinition = new IdmFormDefinition();
		formDefinition.setName(SysSystem.class.getCanonicalName());
		formDefinition = formDefinitionService.save(formDefinition);
		
		IdmFormAttributeDefinition attributeDefinition = new IdmFormAttributeDefinition();
		attributeDefinition.setFormDefinition(formDefinition);
		attributeDefinition.setName("name_" + System.currentTimeMillis());
		attributeDefinition.setDisplayName(attributeDefinition.getName());
		attributeDefinition.setPersistentType(PersistentType.TEXT);			
		attributeDefinition = formAttributeDefinitionRepository.save(attributeDefinition);
	}
}

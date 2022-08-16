package eu.bcvsolutions.idm.rpt.report.provisioning;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.rpt.acc.TestHelper;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Report for comparison values in IdM and system test.
 * 
 * @author Radek Tomiška
 */
public class ChangesOnSystemReportIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ChangesOnSystemReportExecutor reportExecutor;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private ChangesOnSystemReportXlsxRenderer xlsxRenderer;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemAttributeMappingService attributeMappingService;
	@Autowired private SysSystemService systemService;
	@Autowired private IdmIdentityService identityService;
	
	public TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
	
	@Before
	public void before() {
		getHelper().loginAdmin();
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	public void testProvisioningOperationReportWithoutIdentities() throws IOException {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto position = getHelper().createTreeNode();
		getHelper().createContract(identityOne, position);
		SysSystemDto system = createSystemWithOperation();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		getHelper().createIdentityRole(identityTwo, role);
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identityTwo.getId());
		accountFilter.setSystemId(system.getId());
		accountFilter.setEntityType(SystemEntityType.IDENTITY);
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, accounts.size());
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto filterValue = new IdmFormValueDto(definition.getMappedAttributeByCode(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES));
		// TODO: create json java POJO representation
		filterValue.setStringValue("{ \"system\": \"" + system.getId() + "\", \"systemMapping\": \"" + getHelper().getDefaultMapping(system).getId() + "\", \"mappingAttributes\": [] }");
		filter.getValues().add(filterValue);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		//
		// test renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testProvisioningOperationReportWithIdentities() throws IOException {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto position = getHelper().createTreeNode();
		getHelper().createContract(identityTwo, position);
		SysSystemDto system = createSystemWithOperation();
		SysSystemMappingDto defaultMapping = getHelper().getDefaultMapping(system);
		SysSystemAttributeMappingDto attributeName = attributeMappingService
				.findBySystemMappingAndName(defaultMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		getHelper().createIdentityRole(identityTwo, role);
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identityTwo.getId());
		accountFilter.setSystemId(system.getId());
		accountFilter.setEntityType(SystemEntityType.IDENTITY);
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, accounts.size());
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto filterValue = new IdmFormValueDto(definition.getMappedAttributeByCode(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES));
		// TODO: create json java POJO representation
		filterValue.setStringValue("{ \"system\": \"" + system.getId() + "\", \"systemMapping\": \"" + defaultMapping.getId() + "\", \"mappingAttributes\": [ \"" + attributeName.getId() + "\" ] }");
		filter.getValues().add(filterValue);
		IdmFormValueDto filterIdentitites = new IdmFormValueDto(definition.getMappedAttributeByCode(ChangesOnSystemReportExecutor.PARAMETER_ONLY_IDENTITY));
		filterIdentitites.setUuidValue(identityOne.getId());
		filter.getValues().add(filterIdentitites);
		IdmFormValueDto filterTreeNode = new IdmFormValueDto(definition.getMappedAttributeByCode(ChangesOnSystemReportExecutor.PARAMETER_TREE_NODE));
		filterTreeNode.setUuidValue(position.getId());
		filter.getValues().add(filterTreeNode);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		//
		// test renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	
	@Test
	public void testProvisioningOperationReportWithChanges() throws IOException {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto position = getHelper().createTreeNode();
		getHelper().createContract(identityTwo, position);
		SysSystemDto system = createSystemWithOperation();
		SysSystemMappingDto defaultMapping = getHelper().getDefaultMapping(system);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		getHelper().createIdentityRole(identityTwo, role);
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identityTwo.getId());
		accountFilter.setSystemId(system.getId());
		accountFilter.setEntityType(SystemEntityType.IDENTITY);
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, accounts.size());
		//
		// change => readonly system
		system.setDisabledProvisioning(true);
		system.setReadonly(true);
		system = systemService.save(system);
		identityTwo.setLastName(getHelper().createName());
		identityTwo = identityService.save(identityTwo);
		system.setDisabledProvisioning(false);
		system.setReadonly(false);
		system = systemService.save(system);
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto filterValue = new IdmFormValueDto(definition.getMappedAttributeByCode(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES));
		// TODO: create json java POJO representation
		filterValue.setStringValue("{ \"system\": \"" + system.getId() + "\", \"systemMapping\": \"" + defaultMapping.getId() + "\", \"mappingAttributes\": [ ] }");
		filter.getValues().add(filterValue);
		IdmFormValueDto filterIdentitites = new IdmFormValueDto(definition.getMappedAttributeByCode(ChangesOnSystemReportExecutor.PARAMETER_ONLY_IDENTITY));
		filterIdentitites.setUuidValue(identityOne.getId());
		filter.getValues().add(filterIdentitites);
		IdmFormValueDto filterTreeNode = new IdmFormValueDto(definition.getMappedAttributeByCode(ChangesOnSystemReportExecutor.PARAMETER_TREE_NODE));
		filterTreeNode.setUuidValue(position.getId());
		filter.getValues().add(filterTreeNode);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		//
		// test renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}

	private SysSystemDto createSystemWithOperation() {
		SysSystemDto systemOne = getHelper().createTestResourceSystem(true);
		//
		// create provisioning operation
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, systemOne);
		getHelper().createIdentityRole(getHelper().createIdentity(), role);
		//
		return systemOne;
	}
}

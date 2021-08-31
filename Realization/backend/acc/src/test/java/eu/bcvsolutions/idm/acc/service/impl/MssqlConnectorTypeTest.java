package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.connector.AbstractJdbcConnectorType;
import eu.bcvsolutions.idm.acc.connector.MsSqlConnectorType;
import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockMsSqlConnectorType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for MS SQL connector type.
 * Tests are use only if environment use MS SQL database. Otherwise are these tests skipped.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Transactional
public class MssqlConnectorTypeTest extends AbstractJdbcConnectorTypeTest {

	@Autowired
	@Qualifier(MockMsSqlConnectorType.NAME)
	private MockMsSqlConnectorType mockMsSqlConnectorType;

	@Test
	@Override
	public void testJdbcFirstStep() {
		super.testJdbcFirstStep();
	}

	@Test
	@Override
	public void testReopenSystemInWizard() {
		super.testReopenSystemInWizard();
	}

	@Test
	public void testAdditionalMSSQLAttributes() {
		// Check if current running environment use driver same as this test.
		// If not, whole test will be skipped.
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			// Skip test.
			//return;
		}
		ConnectorTypeDto connectorTypeDto = getConnectorTypeDto();
		connectorTypeDto.setReopened(false);

		ConnectorTypeDto jdbcConnectorTypeDto = connectorManager.load(connectorTypeDto);
		assertNotNull(jdbcConnectorTypeDto);

		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.HOST, this.getHost());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PORT, this.getPort());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.DATABASE, this.getDatabase());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.USER, this.getUsername());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PASSWORD, this.getPassword());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.TABLE, "idm_identity");
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.KEY_COLUMN, "username");
		jdbcConnectorTypeDto.setWizardStepName(AbstractJdbcConnectorType.STEP_ONE_CREATE_SYSTEM);

		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.AUTHENTICATION_TYPE_KEY, MsSqlConnectorType.WINDOWS_AUTHENTICATION_TYPE);
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.TRUST_SERVER_CRT_SWITCH, Boolean.TRUE.toString());
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.NTLM_SWITCH, Boolean.TRUE.toString());
		String domain = getHelper().createName();
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.DOMAIN_KEY, domain);
		String instanceName = getHelper().createName();
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.INSTANCE_NAME_KEY, instanceName);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(jdbcConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(AbstractJdbcConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		// Load connector properties from created system.
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(systemDto);
		assertEquals("net.tirasa.connid.bundles.db.table.DatabaseTableConnector",
				connectorInstance.getConnectorKey().getConnectorName());

		IdmFormDefinitionDto connectorFormDef = this.systemService.getConnectorFormDefinition(systemDto);
		String jdbcUrlTemplate = getValueFromConnectorInstance(AbstractJdbcConnectorType.JDBC_URL_TEMPLATE, systemDto, connectorFormDef);
		// Check Windows auth.
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.WINDOWS_AUTHENTICATION_TYPE_TEMPLATE));
		// Check trust CRT.
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.TRUST_SERVER_CRT_TEMPLATE));
		// Check NTLM.
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.NTLM_TEMPLATE));
		// Check Domain.
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.DOMAIN_TEMPLATE + domain));
		// Check instance name.
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.INSTANCE_NAME_TEMPLATE + instanceName));

		// Delete created system.
		systemService.delete(systemDto);
	}

	@Test
	public void testUpdateAdditionalMSSQLAttributes() {
		// Check if current running environment use driver same as this test.
		// If not, whole test will be skipped.
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			// Skip test.
			//return;
		}
		ConnectorTypeDto connectorTypeDto = getConnectorTypeDto();
		connectorTypeDto.setReopened(false);

		ConnectorTypeDto jdbcConnectorTypeDto = connectorManager.load(connectorTypeDto);
		assertNotNull(jdbcConnectorTypeDto);

		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.HOST, this.getHost());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PORT, this.getPort());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.DATABASE, this.getDatabase());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.USER, this.getUsername());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.PASSWORD, this.getPassword());
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.TABLE, "idm_identity");
		jdbcConnectorTypeDto.getMetadata().put(AbstractJdbcConnectorType.KEY_COLUMN, "username");
		jdbcConnectorTypeDto.setWizardStepName(AbstractJdbcConnectorType.STEP_ONE_CREATE_SYSTEM);

		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.AUTHENTICATION_TYPE_KEY, MsSqlConnectorType.WINDOWS_AUTHENTICATION_TYPE);
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.TRUST_SERVER_CRT_SWITCH, Boolean.TRUE.toString());
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.NTLM_SWITCH, Boolean.TRUE.toString());
		String domain = getHelper().createName();
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.DOMAIN_KEY, domain);
		String instanceName = getHelper().createName();
		jdbcConnectorTypeDto.getMetadata().put(MsSqlConnectorType.INSTANCE_NAME_KEY, instanceName);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(jdbcConnectorTypeDto);

		// The system had to be created.
		BaseDto system = stepExecutedResult.getEmbedded().get(AbstractJdbcConnectorType.SYSTEM_DTO_KEY);
		assertTrue(system instanceof SysSystemDto);
		SysSystemDto systemDto = systemService.get(system.getId());
		assertNotNull(systemDto);

		ConnectorType connectorTypeBySystem = connectorManager.findConnectorTypeBySystem(systemDto);
		ConnectorTypeDto reopenSystem = getConnectorTypeDto();
		reopenSystem.setReopened(true);
		reopenSystem.getEmbedded().put(PostgresqlConnectorType.SYSTEM_DTO_KEY, systemDto);
		reopenSystem.getMetadata().put(PostgresqlConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		reopenSystem = connectorManager.load(reopenSystem);
		assertNotNull(reopenSystem);
		reopenSystem.setWizardStepName(AbstractJdbcConnectorType.STEP_ONE_CREATE_SYSTEM);

		// Change addition attributes
		reopenSystem.getMetadata().put(MsSqlConnectorType.AUTHENTICATION_TYPE_KEY, MsSqlConnectorType.SQL_SERVER_AUTHENTICATION_TYPE);
		reopenSystem.getMetadata().put(MsSqlConnectorType.TRUST_SERVER_CRT_SWITCH, Boolean.FALSE.toString());
		reopenSystem.getMetadata().put(MsSqlConnectorType.NTLM_SWITCH, Boolean.FALSE.toString());
		String domainTwo = getHelper().createName();
		reopenSystem.getMetadata().put(MsSqlConnectorType.DOMAIN_KEY, domainTwo);
		String instanceNameTwo = getHelper().createName();
		reopenSystem.getMetadata().put(MsSqlConnectorType.INSTANCE_NAME_KEY, instanceNameTwo);

		// Execute the first step again.
		connectorManager.execute(reopenSystem);

		// Load connector properties from created system.
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(systemDto);
		assertEquals("net.tirasa.connid.bundles.db.table.DatabaseTableConnector",
				connectorInstance.getConnectorKey().getConnectorName());

		IdmFormDefinitionDto connectorFormDef = this.systemService.getConnectorFormDefinition(systemDto);
		String jdbcUrlTemplate = getValueFromConnectorInstance(AbstractJdbcConnectorType.JDBC_URL_TEMPLATE, systemDto, connectorFormDef);
		// Check Windows auth.
		Assert.assertFalse(jdbcUrlTemplate.contains(MsSqlConnectorType.WINDOWS_AUTHENTICATION_TYPE_TEMPLATE));
		// Check trust CRT.
		Assert.assertFalse(jdbcUrlTemplate.contains(MsSqlConnectorType.TRUST_SERVER_CRT_TEMPLATE));
		// Check NTLM.
		Assert.assertFalse(jdbcUrlTemplate.contains(MsSqlConnectorType.NTLM_TEMPLATE));
		// Check Domain.
		Assert.assertFalse(jdbcUrlTemplate.contains(MsSqlConnectorType.DOMAIN_TEMPLATE + domain));
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.DOMAIN_TEMPLATE + domainTwo));
		// Check instance name.
		Assert.assertFalse(jdbcUrlTemplate.contains(MsSqlConnectorType.INSTANCE_NAME_TEMPLATE + instanceName));
		Assert.assertTrue(jdbcUrlTemplate.contains(MsSqlConnectorType.INSTANCE_NAME_TEMPLATE + instanceNameTwo));
		
		// Delete created system.
		systemService.delete(systemDto);
	}

	@Override
	protected ConnectorTypeDto getConnectorTypeDto() {
		return connectorManager.convertTypeToDto(mockMsSqlConnectorType);
	}

	protected String getHost() {
		//"jdbc:sqlserver://localhost:1433;databaseName=bcv_idm_10";
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			return "localhost";
		}
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("//")[1].split(":")[0];
	}

	protected String getUsername() {
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			return "user";
		}
		return env.getProperty("spring.datasource.username");
	}

	protected String getPassword() {
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			return "password";
		}
		return env.getProperty("spring.datasource.password");
	}

	protected String getPort() {
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			return "1433";
		}
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("//")[1].split(":")[1].split(";")[0];
	}

	protected String getDatabase() {
		if (!getJdbcConnectorTypeDriverName().equals(getDriver())) {
			return "db";
		}
		String jdbcUrl = env.getProperty("spring.datasource.url");
		return jdbcUrl.split("databaseName=")[1];
	}

	protected String getJdbcConnectorType() {
		return MockMsSqlConnectorType.NAME;
	}

	protected String getJdbcConnectorTypeDriverName() {
		MsSqlConnectorType type = new MsSqlConnectorType();
		return type.getJdbcDriverName();
	}
}

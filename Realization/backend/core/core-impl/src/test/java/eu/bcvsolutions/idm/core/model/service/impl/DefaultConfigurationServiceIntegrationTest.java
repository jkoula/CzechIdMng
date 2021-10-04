package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Application configuration tests.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultConfigurationServiceIntegrationTest extends AbstractIntegrationTest {
	
	private static final String TEST_PROPERTY_KEY = "test.property";
	private static final String TEST_PROPERTY_DB_KEY = "test.db.property";
	public static final String TEST_GUARDED_PROPERTY_KEY = "idm.sec.core.password.test";
	private static final String TEST_GUARDED_PROPERTY_VALUE = "secret_password";
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfidentialStorage confidentialStorage;
	@Autowired private IdmConfigurationService idmConfigurationService;
	//
	private ConfigurationService configurationService;
	
	@Before
	public void init() {
		configurationService = context.getAutowireCapableBeanFactory().createBean(DefaultConfigurationService.class);
	}
	
	@Test
	public void testReadNotExists() {
		Assert.assertNull(configurationService.getValue(getHelper().createName()));
	}
	
	@Test
	public void testReadNotExistsWithDefault() {
		Assert.assertEquals("true", configurationService.getValue(getHelper().createName(), "true"));
	}
	
	@Test
	public void testReadBooleanNotExistsWithDefault() {
		Assert.assertTrue(configurationService.getBooleanValue(getHelper().createName(), true));
	}
	
	@Test
	public void testReadPropertyFromFile() {
		Assert.assertEquals("true", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadBooleanPropertyFromFile() {
		Assert.assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_DB_KEY, "true"));
		Assert.assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_DB_KEY));
	}
	
	@Test
	public void testReadOverridenPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_KEY, "false"));
		Assert.assertEquals("false", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadGuardedPropertyFromFile() {
		Assert.assertEquals(TEST_GUARDED_PROPERTY_VALUE, configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
	
	@Test
	public void testReadConfidentialPropertyFromDB() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_GUARDED_PROPERTY_KEY, "secured_change"));
		//
		Assert.assertEquals("secured_change", configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
	
	@Test
	public void testGlobalDateFormatChange() {
		final String format = "dd.MM";
		configurationService.setValue(ConfigurationService.PROPERTY_APP_DATE_FORMAT, format);
		Assert.assertEquals(format, configurationService.getDateFormat());
		configurationService.setValue(ConfigurationService.PROPERTY_APP_DATE_FORMAT, ConfigurationService.DEFAULT_APP_DATE_FORMAT);
		Assert.assertEquals(ConfigurationService.DEFAULT_APP_DATE_FORMAT, configurationService.getDateFormat());
	}
	
	@Test
	public void testDefaultDateTimeFormat() {
		Assert.assertEquals(ConfigurationService.DEFAULT_APP_DATETIME_FORMAT, configurationService.getDateTimeFormat());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testSaveWrongContractState() {
		configurationService.setValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_STATE, "wrong");
	}
	
	@Test
	public void testChangeCongidentialProperty() {
		String confidentialKey = String.format("%s.%s", GuardedString.GUARDED_PROPERTY_NAMES[0], getHelper().createName());
		String value = getHelper().createName();
		//
		// empty at start - null
		configurationService.setValue(confidentialKey, "");
		IdmConfigurationDto confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertTrue(confidentialConfiguration.isConfidential());
		Assert.assertNull(confidentialConfiguration.getValue());
		Assert.assertNull(confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
		//
		// filled correctly
		configurationService.setValue(confidentialKey, value);
		confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertTrue(confidentialConfiguration.isConfidential());
		String proxyValue = confidentialConfiguration.getValue();
		Assert.assertTrue(proxyValue.startsWith(IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE));
		Assert.assertEquals(value, confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
		//
		// null => no change => previous proxy value 
		configurationService.setValue(confidentialKey, null);
		confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertEquals(proxyValue, confidentialConfiguration.getValue());
		Assert.assertEquals(value, confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
		//
		// same value => same proxy value
		configurationService.setValue(confidentialKey, value);
		confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertEquals(proxyValue, confidentialConfiguration.getValue());
		Assert.assertEquals(value, confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
		//
		// different value => new proxy value
		configurationService.setValue(confidentialKey, "updateValue");
		confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertNotEquals(proxyValue, confidentialConfiguration.getValue());
		Assert.assertEquals("updateValue", confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
		//
		// empty value => null
		configurationService.setValue(confidentialKey, "");
		confidentialConfiguration = idmConfigurationService.getByCode(confidentialKey);
		Assert.assertNull(confidentialConfiguration.getValue());
		Assert.assertNull(confidentialStorage.get(confidentialConfiguration, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class));
	}
	
}

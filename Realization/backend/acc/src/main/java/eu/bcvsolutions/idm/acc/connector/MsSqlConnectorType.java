package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.http.auth.AUTH;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * MS SQL connector type extends standard table connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(MsSqlConnectorType.NAME)
public class MsSqlConnectorType extends AbstractJdbcConnectorType {

	public static final String NAME = "mssql-connector-type";
	public static final String AUTHENTICATION_TYPE_KEY = "authenticationType";
	public static final String WINDOWS_AUTHENTICATION_TYPE = "WINDOWS_AUTHENTICATION";
	public static final String SQL_SERVER_AUTHENTICATION_TYPE = "SQL_SERVER_AUTHENTICATION";
	public static final String TRUST_SERVER_CRT_SWITCH = "trustServerCertificate";
	public static final String NTLM_SWITCH = "ntlm";
	public static final String DOMAIN_KEY = "domain";
	public static final String INSTANCE_NAME_KEY = "instanceName";
	public static final String WINDOWS_AUTHENTICATION_TYPE_TEMPLATE = ";integratedSecurity=true";
	private static final String WINDOWS_AUTHENTICATION_TYPE_FALSE_TEMPLATE = ";integratedSecurity=false";
	public static final String TRUST_SERVER_CRT_TEMPLATE = ";trustServerCertificate=true";
	private static final String TRUST_SERVER_CRT_FALSE_TEMPLATE = ";trustServerCertificate=false";
	public static final String DOMAIN_TEMPLATE = ";domain=";
	public static final String INSTANCE_NAME_TEMPLATE = ";instanceName=";
	public static final String NTLM_TEMPLATE = ";authenticationScheme=NTLM";

	@Override
	public String getIconKey() {
		return "mssql-connector";
	}

	@Override
	public String getJdbcDriverName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public String getJdbcUrlTemplate() {
		return "jdbc:sqlserver://%h:%p;databaseName=%d";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("SQL server system", 1));
		metadata.put(HOST, "localhost");
		metadata.put(PORT, "1433");
		metadata.put(AUTHENTICATION_TYPE_KEY, SQL_SERVER_AUTHENTICATION_TYPE);
		return metadata;
	}

	@Override
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		ConnectorTypeDto connectorTypeDto = super.load(connectorType);
		if (!connectorTypeDto.isReopened()) {
			return connectorTypeDto;
		}

		// Load the system.
		SysSystemDto systemDto = (SysSystemDto) connectorType.getEmbedded().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemDto, "System must exists!");
		Map<String, String> metadata = connectorType.getMetadata();

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String jdbcUrlTemplate = getValueFromConnectorInstance(JDBC_URL_TEMPLATE, systemDto, connectorFormDef);

		// Load an authentication type.
		metadata.put(AUTHENTICATION_TYPE_KEY, jdbcUrlTemplate.contains(WINDOWS_AUTHENTICATION_TYPE_TEMPLATE) 
				? WINDOWS_AUTHENTICATION_TYPE 
				: SQL_SERVER_AUTHENTICATION_TYPE);
		// Load crt skip
		metadata.put(TRUST_SERVER_CRT_SWITCH, String.valueOf(jdbcUrlTemplate.contains(TRUST_SERVER_CRT_TEMPLATE)));
		// Load NTLM
		metadata.put(NTLM_SWITCH, String.valueOf(jdbcUrlTemplate.contains(NTLM_TEMPLATE)));
		// Load a domain.
		String fullDomain = extractFullParameter(jdbcUrlTemplate, DOMAIN_TEMPLATE);
		if (Strings.isNotBlank(fullDomain)) {
			fullDomain = fullDomain.replace(DOMAIN_TEMPLATE, "");
		}
		metadata.put(DOMAIN_KEY, fullDomain);
		// Load an instance name.
		String fullInstanceName = extractFullParameter(jdbcUrlTemplate, INSTANCE_NAME_TEMPLATE);
		if (Strings.isNotBlank(fullInstanceName)) {
			fullInstanceName = fullInstanceName.replace(INSTANCE_NAME_TEMPLATE, "");
		}
		metadata.put(INSTANCE_NAME_KEY, fullInstanceName);
		
		
		return connectorTypeDto;
	}

	@Override
	protected void executeStepOne(ConnectorTypeDto connectorType) {
		String port = connectorType.getMetadata().get(PORT);
		Assert.notNull(port, "Port cannot be null!");
		String host = connectorType.getMetadata().get(HOST);
		Assert.notNull(host, "Host cannot be null!");
		String database = connectorType.getMetadata().get(DATABASE);
		Assert.notNull(database, "Database cannot be null!");
		String table = connectorType.getMetadata().get(TABLE);
		Assert.notNull(table, "Table cannot be null!");
		String keyColumn = connectorType.getMetadata().get(KEY_COLUMN);
		Assert.notNull(keyColumn, "Key column cannot be null!");
		String authenticationType = connectorType.getMetadata().get(AUTHENTICATION_TYPE_KEY);
		Assert.notNull(authenticationType, "Authentication type cannot be null!");

		String domain = connectorType.getMetadata().get(DOMAIN_KEY);
		String instanceName = connectorType.getMetadata().get(INSTANCE_NAME_KEY);
		String ntlmString = connectorType.getMetadata().get(NTLM_SWITCH);
		boolean ntlm = Boolean.parseBoolean(ntlmString);
		String trustServerCrtString = connectorType.getMetadata().get(TRUST_SERVER_CRT_SWITCH);
		boolean trustServerCrt = Boolean.parseBoolean(trustServerCrtString);


		// User and password is mandatory only for SQL Server authentication type or for NTLM
		String user = connectorType.getMetadata().get(USER);
		boolean passwordIsMandatory = false;
		if (authenticationType.equals(SQL_SERVER_AUTHENTICATION_TYPE)
				|| (authenticationType.equals(WINDOWS_AUTHENTICATION_TYPE) && ntlm)) {
			Assert.notNull(user, "Username cannot be null!");
			passwordIsMandatory = true;
		}

		String password = connectorType.getMetadata().get(PASSWORD);
		if (Strings.isBlank(password) && !passwordIsMandatory) {
			password = "random_password"; // Password is null and is not mandatory, but connector wants it -> random value.
		}
		// Remove password from metadata.
		connectorType.getMetadata().remove(PASSWORD);

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		SysSystemDto systemDto;
		if (systemId != null) {
			// System already exists.
			systemDto = getSystemService().get(UUID.fromString(systemId), IdmBasePermission.READ);
		} else {
			// Create new system.
			systemDto = new SysSystemDto();
		}
		systemDto.setName(connectorType.getMetadata().get(SYSTEM_NAME));
		// Resolve remote system.
		systemDto.setRemoteServer(connectorType.getRemoteServer());
		// Find connector key and set it to the system.
		IcConnectorKey connectorKey = getConnectorManager().findConnectorKey(connectorType);
		Assert.notNull(connectorKey, "Connector key was not found!");
		systemDto.setConnectorKey(new SysConnectorKeyDto(connectorKey));
		systemDto = getSystemService().save(systemDto, IdmBasePermission.CREATE);

		// Put new system to the connector type (will be returned to FE).
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Set the port.
		this.setValueToConnectorInstance(PORT, port, systemDto, connectorFormDef);
		// Set the host.
		this.setValueToConnectorInstance(HOST, host, systemDto, connectorFormDef);
		// Set the database.
		this.setValueToConnectorInstance(DATABASE, database, systemDto, connectorFormDef);
		// Set the table.
		this.setValueToConnectorInstance(TABLE, table, systemDto, connectorFormDef);
		// Set the user.
		this.setValueToConnectorInstance(USER, user, systemDto, connectorFormDef);
		// Set the password.
		// Password is mandatory only if none exists in connector configuration and for SQL Server authentication type or for NTLM.
		String passwordInSystem = this.getValueFromConnectorInstance(PASSWORD, systemDto, connectorFormDef);
		if (Strings.isNotBlank(password) && !GuardedString.SECRED_PROXY_STRING.equals(password)) {
			this.setValueToConnectorInstance(PASSWORD, password, systemDto, connectorFormDef);
		} else if (passwordIsMandatory) {
			Assert.notNull(passwordInSystem, "Password cannot be null!");
		}
		// Set the JDBC driver.
		this.setValueToConnectorInstance(JDBC_DRIVER, getJdbcDriverName(), systemDto, connectorFormDef);
		// Compile JDBC url with additional parameters.
		String jdbcUrlTemplate = getJdbcUrlTemplate();
		if(connectorType.isReopened()) {
			// For reopen system will be used persisted url.
			String jdbcUrlTemplateOriginal = getValueFromConnectorInstance(JDBC_URL_TEMPLATE, systemDto, connectorFormDef);
			if (Strings.isNotBlank(jdbcUrlTemplateOriginal)){
				jdbcUrlTemplate = jdbcUrlTemplateOriginal;
			}
		}
		String jdbcUrl = compileJdbcUrl(jdbcUrlTemplate, authenticationType, ntlm, trustServerCrt, domain, instanceName);
		// Set the JDBC url template.
		this.setValueToConnectorInstance(JDBC_URL_TEMPLATE, jdbcUrl, systemDto, connectorFormDef);
		// Set the column with PK.
		this.setValueToConnectorInstance(KEY_COLUMN, keyColumn, systemDto, connectorFormDef);

		// Generate schema
		try {
			List<SysSchemaObjectClassDto> schemas = this.getSystemService().generateSchema(systemDto);
			SysSchemaObjectClassDto schemaAccount = schemas.stream()
					.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
					.orElse(null);
			Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");
			connectorType.getMetadata().put(SCHEMA_ID_KEY, schemaAccount.getId().toString());
		} catch (ResultCodeException ex) {
			// Throw nice exception if lib for Windows authentication missing.
			Throwable cause = ex.getCause();
			if (cause.getMessage().contains("This driver is not configured for integrated authentication.")) {
				Throwable linked = cause.getCause();
				if (linked instanceof SQLException) {
					Throwable missingDriverEx = linked.getCause();
					if (missingDriverEx != null) {
						String missingDriverExMessage = missingDriverEx.getMessage();
						if (missingDriverExMessage.length() > 120){
							missingDriverExMessage = missingDriverExMessage.substring(0, 119) + "...";
						}
						throw new ResultCodeException(AccResultCode.WIZARD_MSSQL_CONNECTOR_LIB_MISSING,
								ImmutableMap.of("text", missingDriverExMessage));
					}
				}
			}
			throw ex;
		}
	}

	private String compileJdbcUrl(String jdbcUrlTemplate, String authenticationType, boolean ntlm, boolean trustServerCrt, String domain, String instanceName) {
		if (WINDOWS_AUTHENTICATION_TYPE.equals(authenticationType) && !jdbcUrlTemplate.contains(WINDOWS_AUTHENTICATION_TYPE_TEMPLATE)) {
			// "integratedSecurity=true" missing
			if (jdbcUrlTemplate.contains(WINDOWS_AUTHENTICATION_TYPE_FALSE_TEMPLATE)) {
				// "integratedSecurity=false" is used -> replace it.
				jdbcUrlTemplate = jdbcUrlTemplate.replace(WINDOWS_AUTHENTICATION_TYPE_FALSE_TEMPLATE, WINDOWS_AUTHENTICATION_TYPE_TEMPLATE);
			} else {
				jdbcUrlTemplate = jdbcUrlTemplate + WINDOWS_AUTHENTICATION_TYPE_TEMPLATE;
			}
		}
		if (SQL_SERVER_AUTHENTICATION_TYPE.equals(authenticationType) && jdbcUrlTemplate.contains(WINDOWS_AUTHENTICATION_TYPE_TEMPLATE)) {
			// "integratedSecurity=true" is used -> remove it.
			jdbcUrlTemplate = jdbcUrlTemplate.replace(WINDOWS_AUTHENTICATION_TYPE_TEMPLATE, "");
		}

		// Add NTLM.
		if (ntlm && !jdbcUrlTemplate.contains(NTLM_TEMPLATE)) {
			// "authenticationScheme=NTLM" missing
			jdbcUrlTemplate = jdbcUrlTemplate + NTLM_TEMPLATE;
		}

		// Remove NTLM
		if (!ntlm && jdbcUrlTemplate.contains(NTLM_TEMPLATE)) {
			// "authenticationScheme=NTLM" is used -> remove it.
			jdbcUrlTemplate = jdbcUrlTemplate.replace(NTLM_TEMPLATE, "");
		}

		// Add trustServerCrt.
		if (trustServerCrt && !jdbcUrlTemplate.contains(TRUST_SERVER_CRT_TEMPLATE)) {
			if (jdbcUrlTemplate.contains(TRUST_SERVER_CRT_FALSE_TEMPLATE)) {
				// "trustServerCertificate=false" is used -> replace it.
				jdbcUrlTemplate = jdbcUrlTemplate.replace(TRUST_SERVER_CRT_FALSE_TEMPLATE, TRUST_SERVER_CRT_TEMPLATE);
			} else {
				jdbcUrlTemplate = jdbcUrlTemplate + TRUST_SERVER_CRT_TEMPLATE;
			}
		}

		// Remove trustServerCrt
		if (!trustServerCrt && jdbcUrlTemplate.contains(TRUST_SERVER_CRT_TEMPLATE)) {
			// "trustServerCertificate=true" is used -> remove it.
			jdbcUrlTemplate = jdbcUrlTemplate.replace(TRUST_SERVER_CRT_TEMPLATE, "");
		}

		// Add domain.
		if (Strings.isNotBlank(domain)) {
			String fullDomain = DOMAIN_TEMPLATE + domain;

			if (jdbcUrlTemplate.contains(DOMAIN_TEMPLATE) && !jdbcUrlTemplate.contains(fullDomain)) {
				String oldFullDomain = extractFullParameter(jdbcUrlTemplate, DOMAIN_TEMPLATE);
				// Different "domain=" is used -> replace it.
				jdbcUrlTemplate = jdbcUrlTemplate.replace(oldFullDomain, fullDomain);
			}

			if (!jdbcUrlTemplate.contains(DOMAIN_TEMPLATE)) {
				// Domain missing, will be added.
				jdbcUrlTemplate = jdbcUrlTemplate + fullDomain;
			}
		}

		// Remove domain
		if (Strings.isBlank(domain) && jdbcUrlTemplate.contains(DOMAIN_TEMPLATE)) {
			// "domain=" is used -> remove it.
			String fullDomain = extractFullParameter(jdbcUrlTemplate, DOMAIN_TEMPLATE);
			jdbcUrlTemplate = jdbcUrlTemplate.replace(fullDomain, "");
		}

		// Add instanceName.
		if (Strings.isNotBlank(instanceName)) {
			String fullInstanceName = INSTANCE_NAME_TEMPLATE + instanceName;

			if (jdbcUrlTemplate.contains(INSTANCE_NAME_TEMPLATE) && !jdbcUrlTemplate.contains(fullInstanceName)) {
				String oldFullInstanceName = extractFullParameter(jdbcUrlTemplate, INSTANCE_NAME_TEMPLATE);
				// Different "instanceName=" is used -> replace it.
				jdbcUrlTemplate = jdbcUrlTemplate.replace(oldFullInstanceName, fullInstanceName);
			}

			if (!jdbcUrlTemplate.contains(INSTANCE_NAME_TEMPLATE)) {
				// Instance name missing, will be added.
				jdbcUrlTemplate = jdbcUrlTemplate + fullInstanceName;
			}
		}

		// Remove instanceName
		if (Strings.isBlank(instanceName) && jdbcUrlTemplate.contains(INSTANCE_NAME_TEMPLATE)) {
			// "instanceName=" is used -> remove it.
			String fullInstanceName = extractFullParameter(jdbcUrlTemplate, INSTANCE_NAME_TEMPLATE);
			jdbcUrlTemplate = jdbcUrlTemplate.replace(fullInstanceName, "");
		}

		return jdbcUrlTemplate;
	}

	private String extractFullParameter(String jdbcUrlTemplate, String template) {
		int start = jdbcUrlTemplate.indexOf(template);
		if (start == -1) {
			return null;
		}
		String value = jdbcUrlTemplate.substring(start + template.length());
		int end = value.indexOf(';');
		if (end == -1) {
			end = value.length();
		}
		end = end + jdbcUrlTemplate.substring(0, start).length() + template.length();

		return jdbcUrlTemplate.substring(start, end);

	}


	@Override
	public int getOrder() {
		return 170;
	}

}

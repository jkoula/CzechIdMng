package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.EmbeddedDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.theme.PaletteColorDto;
import eu.bcvsolutions.idm.core.api.dto.theme.PaletteDto;
import eu.bcvsolutions.idm.core.api.dto.theme.ShapeDto;
import eu.bcvsolutions.idm.core.api.dto.theme.ThemeDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.impl.LogbackLoggerManagerIntegrationTest;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmConfigurationControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmConfigurationDto> {

	@Autowired private IdmConfigurationController controller;
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private TokenManager tokenManager;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private ApplicationConfiguration applicationConfiguration;
	
	@Override
	protected AbstractReadWriteDtoController<IdmConfigurationDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected IdmConfigurationDto prepareDto() {
		IdmConfigurationDto dto = new IdmConfigurationDto();
		dto.setName(ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + getHelper().createName());
		dto.setValue(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testGetAllConfigurationsFromFiles() throws Exception {
		// configuration from files and logback logger
		String response = getMockMvc().perform(get(getBaseUrl() + "/all/file")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		List<IdmConfigurationDto> dtos = getMapper().readValue(response, new TypeReference<List<IdmConfigurationDto>>() {});
		//
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().equals(ConfigurationService.PROPERTY_APP_INSTANCE_ID))); // all property files has this ...
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().endsWith(LogbackLoggerManagerIntegrationTest.TEST_PACKAGE_FROM_PROPERTIES))); // all logger configuration has this test package
	}
	
	@Test
	public void testGetAllConfigurationsFromEnvironment() throws Exception {
		// configuration from files and logback logger
		String response = getMockMvc().perform(get(getBaseUrl() + "/all/environment")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		List<IdmConfigurationDto> dtos = getMapper().readValue(response, new TypeReference<List<IdmConfigurationDto>>() {});
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().equals("java.specification.version"))); // all property files has this ...
	}
	
	@Test
	public void testGetPublicConfigurationsWithWrongToken() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// credentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		LoginDto authenticate = authenticationManager.authenticate(loginDto);
		Assert.assertNotNull(authenticate.getToken());
		// disable token => invalidate authentication
		tokenManager.disableToken(authenticate.getAuthentication().getId());
		// but public endpoints doesn't check it
		String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations")
                .contentType(TestHelper.HAL_CONTENT_TYPE)
                .param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, authenticate.getToken()))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		List<IdmConfigurationDto> dtos = getMapper().readValue(response, new TypeReference<List<IdmConfigurationDto>>() {});
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().equals("idm.pub.core.public-setting")));
	}
	
	@Test
	public void testGetRegisteredReadDtoServices() throws Exception {
		// configuration from files and logback logger
		String response = getMockMvc().perform(get(getBaseUrl() + "/search/read-dto-services")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		JsonNode json = getMapper().readTree(response);
		JsonNode jsonEmbedded = json.get(EmbeddedDto.PROPERTY_EMBEDDED); // by convention
		JsonNode jsonResources = jsonEmbedded.get(getResourcesName(AvailableServiceDto.class));
		//
		// convert embedded object to target DTO classes
		List<AvailableServiceDto> dtos = new ArrayList<>();
		jsonResources.forEach(jsonResource -> {
			dtos.add(getMapper().convertValue(jsonResource, AvailableServiceDto.class));
		});
		//
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getServiceName().equals(DefaultIdmIdentityService.class.getSimpleName())));
	}
	
	@Test
	public void testDownloadApplicationLogo() throws Exception {
		// not configured
		try {
			getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/logo")
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			
			// wrongly configured
			getHelper().setConfigurationValue(ApplicationConfiguration.PROPERTY_APPLICATION_LOGO, UUID.randomUUID().toString());
			getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/logo")
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNotFound());
			
			// upload
			String fileName = "file.png";
			String content = "some image";
			getMockMvc().perform(MockMvcRequestBuilders.multipart(getBaseUrl() + "/application/logo")
					.file(new MockMultipartFile("data", fileName, "image/png", IOUtils.toByteArray(IOUtils.toInputStream(content))))
	        		.param("fileName", fileName)
	        		.with(authentication(getAdminAuthentication())))
					.andExpect(status().isOk());
			
			// get configured
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/logo")
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			Assert.assertEquals(content, response);
			//
			IdmAttachmentDto image = attachmentManager.get(applicationConfiguration.getApplicationLogoId());
			Assert.assertEquals(content.length(), image.getFilesize().intValue());
			Assert.assertEquals(fileName, image.getName());
			InputStream is = attachmentManager.getAttachmentData(image.getId());
			try {
				Assert.assertEquals(content, IOUtils.toString(is));
			} finally {
				IOUtils.closeQuietly(is);
			}
			//
			// delete
			getMockMvc().perform(delete(getBaseUrl() + "/application/logo")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			//
			// not configured
			getMockMvc().perform(delete(getBaseUrl() + "/application/logo")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/logo")
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			
		} finally {
			// just for sure, it some test fails
			applicationConfiguration.deleteApplicationLogo();
		}
	}
	
	@Test
	public void testGetApplicationTheme() throws Exception {
		try {
			// not configured
			getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/theme")
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			// not configured - dark
			getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/theme")
					.param("type", "dark")
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNoContent());
			//
			// configure 
			ThemeDto configureTheme = new ThemeDto();
			configureTheme.setPalette(new PaletteDto());
			configureTheme.getPalette().setPrimary(new PaletteColorDto("#000"));
			configureTheme.setShape(new ShapeDto());
			configureTheme.getShape().setBorderRadius(5);
			getHelper().setConfigurationValue(ApplicationConfiguration.PROPERTY_APPLICATION_THEME, getMapper().writeValueAsString(configureTheme));
			//
			// get configured theme
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations/application/theme")
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			ThemeDto theme = getMapper().readValue(response, ThemeDto.class);
			Assert.assertNotNull(theme);
			Assert.assertNotNull(theme.getPalette());
			Assert.assertEquals(configureTheme.getPalette().getPrimary().getMain(), theme.getPalette().getPrimary().getMain());
			Assert.assertNotNull(theme.getShape());
			Assert.assertEquals(configureTheme.getShape().getBorderRadius(), theme.getShape().getBorderRadius());
		} finally {
			getHelper().deleteConfigurationValue(ApplicationConfiguration.PROPERTY_APPLICATION_THEME);
		}
	}
}

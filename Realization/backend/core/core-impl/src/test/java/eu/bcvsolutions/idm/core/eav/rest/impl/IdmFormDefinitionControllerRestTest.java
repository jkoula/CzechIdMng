package eu.bcvsolutions.idm.core.eav.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormDefinitionControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmFormDefinitionDto> {

	@Autowired private IdmFormDefinitionController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmFormDefinitionDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmFormDefinitionDto prepareDto() {
		IdmFormDefinitionDto dto = new IdmFormDefinitionDto();
		dto.setType(getHelper().createName());
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		//
		return dto;
	}
	
	@Test
	public void testDeleteMainDefinition() throws Exception {
		IdmFormDefinitionDto dto = prepareDto();
		dto.setMain(true);
		dto = createDto(dto);
		IdmFormDefinitionDto getDto = getDto(dto.getId());
		Assert.assertNotNull(getDto);
		Assert.assertEquals(dto.getId(), getDto.getId());
		Assert.assertTrue(getDto.isMain());
		//
		MvcResult result = getMockMvc().perform(delete(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andReturn();
		Exception exception = result.getResolvedException();
		Assert.assertTrue(exception instanceof ResultCodeException);
		Assert.assertEquals(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_MAIN_FORM.getCode(), ((ResultCodeException) exception).getError().getError().getStatusEnum());
	}
	
	@Test
	public void testUpdateMainDefinition() throws Exception {
		IdmFormDefinitionDto dto = prepareDto();
		dto.setMain(true);
		dto = createDto(dto);
		dto.setMain(false);
		//
		MvcResult result = getMockMvc().perform(put(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andReturn();
		Exception exception = result.getResolvedException();
		Assert.assertTrue(exception instanceof ResultCodeException);
		Assert.assertEquals(CoreResultCode.FORM_DEFINITION_UPDATE_FAILED_MAIN_FORM.getCode(), ((ResultCodeException) exception).getError().getError().getStatusEnum());
	}
}

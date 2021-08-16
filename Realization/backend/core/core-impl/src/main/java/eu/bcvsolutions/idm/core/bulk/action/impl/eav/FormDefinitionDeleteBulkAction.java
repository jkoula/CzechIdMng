package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent.FormDefinitionEventType;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Delete form definition.
 * 
 * @author Ondrej Husnik
 * @author Radek Tomiška
 */
@Component(FormDefinitionDeleteBulkAction.NAME)
@Description("Delete form definition.")
public class FormDefinitionDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormDefinitionDto, IdmFormDefinitionFilter> {

	public static final String NAME = "form-definition-delete-bulk-action";
	//
	@Autowired private IdmFormDefinitionService service;
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_DEFINITION_DELETE);
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		// add force delete, if currently logged user is ROLE_ADMIN
		if (securityService.hasAnyAuthority(CoreGroupPermission.FORM_DEFINITION_ADMIN)) {
			formAttributes.add(new IdmFormAttributeDto(EntityEventProcessor.PROPERTY_FORCE_DELETE, "Force delete", PersistentType.BOOLEAN));
		}
		return formAttributes;
	}
	
	@Override
	protected OperationResult processDto(IdmFormDefinitionDto formDefinition) {
		try {
			FormDefinitionEvent event = new FormDefinitionEvent(
					FormDefinitionEventType.DELETE, formDefinition, new ConfigurationMap(getProperties()).toMap()
			);
			event.setPriority(PriorityType.HIGH);
			service.publish(event);
			//
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch (ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				return new OperationResult.Builder(OperationState.EXCEPTION) //
						.setException((ResultCodeException) resolvedException) //
						.build(); //
			}
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}

	@Override
	public ReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinitionFilter> getService() {
		return service;
	}
}

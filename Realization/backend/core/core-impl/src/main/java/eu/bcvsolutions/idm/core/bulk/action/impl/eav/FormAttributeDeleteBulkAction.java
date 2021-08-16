package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent.FormAttributeEventType;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Delete form attribute.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(FormAttributeDeleteBulkAction.NAME)
@Description("Delete form attribute.")
public class FormAttributeDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormAttributeDto, IdmFormAttributeFilter> {

	public static final String NAME = "core-form-attribute-delete-bulk-action";
	//
	@Autowired private IdmFormAttributeService service;
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_ATTRIBUTE_DELETE);
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		Set<String> distinctAttributes = new HashSet<>();
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		// add force delete, if currently logged user is ROLE_ADMIN
		if (securityService.hasAnyAuthority(CoreGroupPermission.FORM_ATTRIBUTE_ADMIN)) {
			formAttributes.add(new IdmFormAttributeDto(EntityEventProcessor.PROPERTY_FORCE_DELETE, "Force delete", PersistentType.BOOLEAN));
			distinctAttributes.add(EntityEventProcessor.PROPERTY_FORCE_DELETE);
		}
		return formAttributes;
	}
	
	@Override
	protected OperationResult processDto(IdmFormAttributeDto formAttribute) {
		try {
			FormAttributeEvent event = new FormAttributeEvent(
					FormAttributeEventType.DELETE, formAttribute, new ConfigurationMap(getProperties()).toMap()
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
	public ReadWriteDtoService<IdmFormAttributeDto, IdmFormAttributeFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}

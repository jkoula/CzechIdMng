package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Delete given role catalogue item.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
@Component(RoleCatalogueDeleteBulkAction.NAME)
@Description("Delete given role catalogue item.")
public class RoleCatalogueDeleteBulkAction extends AbstractRemoveBulkAction<IdmRoleCatalogueDto, IdmRoleCatalogueFilter> {

	public static final String NAME = "core-role-catalogue-delete-bulk-action";
	//
	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	@Autowired private SecurityService securityService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLECATALOGUE_DELETE);
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		// add force delete, if currently logged user is ROLECATALOGUE_ADMIN
		if (securityService.hasAnyAuthority(CoreGroupPermission.ROLECATALOGUE_ADMIN)) {
			formAttributes.add(new IdmFormAttributeDto(EntityEventProcessor.PROPERTY_FORCE_DELETE, "Force delete", PersistentType.BOOLEAN));
		}
		//
		return formAttributes;
	}

	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();

		Map<ResultModel, Long> models = new HashMap<>();
		entities.forEach(roleCatalogueId -> {
			IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
			filter.setParent(roleCatalogueId);
			filter.setRecursively(true);
			IdmRoleCatalogueDto roleCatalogue = getService().get(roleCatalogueId);
			long count = roleCatalogueService.count(filter);
			if (count > 0) {
				if (securityService.hasAnyAuthority(CoreGroupPermission.ROLECATALOGUE_ADMIN)) {
					models.put(
							new DefaultResultModel(
									CoreResultCode.ROLE_CATALOGUE_FORCE_DELETE_HAS_CHILDREN,
									ImmutableMap.of("roleCatalogue", roleCatalogue.getCode(), "count", count)
							),
							count
					);					
				} else {
					models.put(
							new DefaultResultModel(
									CoreResultCode.ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN,
									ImmutableMap.of("roleCatalogue", roleCatalogue.getCode(), "count", count)
							),
							count
					);
				}
			}
		});
		//
		// sort by count
		List<Entry<ResultModel, Long>> collect = models //
				.entrySet() //
				.stream() //
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
				.limit(5) //
				.collect(Collectors.toList()); //
		collect.forEach(entry -> {
			result.addInfo(entry.getKey());
		});
		//
		return result;
	}
	
	@Override
	protected OperationResult processDto(IdmRoleCatalogueDto roleCatalogue) {
		boolean forceDelete = getParameterConverter().toBoolean(getProperties(), EntityEventProcessor.PROPERTY_FORCE_DELETE, false);
		if (!forceDelete) {
			return super.processDto(roleCatalogue);
		}
		// force delete
		try {
			// force delete can execute role catalogue admin only
			getService().checkAccess(roleCatalogue, IdmBasePermission.ADMIN);
			//
			RoleCatalogueEvent roleCatalogueEvent = new RoleCatalogueEvent(
					RoleCatalogueEventType.DELETE,
					roleCatalogue,
					new ConfigurationMap(getProperties()).toMap()
			);
			roleCatalogueService.publish(roleCatalogueEvent);
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
	public ReadWriteDtoService<IdmRoleCatalogueDto, IdmRoleCatalogueFilter> getService() {
		return roleCatalogueService;
	}
}

package eu.bcvsolutions.idm.core.bulk.action.impl.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent.TreeTypeEventType;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Delete given tree types.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
@Component(TreeTypeDeleteBulkAction.NAME)
@Description("Delete given tree types.")
public class TreeTypeDeleteBulkAction extends AbstractRemoveBulkAction<IdmTreeTypeDto, IdmTreeTypeFilter> {

	public static final String NAME = "core-tree-type-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeTypeDeleteBulkAction.class);
	//
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private EntityStateManager entityStateManager;
	@Autowired private SecurityService securityService;
	@Autowired private TreeNodeDeleteBulkAction treeNodeDeleteBulkAction;
	//
	private final List<UUID> processedIds = new ArrayList<UUID>();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.TREETYPE_DELETE);
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		// add force delete, if currently logged user is TREETYPE_ADMIN
		if (securityService.hasAnyAuthority(CoreGroupPermission.TREETYPE_ADMIN)) {
			formAttributes.add(new IdmFormAttributeDto(EntityEventProcessor.PROPERTY_FORCE_DELETE, "Force delete", PersistentType.BOOLEAN));
		}
		//
		return formAttributes;
	}
	
	@Override
	protected OperationResult processDto(IdmTreeTypeDto treeType) {
		boolean forceDelete = getParameterConverter().toBoolean(getProperties(), EntityEventProcessor.PROPERTY_FORCE_DELETE, false);
		if (!forceDelete) {
			return super.processDto(treeType);
		}
		// force delete - without request by event
		try {
			// force delete can execute tree type admin only
			getService().checkAccess(treeType, IdmBasePermission.ADMIN);
			//
			TreeTypeEvent treeTypeEvent = new TreeTypeEvent(TreeTypeEventType.DELETE, treeType, new ConfigurationMap(getProperties()).toMap());
			treeTypeEvent.setPriority(PriorityType.HIGH);
			EventContext<IdmTreeTypeDto> result = treeTypeService.publish(treeTypeEvent);
			processedIds.add(result.getContent().getId());
			//
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch (ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				return new OperationResult.Builder(OperationState.EXCEPTION)
						.setException((ResultCodeException) resolvedException)
						.build();
			}
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception exception) {
		if (exception != null 
				|| (result != null && OperationState.EXECUTED != result.getState())) {
			return super.end(result, exception);
		}
		// success
		boolean forceDelete = isForceDelete();
		if (!forceDelete) {
			return super.end(result, exception);
		}
		//
		for (UUID treeTypeId : processedIds) {
			IdmTreeTypeDto treeType = getService().get(treeTypeId);
			if (treeType != null) {
				// delete tree nodes => tree node related records are removed asynchronously, but tree node itself will be removed here
				IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
				filter.setTreeTypeId(treeTypeId);
				filter.setRoots(Boolean.TRUE);
				for (IdmTreeNodeDto treeNode : treeNodeService.find(filter, null).getContent()) {
					OperationResult errorResult = treeNodeDeleteBulkAction.deleteTreeNode(treeNode, result);
					if (errorResult != null) {
						return errorResult;
					}
				}
				//
				treeTypeService.delete(treeType);
				//
				LOG.debug("Tree type [{}] deleted.", treeType.getCode());
			} else {
				LOG.debug("Tree type [{}] already deleted.", treeTypeId);
			}
			// clean up all states
			entityStateManager.deleteStates(new IdmTreeTypeDto(treeTypeId), null, null);
		}
		return super.end(result, exception);
	}

	@Override
	public ReadWriteDtoService<IdmTreeTypeDto, IdmTreeTypeFilter> getService() {
		return treeTypeService;
	}
}

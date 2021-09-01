package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.AbstractAssignRoleBulkAction;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;

/**
 * Bulk operation for add role to identity.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Component(IdentityAddRoleBulkAction.NAME)
@Description("Add role to idetity in bulk action.")
public class IdentityAddRoleBulkAction extends AbstractAssignRoleBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityAddRoleBulkAction.class);
	public static final String NAME = "identity-add-role-bulk-action";
	//
	public static final String ROLE_CODE = PROPERTY_ROLE;
	public static final String PRIMARY_CONTRACT_CODE = "mainContract";
	public static final String VALID_TILL_CODE = PROPERTY_VALID_TILL;
	public static final String VALID_FROM_CODE = PROPERTY_VALID_FROM;
	public static final String APPROVE_CODE = PROPERTY_APPROVE;
	//
	@Autowired private IdmIdentityService identityService;

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		// identity select is not needed
		formAttributes.removeIf(attribute -> attribute.getCode().equals(PROPERTY_IDENTITY));
		// role select is needed
		formAttributes.add(0, getRoleAttribute());
		//
		return formAttributes;
	}

	@Override
	public String getName() {
		return IdentityAddRoleBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		assignRoles(Lists.newArrayList(identity.getId()), getRoles());
		//
		return null;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(
				CoreGroupPermission.IDENTITY_READ
		);
	}
	
	/**
	 * Is set only primary contract
	 *
	 * @return
	 */
	@Override
	protected boolean isPrimeContract() {
		return getParameterConverter().toBoolean(getProperties(), PRIMARY_CONTRACT_CODE, true);
	}

	/**
	 * Get {@link IdmFormAttributeDto} for checkbox primary contract.
	 *
	 * @return
	 */
	@Override
	protected IdmFormAttributeDto getPrimeContractAttribute() {
		IdmFormAttributeDto primaryContract = new IdmFormAttributeDto(
				PRIMARY_CONTRACT_CODE, 
				PRIMARY_CONTRACT_CODE, 
				PersistentType.BOOLEAN);
		primaryContract.setDefaultValue(Boolean.TRUE.toString());
		return primaryContract;
	}
	
	@Override
	public <DTO extends AbstractDto> IdmProcessedTaskItemDto logItemProcessed(DTO item, OperationResult opResult) {
		if (item instanceof IdmIdentityDto && opResult == null) {
			// we don't want to log roles, which are iterated only
			LOG.debug("Role [{}] was processed by bulk action.", item.getId());
			//
			return null;
		}
		return super.logItemProcessed(item, opResult);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}

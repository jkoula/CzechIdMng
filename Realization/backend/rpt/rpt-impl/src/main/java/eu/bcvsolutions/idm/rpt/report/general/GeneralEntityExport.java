package eu.bcvsolutions.idm.rpt.report.general;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.rpt.entity.RptReport;

/**
 * Implementation of general entity report. This action will be available for all
 * AbstractDtos (supports bulk actions on BE).
 *
 * @author Vít Švanda
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 * @author Tomáš Doischer
 */
@Component
@Enabled(RptModuleDescriptor.MODULE_ID)
public class GeneralEntityExport extends AbstractFormableEntityExport<AbstractDto, BaseFilter> {
	
	@Autowired
	private LookupService lookupService;
	@Autowired
	private FormService formService;
	@SuppressWarnings("rawtypes")
	@Autowired
	private List<AbstractFormValueService> formValueServices;
	
	private ReadWriteDtoService<AbstractDto, BaseFilter> localService;

	public GeneralEntityExport(RptReportService reportService, AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {
		super(reportService, attachmentManager, mapper, formService);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected List<String> getAuthoritiesForEntity() {
		ReadWriteDtoService<AbstractDto, BaseFilter> service = getService();

		if (!(service instanceof AuthorizableService)) {
			// Service is not authorizable => only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
		
		AuthorizableService authorizableService = (AuthorizableService) service;
		AuthorizableType authorizableType = authorizableService.getAuthorizableType();
		if (authorizableType == null) {
			// Service is authorizable but group is not specified => only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
			
		boolean readPermissionFound = authorizableType.getGroup().getPermissions()
				.stream()
				.filter(permission -> IdmBasePermission.READ == permission)
				.findFirst()
				.isPresent();
		if (!readPermissionFound) {
			// By default only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
		
		// If exist, read permission for that type will be returned.
		return Lists.newArrayList(
				MessageFormat.format("{0}{1}{2}",
						authorizableType.getGroup().getName(),
						IdmBasePermission.SEPARATOR,
						IdmBasePermission.READ.name())
		);
	}
	
	/**
	 * Get service dynamically by action.
	 * @return 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ReadWriteDtoService<AbstractDto, BaseFilter> getService() {
		if (localService != null) {
			if (this.getEntityClass().equals(IdmFormValue.class)) {
				localService = getFormValueService();
				if (localService != null) {
					return localService;
				}
			} else {
				return localService;
			}
		}

		Class<? extends BaseEntity> localEntityClass = this.getEntityClass();
		if (localEntityClass == null) {
			return null;
		}

		localService = (ReadWriteDtoService<AbstractDto, BaseFilter>) lookupService
				.getDtoService((Class<? extends BaseEntity>) localEntityClass);
		return localService;
	}
	
	@SuppressWarnings("rawtypes")
	private AbstractFormValueService getFormValueService() {
		// we can get the form value type from formDefinition
		if (this.getAction() != null && this.getAction().getFilter() != null &&
				this.getAction().getFilter().get("definitionId") != null) {
			UUID formDefId = UUID.fromString((String) this.getAction().getFilter().get("definitionId"));
			IdmFormDefinitionDto formDef = formService.getDefinition(formDefId);
			for (AbstractFormValueService formValueService : formValueServices) {
				if (formValueService.getOwnerClass().getCanonicalName().equals(formDef.getType())) {
					return formValueService;
				}
			}
		}
		// we can get the form value type from the form values
		if (this.getAction() != null && this.getAction().getIdentifiers() != null &&
				!this.getAction().getIdentifiers().isEmpty()) {
			UUID identifier = this.getAction().getIdentifiers().iterator().next();
			IdmFormValueDto formValue;
			for (AbstractFormValueService formValueService : formValueServices) {
				formValue = formValueService.get(identifier);
				if (formValue != null) {
					return formValueService;
				}
			}
			
		}
		
		return null;
	}

	@Override
	public boolean supports(Class<? extends BaseEntity> clazz) {
		return AbstractEntity.class.isAssignableFrom(clazz) 
				&& !RptReport.class.isAssignableFrom(clazz); // cyclic report of report is not supported now
	}

	@Override
	public boolean isGeneric() {
		return true;
	}
}

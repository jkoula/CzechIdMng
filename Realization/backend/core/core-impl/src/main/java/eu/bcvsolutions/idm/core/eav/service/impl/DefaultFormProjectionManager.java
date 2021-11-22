package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.FormProjectionLookup;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionRoute;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public class DefaultFormProjectionManager implements FormProjectionManager {

	@Autowired private ApplicationContext context;
	@Autowired @Lazy private EnabledEvaluator enabledEvaluator;
	@Autowired private LookupService lookupService;
	//
	private final PluginRegistry<FormProjectionLookup<?>, Class<?>> formProjectionLookups;
	
	@Autowired
	public DefaultFormProjectionManager(List<? extends FormProjectionLookup<?>> formProjectionLookups) {
		Assert.notNull(formProjectionLookups, "Projection lookupsare required");
		//
		this.formProjectionLookups = OrderAwarePluginRegistry.create(formProjectionLookups);
	}
	
	@Override
	public List<FormProjectionRouteDto> getSupportedRoutes() {
		return context
			.getBeansOfType(FormProjectionRoute.class)
			.values()
			.stream()
			.filter(enabledEvaluator::isEnabled)
			.sorted(Comparator.comparing(FormProjectionRoute::getOrder))
			.map(route -> {
				FormProjectionRouteDto routeDto = new FormProjectionRouteDto();
				routeDto.setId(route.getId());
				routeDto.setName(route.getName());
				routeDto.setOwnerType(route.getOwnerType().getCanonicalName());
				routeDto.setModule(route.getModule());
				routeDto.setDescription(route.getDescription());
				routeDto.setFormDefinition(route.getFormDefinition());
				//
				return routeDto;
			})
			.collect(Collectors.toList());
	}
	
	@Override
	public IdmFormDefinitionDto getBasicFieldsDefinition(Identifiable dto) {
		return getConfiguredFormDefinition(dto, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IdmFormDefinitionDto getConfiguredFormDefinition(Identifiable owner, IdmFormDefinitionDto formDefinition) {
		FormProjectionLookup<BaseDto> lookup = (FormProjectionLookup<BaseDto>) formProjectionLookups.getPluginFor(owner.getClass());
		if (lookup == null) {
			return formDefinition;
		}
		//
		return lookup.lookupFormDefinition(getOwner(owner), formDefinition);
		
	}
	
	@Override
	public IdmFormDefinitionDto overrideFormDefinition(Identifiable owner, IdmFormDefinitionDto originalDefinition) {
		return overrideFormDefinition(originalDefinition, getConfiguredFormDefinition(owner, originalDefinition));
	}
	
	@Override
	public IdmFormDefinitionDto overrideFormDefinition(IdmFormDefinitionDto originalDefinition, IdmFormDefinitionDto configuredDefinition) {
		if (configuredDefinition == null) {
			return originalDefinition;
		}
		// overridden definition found and given definition to override is null => nothing to override, return configured
		if (originalDefinition == null) {
			return configuredDefinition;
		}
		for (IdmFormAttributeDto overridenAttribute : configuredDefinition.getFormAttributes()) {
			IdmFormAttributeDto mappedAttribute = originalDefinition.getMappedAttribute(overridenAttribute.getId());
			if (mappedAttribute == null) { // configured, but attribute was removed in the meantime
				continue;
			}
			mappedAttribute.setReadonly(overridenAttribute.isReadonly());
			mappedAttribute.setRequired(overridenAttribute.isRequired());
			if (StringUtils.isNotEmpty(overridenAttribute.getLabel())) {
				mappedAttribute.setName(overridenAttribute.getLabel());
			}
			if (StringUtils.isNotEmpty(overridenAttribute.getPlaceholder())) {
				mappedAttribute.setPlaceholder(overridenAttribute.getPlaceholder());
			}
			if (overridenAttribute.getMin() != null) {
				mappedAttribute.setMin(overridenAttribute.getMin());
			}
			if (overridenAttribute.getMax() != null) {
				mappedAttribute.setMax(overridenAttribute.getMax());
			}
			if (StringUtils.isNotEmpty(overridenAttribute.getRegex())) {
				mappedAttribute.setRegex(overridenAttribute.getRegex());
			}
			if (StringUtils.isNotEmpty(overridenAttribute.getValidationMessage())) {
				mappedAttribute.setValidationMessage(overridenAttribute.getValidationMessage());
			}			
		}
		//
		return originalDefinition;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IdmFormInstanceDto getBasicFieldsInstance(Identifiable owner) {
		FormProjectionLookup<BaseDto> lookup = (FormProjectionLookup<BaseDto>) formProjectionLookups.getPluginFor(owner.getClass());
		if (lookup == null) {
			return null;
		}
		return lookup.lookupBasicFieldsInstance(getOwner(owner));
	}

	private BaseDto getOwner(Identifiable owner) {
		Assert.notNull(owner, "Projection owner is required.");
		//
		if (owner instanceof BaseDto) {
			return (BaseDto) owner;
		}
		//
		return lookupService.lookupDto(owner.getClass(), owner.getId());
	}
}

package eu.bcvsolutions.idm.rpt.report.general;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Base implementation of {@link eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity} reporting to json. It exports everything,
 * that {@link AbstractEntityExport} does and adds all eav attributes on top. If there is only one {@link IdmFormDefinitionDto}
 * for given type, attributes are added to result with their code and value serialized to {@link String}. If there are more
 * {@link IdmFormDefinitionDto} for given type, each attribute's code will be prefixed with corresponding definition code.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 *
 * @param <D>
 * @param <F>
 */
public abstract class AbstractFormableEntityExport<D extends AbstractDto, F extends BaseFilter> extends AbstractEntityExport<D, F> {

	private final FormService formService;

	public AbstractFormableEntityExport(
			RptReportService reportService,
			AttachmentManager attachmentManager,
			ObjectMapper mapper,
			FormService formService) {
		super(reportService, attachmentManager, mapper);
		//
		this.formService = formService;
	}

	@Override
	protected Map<String, String> tramsformToMap(D dto) {
		Map<String, String> map = super.tramsformToMap(dto);
		//
		if (dto instanceof FormableDto) {
			List<IdmFormDefinitionDto> definitions = formService.getDefinitions(dto, IdmBasePermission.AUTOCOMPLETE);
			List<IdmFormInstanceDto> formInstances = definitions
					.stream()
					.map(d -> formService.getFormInstance(dto, d, IdmBasePermission.READ))
					.collect(Collectors.toList());
			//
			FormableDto formableDto = (FormableDto) dto;
			formInstances.forEach(formInstance -> processFormInstance(map, formInstance, formableDto.getEavs().size() > 1));
		}
		return map;
	}

	private void processFormInstance(Map<String, String> resultMap, IdmFormInstanceDto formInstance, boolean prefixEavsWithDefinitionCode) {
		Map<String, List<Serializable>> eavsWithValues = new HashMap<>();
		// fill existing values
		formInstance.getValues().forEach(val -> {
			final String eavName = getEavName(formInstance
					.getMappedAttribute(val.getFormAttribute()), formInstance, prefixEavsWithDefinitionCode);
			if (eavName == null) {
				return;
			}
			if (!eavsWithValues.containsKey(eavName)) {
				eavsWithValues.put(eavName, new ArrayList<>());
			} 
			eavsWithValues.get(eavName).add(val.getValue());
		});
		//fill other attributes with empty values
		formInstance.getFormDefinition().getFormAttributes().stream()
				.map(attr -> getEavName(attr, formInstance, prefixEavsWithDefinitionCode))
				.filter(attr -> !eavsWithValues.containsKey(attr))
				.forEach(attr -> eavsWithValues.put(attr, new ArrayList<>()));
		// transform values to result
		eavsWithValues.keySet().forEach(attr -> {
			List<Serializable> values = eavsWithValues.get(attr);
			if (values.isEmpty()) {
				if (!resultMap.containsKey(attr)) {
					resultMap.put(attr, "");
				} else {
					resultMap.put(sanitizeEavName(attr, resultMap), "");
				}
			} else {
				if (!resultMap.containsKey(attr)) {
					resultMap.put(attr, String.valueOf(values.size() > 1 ? values : values.get(0)));
				} else {
					resultMap.put(sanitizeEavName(attr, resultMap), String.valueOf(values.size() > 1 ? values : values.get(0)));
				}
			}
		});
	}

	private String sanitizeEavName(String attr, Map<String, String> resultMap) {
		StringBuilder sanitizedNameBuilder = new StringBuilder();
		sanitizedNameBuilder.append(attr);
		sanitizedNameBuilder.append("_eav");
		int i = 1;
		sanitizedNameBuilder.append(i);
		while (resultMap.containsKey(sanitizedNameBuilder.toString())) {
			i++;
			sanitizedNameBuilder.replace(sanitizedNameBuilder.length() - 1, sanitizedNameBuilder.length(), String.valueOf(i));
		}
		
		return sanitizedNameBuilder.toString();
	}
	
	protected String getEavName(IdmFormAttributeDto mappedAttribute,IdmFormInstanceDto formInstance, boolean prefixEavsWithDefinitionCode) {
		if (mappedAttribute == null || (prefixEavsWithDefinitionCode && formInstance == null)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (prefixEavsWithDefinitionCode) {
			sb.append(formInstance.getFormDefinition().getCode());
			sb.append('_');
		}
		return sb.append(mappedAttribute.getCode()).toString();
	}

}

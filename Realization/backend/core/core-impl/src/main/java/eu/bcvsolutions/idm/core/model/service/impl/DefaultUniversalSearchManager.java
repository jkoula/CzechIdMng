package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.UniversalSearchManager;
import eu.bcvsolutions.idm.core.eav.api.service.UniversalSearchType;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Default universal search manager
 *
 * @author Vít Švanda
 * @since 12.0.0
 *
 */
@Service("universalSearchManager")
public class DefaultUniversalSearchManager implements UniversalSearchManager {

	@Autowired
	private ApplicationContext context;
	@Lazy
	@Autowired
	private EnabledEvaluator enabledEvaluator;

	@Override
	public List<UniversalSearchType<? extends AbstractDto, ? extends BaseFilter>> getSupportedTypes() {
		return context
				.getBeansOfType(UniversalSearchType.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.sorted(Comparator.comparing(UniversalSearchType::getOrder))
				.collect(Collectors.toList());
	}

	@Override
	public UniversalSearchTypeDto convertUniversalSearchTypeToDto(UniversalSearchType<? extends AbstractDto, ? extends BaseFilter> universalSearchType) {
		UniversalSearchTypeDto universalSearchTypeDto = new UniversalSearchTypeDto();
		universalSearchTypeDto.setId(universalSearchType.getId());
		universalSearchTypeDto.setType(universalSearchType.getId());
		if (universalSearchType.getOwnerType() != null) {
			universalSearchTypeDto.setOwnerType(universalSearchType.getOwnerType().getCanonicalName());
		}
		universalSearchTypeDto.setModule(universalSearchType.getModule());
		return universalSearchTypeDto;
	}
}

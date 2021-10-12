package eu.bcvsolutions.idm.core.model.service.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.UniversalSearchManager;
import eu.bcvsolutions.idm.core.eav.api.service.UniversalSearchType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Default universal search manager
 *
 * @author Vít Švanda
 * @since 11.3.0
 *
 */
@Service("universalSearchManager")
public class DefaultUniversalSearchManager implements UniversalSearchManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultUniversalSearchManager.class);
	
	@Autowired
	private ApplicationContext context;
	@Lazy
	@Autowired
	private EnabledEvaluator enabledEvaluator;
	

	@Override
	public List<UniversalSearchType<? extends AbstractDto, ? extends BaseFilter>> getSupportedTypes() {
		List<UniversalSearchType<?,?>> results = Lists.newArrayList();
		List<UniversalSearchType> types = context
				.getBeansOfType(UniversalSearchType.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.sorted(Comparator.comparing(UniversalSearchType::getOrder))
				.collect(Collectors.toList());
		for (UniversalSearchType type : types) {
			results.add(type);
		}
		
		return results;
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
//		universalSearchTypeDto.setDisabled(universalSearchType.i);
		//
		return universalSearchTypeDto;
	}

	public UniversalSearchType<? extends  AbstractDto, ? extends BaseFilter> getUniversalSearchType(String id) {
		return this.getSupportedTypes().stream()
				.filter(type -> type.getId().equals(id))
				.findFirst()
				.orElse(null);
	}
	
}

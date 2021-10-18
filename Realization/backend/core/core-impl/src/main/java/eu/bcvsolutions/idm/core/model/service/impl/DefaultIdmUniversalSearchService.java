package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmUniversalSearchFilter;
import eu.bcvsolutions.idm.core.api.service.IdmUniversalSearchService;
import eu.bcvsolutions.idm.core.api.service.UniversalSearchManager;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Default implementation of service for universal search.
 *
 * @since 11.3.0
 * @author Vít Švanda
 */
@Service("universalSearchService")
public class DefaultIdmUniversalSearchService extends
		AbstractBaseDtoService<UniversalSearchDto, IdmUniversalSearchFilter>
		implements IdmUniversalSearchService {

	@Autowired
	private UniversalSearchManager universalSearchManager;

	@Override
	public Page<UniversalSearchDto> find(IdmUniversalSearchFilter filter, Pageable pageable,
										 BasePermission... permission) {
		Assert.notNull(filter, "Filter is required.");

		if (pageable == null) {
			// Page is null, so we set page to max value
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}

		long total;
		List<UniversalSearchDto> results = new ArrayList<>();
		Pageable finalPageable = pageable;
		universalSearchManager.getSupportedTypes().forEach(type -> {
			Page<? extends AbstractDto> dtosPage = type.find(filter.getText(), PageRequest.of(0, 5, finalPageable.getSort()), IdmBasePermission.READ);
			UniversalSearchTypeDto universalSearchTypeDto = universalSearchManager.convertUniversalSearchTypeToDto(type);
			universalSearchTypeDto.setCount(dtosPage.getTotalElements());

			// Transform DTO to universal search DTO.
			dtosPage.getContent().forEach(dto -> {
				UniversalSearchDto universalSearchDto = type.dtoToUniversalSearchDto(dto);
				universalSearchDto.setType(universalSearchTypeDto);

				results.add(universalSearchDto);
			});

		});

		total = results.size();
		PageRequest pageableRequest = PageRequest.of(pageable.getPageNumber(),
				Math.max(results.size(), pageable.getPageSize()), pageable.getSort());
		return new PageImpl<>(results, pageableRequest, total);
	}
}

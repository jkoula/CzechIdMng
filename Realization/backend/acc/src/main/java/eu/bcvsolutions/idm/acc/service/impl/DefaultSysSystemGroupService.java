package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemGroup;
import eu.bcvsolutions.idm.acc.entity.SysSystemGroup_;
import eu.bcvsolutions.idm.acc.repository.SysSystemGroupRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue_;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * System groups service (cross-domain)
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Service("sysSystemGroupService")
public class DefaultSysSystemGroupService
		extends AbstractEventableDtoService<SysSystemGroupDto, SysSystemGroup, SysSystemGroupFilter>
		implements SysSystemGroupService {
	
	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;

	@Autowired
	public DefaultSysSystemGroupService(SysSystemGroupRepository repository,
										EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEMGROUP, getEntityClass());
	}

	@Override
	protected List<Predicate> toPredicates(Root<SysSystemGroup> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysSystemGroupFilter filter) {

		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		// Group text
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(SysSystemGroup_.code)),
							"%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(SysSystemGroup_.description)),
							"%" + filter.getText().toLowerCase() + "%")));
		}
		
		// Group type
		if (filter.getGroupType() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroup_.type), filter.getGroupType()));
		}

		return predicates;
	}

	@Override
	public SysSystemGroupDto getByCode(String code) {
		SysSystemGroupFilter filter = new SysSystemGroupFilter();
		filter.setCodeableIdentifier(code);
		return find(filter, PageRequest.of(0, 1))
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
	}
}

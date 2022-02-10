package eu.bcvsolutions.idm.core.audit.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent_;
import eu.bcvsolutions.idm.core.audit.entity.key.IdmLoggingEventExceptionPrimaryKey_;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException;
import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEventException_;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventExceptionRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link IdmLoggingEventService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmLoggingEventExceptionService extends
		AbstractReadWriteDtoService<IdmLoggingEventExceptionDto, IdmLoggingEventException, IdmLoggingEventExceptionFilter>
		implements IdmLoggingEventExceptionService {

	private final IdmLoggingEventExceptionRepository repository;
	private final IdmLoggingEventRepository loggingEventRepository;
	
	@Autowired
	public DefaultIdmLoggingEventExceptionService(IdmLoggingEventExceptionRepository repository, IdmLoggingEventRepository loggingEventRepository) {
		super(repository);
		//
		this.repository = repository;
		this.loggingEventRepository = loggingEventRepository;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmLoggingEventException> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmLoggingEventExceptionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		if (filter.getEvent() != null) {
			predicates.add(
					builder.equal(root.get(IdmLoggingEventException_.id).get(IdmLoggingEventExceptionPrimaryKey_.event).get(IdmLoggingEvent_.id), filter.getEvent()));
		}

		if (filter.getId() != null) {
			predicates.add(
					builder.equal(root.get(IdmLoggingEventException_.id).get(IdmLoggingEventExceptionPrimaryKey_.id), filter.getId()));
		}

		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUDIT, null);
	}
	
	@Override
	protected IdmLoggingEventExceptionDto toDto(IdmLoggingEventException entity, IdmLoggingEventExceptionDto dto) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			dto = new IdmLoggingEventExceptionDto();
		}
		dto.setTraceLine(entity.getTraceLine());
		dto.setId(entity.getId());
		dto.setEvent((Long) entity.getEvent().getId());
		return dto;
	}


	@Override
	protected IdmLoggingEventException toEntity(IdmLoggingEventExceptionDto dto, IdmLoggingEventException entity) {
		final IdmLoggingEventException idmLoggingEventException = super.toEntity(dto, entity);
		if (idmLoggingEventException.getEvent() == null && dto.getEvent() != null) {
			// Try to find event
			final IdmLoggingEvent event = loggingEventRepository.findOneById(dto.getEvent());
			idmLoggingEventException.setEvent(event);
		}

		idmLoggingEventException.setId(dto.getId());

		return  idmLoggingEventException;
	}

	/**
	 * Needst to be overriden here, because original implementation from parent cannot handle composite identifier.
	 *
	 * @param dto {@link IdmLoggingEventExceptionDto} to save
	 * @return saved {@link IdmLoggingEventExceptionDto}
	 */
	@Override
	@Transactional
	public IdmLoggingEventExceptionDto saveInternal(IdmLoggingEventExceptionDto dto) {
		Assert.notNull(dto, "DTO is required for save.");
		dto = validateDto(dto);
		//
		IdmLoggingEventException persistedEntity = null;
		if (dto.getId() != null) {
			IdmLoggingEventExceptionFilter filter = new IdmLoggingEventExceptionFilter();
			filter.setId((Long) dto.getId());
			filter.setEvent(dto.getEvent());
			persistedEntity = findEntities(filter, PageRequest.of(0,1)).stream().findFirst().orElse(null);
		}
		// convert to entity
		IdmLoggingEventException entity = toEntity(dto, persistedEntity);
		// validate
		entity = validateEntity(entity);
		// then persist
		entity = getRepository().saveAndFlush(entity);
		// finally convert to dto
		return toDto(entity);
	}

	@Override
	protected IdmLoggingEventException getEntity(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Identifier is required to load a log event.");
		IdmLoggingEventExceptionFilter filter = new IdmLoggingEventExceptionFilter();
		filter.setId(Long.valueOf(id.toString()));
		List<IdmLoggingEventExceptionDto> entities = this.find(filter, null, permission).getContent();
		if (entities.isEmpty()) {
			return null;
		}
		// for given id must found only one entity
		IdmLoggingEventException entity = this.toEntity(entities.get(0));
		return checkAccess(entity, permission);
	}

	@Override
	@Transactional
	public void deleteByEventId(Long eventId) {
		this.repository.deleteByEventId(eventId);
	}
}

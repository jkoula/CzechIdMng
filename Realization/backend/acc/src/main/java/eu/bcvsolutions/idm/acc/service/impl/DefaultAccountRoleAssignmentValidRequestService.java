package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccRoleAssignmentValidRequestDto;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignmentValidRequest;
import eu.bcvsolutions.idm.acc.repository.AccAccountRoleAssignmentValidRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentValidRequestService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation {@link IdmIdentityRoleValidRequestService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultAccountRoleAssignmentValidRequestService
		extends AbstractReadWriteDtoService<AccRoleAssignmentValidRequestDto, AccAccountRoleAssignmentValidRequest, EmptyFilter>
		implements AccAccountRoleAssignmentValidRequestService {

	private final AccAccountRoleAssignmentValidRequestRepository repository;
	private final EntityEventManager entityEventManager;

	@Autowired
	public DefaultAccountRoleAssignmentValidRequestService(
			AccAccountRoleAssignmentValidRequestRepository repository, EntityEventManager entityEventManager) {
		super(repository);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}

	@Override
	public AccRoleAssignmentValidRequestDto createByAccountRoleId(UUID identityRoleId) {
		AccRoleAssignmentValidRequestDto dto = toDto(repository.findOneByAccountRoleAssignment_Id(identityRoleId));
		//
		if (dto == null) {
			dto = new AccRoleAssignmentValidRequestDto();
			dto.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			dto.setAccountRoleAssignment(identityRoleId);
		}
		//
		// just update modified date
		return this.save(dto);
	}

	@Override
	public List<AccRoleAssignmentValidRequestDto> findAllValid() {
		return this.findAllValidFrom(ZonedDateTime.now());
	}

	@Override
	public void publishOrIncrease(AccRoleAssignmentValidRequestDto validRequestDto) {
		try {
			// after success provisioning is request removed from db
			entityEventManager.process(new IdentityRoleValidRequestEvent<>(IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID, validRequestDto));
		} catch (RuntimeException e) {
			// log failed operation
			validRequestDto.increaseAttempt();
			validRequestDto.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setCause(e).build());

			this.save(validRequestDto);
		}
	}

	@Override
	public List<AccRoleAssignmentValidRequestDto> findAllValidFrom(ZonedDateTime from) {
		return toDtos(this.repository.findAllValidFrom(from.toLocalDate()), true);
	}

	@Override
	public List<AccRoleAssignmentValidRequestDto> findAllValidRequestForRoleId(UUID roleId) {
		return toDtos(repository.findAllByAccountRoleAssignment_Role_Id(roleId), true);
	}

	@Override
	public List<AccRoleAssignmentValidRequestDto> findAllValidRequestForAccountId(UUID accountId) {
		return toDtos(repository.findAllByAccountRoleAssignment_Id(accountId), true);
	}

	@Override
	public void deleteAll(List<AccRoleAssignmentValidRequestDto> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (AccRoleAssignmentValidRequestDto entity : entities) {
				this.deleteInternalById(entity.getId());
			}
		}
	}

	@Override
	public List<AccRoleAssignmentValidRequestDto> findAllValidRequestForRoleAssignmentId(UUID identityRoleId) {
		return toDtos(repository.findAllByAccountRoleAssignment_Id(identityRoleId), true);
	}

}

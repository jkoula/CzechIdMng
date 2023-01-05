package eu.bcvsolutions.idm.acc.security.evaluator;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerRoleFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole_;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner_;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Returns systems, where logged user is in system owners (by identity or by role).
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Component(SystemOwnerEvaluator.EVALUATOR_NAME)
@Description("Returns systems, where logged user is in system owners (by identity or by role).")
public class SystemOwnerEvaluator extends AbstractAuthorizationEvaluator<SysSystem> {

	public static final String EVALUATOR_NAME = "acc-system-owner-evaluator";

	@Autowired
	private SecurityService securityService;
	@Autowired
	private SysSystemOwnerService systemOwnerService;
	@Autowired
	private SysSystemOwnerRoleService systemOwnerRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}

	@Override
	public Predicate getPredicate(Root<SysSystem> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		AbstractAuthentication authentication = securityService.getAuthentication();
		if (authentication == null || authentication.getCurrentIdentity() == null) {
			return null;
		}

		if (hasPermission(policy, permission)) {
			// by identity
			Subquery<SysSystemOwner> subquery = query.subquery(SysSystemOwner.class);
			Root<SysSystemOwner> subRoot = subquery.from(SysSystemOwner.class);
			subquery.select(subRoot);

			subquery.where(
					builder.and(
							builder.equal(subRoot.get(SysSystemOwner_.system), root), // correlation attr
							builder.equal(subRoot.get(SysSystemOwner_.owner).get(AbstractEntity_.id), authentication.getCurrentIdentity().getId())
					)
			);

			// by role - currently logged identity has a role
			Subquery<SysSystemOwnerRole> subqueryOwnerRole = query.subquery(SysSystemOwnerRole.class);
			Root<SysSystemOwnerRole> subRootOwnerRole = subqueryOwnerRole.from(SysSystemOwnerRole.class);
			subqueryOwnerRole.select(subRootOwnerRole);

			// assigned roles
			Subquery<IdmRole> subqueryIdentityRole = query.subquery(IdmRole.class);
			Root<IdmIdentityRole> subrootIdentityRole = subqueryIdentityRole.from(IdmIdentityRole.class);
			subqueryIdentityRole.select(subrootIdentityRole.get(IdmIdentityRole_.role));
			final LocalDate today = LocalDate.now();
			subqueryIdentityRole.where(
					builder.and(
							builder.equal(
									subrootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(AbstractEntity_.id),
									authentication.getCurrentIdentity().getId()),
							RepositoryUtils.getValidPredicate(subrootIdentityRole, builder, today),
							RepositoryUtils.getValidPredicate(subrootIdentityRole.get(IdmIdentityRole_.identityContract), builder, today),
							builder.equal(subrootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.FALSE)
					));

			subqueryOwnerRole.where(
					builder.and(
							builder.equal(subRootOwnerRole.get(SysSystemOwnerRole_.system), root), // correlation attr
							subRootOwnerRole.get(SysSystemOwnerRole_.ownerRole).in(subqueryIdentityRole)
					)
			);

			return builder.or(
					builder.exists(subquery),
					builder.exists(subqueryOwnerRole)
			);
		}
		return null;
	}

	@Override
	public Set<String> getPermissions(SysSystem entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || entity.getId() == null || !securityService.isAuthenticated()) {
			return permissions;
		}

		SysSystemOwnerFilter filter = new SysSystemOwnerFilter();
		filter.setSystem(entity.getId());
		filter.setOwner(securityService.getCurrentId());

		// by identity
		if (systemOwnerService.find(filter, PageRequest.of(0, 1)).getTotalElements() > 0) {
			permissions.addAll(policy.getPermissions());
			return permissions;
		}

		// by role
		SysSystemOwnerRoleFilter filterRole = new SysSystemOwnerRoleFilter();
		filterRole.setSystem(entity.getId());
		Set<UUID> ownerRoles = systemOwnerRoleService
				.find(filterRole, null)
				.getContent()
				.stream()
				.map(SysSystemOwnerRoleDto::getOwnerRole)
				.collect(Collectors.toSet());

		// identity roles
		if (identityRoleService
				.findValidRoles(securityService.getCurrentId(), null)
				.getContent()
				.stream()
				.anyMatch(ir -> ownerRoles.contains(ir.getRole()))) {
			permissions.addAll(policy.getPermissions());
		}

		return permissions;
	}
}

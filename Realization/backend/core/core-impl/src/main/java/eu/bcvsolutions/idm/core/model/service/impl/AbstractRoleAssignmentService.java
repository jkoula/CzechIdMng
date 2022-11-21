package eu.bcvsolutions.idm.core.model.service.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleAssignmentRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentService<D extends AbstractRoleAssignmentDto, E extends AbstractRoleAssignment, F extends BaseRoleAssignmentFilter>
        extends AbstractFormableService<D, E, F> implements IdmRoleAssignmentService<D, F>, ApplicationContextAware {

    private final IdmRoleService roleService;
    private final IdmAutomaticRoleRepository automaticRoleRepository;
    private final LookupService lookupService;
    @Autowired
    private IdmRoleSystemService roleSystemService;

    private final IdmRoleAssignmentRepository<E> roleAssignmentRepository;

    private final FilterManager filterManager;
    protected ApplicationContext applicationContext;

    protected AbstractRoleAssignmentService(IdmRoleAssignmentRepository<E> repository, EntityEventManager entityEventManager, FormService formService, IdmRoleService roleService, IdmAutomaticRoleRepository automaticRoleRepository, LookupService lookupService, FilterManager filterManager) {
        super(repository, entityEventManager, formService);
        this.roleService = roleService;
        this.automaticRoleRepository = automaticRoleRepository;
        this.lookupService = lookupService;
        this.roleAssignmentRepository = repository;
        this.filterManager = filterManager;
    }

    @Override
    protected List<Predicate> toPredicates(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, F filter) {
        List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        if (filter.getOwnerType() != null && !filter.getOwnerType().isAssignableFrom(getOwnerType())) {
            // If supported owner type by this service does not match owner type specified in filter, we want to return
            // empty result set.
            predicates.add(builder.disjunction());
            return predicates;
        }
        // by role text
        String roleText = filter.getRoleText();
        if (StringUtils.isNotEmpty(roleText)) {
            IdmRoleFilter subFilter = new IdmRoleFilter();
            subFilter.setText(roleText);
            Subquery<IdmRole> subquery = query.subquery(IdmRole.class);
            Root<IdmRole> subRoot = subquery.from(IdmRole.class);
            subquery.select(subRoot);
            //
            Predicate rolePredicate = filterManager
                    .getBuilder(IdmRole.class, DataFilter.PARAMETER_TEXT)
                    .getPredicate(subRoot, subquery, builder, subFilter);
            //
            subquery.where(
                    builder.and(
                            builder.equal(root.get(AbstractRoleAssignment_.role), subRoot), // correlation attr
                            rolePredicate
                    )
            );
            //
            predicates.add(builder.exists(subquery));
        }
        List<UUID> roles = filter.getRoles();
        if (!roles.isEmpty()) {
            predicates.add(root.get(AbstractRoleAssignment_.role).get(AbstractEntity_.id).in(roles));
        }
        List<String> roleEnvironments = filter.getRoleEnvironments();
        if (CollectionUtils.isNotEmpty(roleEnvironments)) {
            predicates.add(root.get(AbstractRoleAssignment_.role).get(IdmRole_.environment).in(roleEnvironments));
        }
        UUID roleCatalogueId = filter.getRoleCatalogueId();
        if (roleCatalogueId != null) {
            Subquery<IdmRoleCatalogueRole> roleCatalogueRoleSubquery = query.subquery(IdmRoleCatalogueRole.class);
            Root<IdmRoleCatalogueRole> subRootRoleCatalogueRole = roleCatalogueRoleSubquery.from(IdmRoleCatalogueRole.class);
            roleCatalogueRoleSubquery.select(subRootRoleCatalogueRole);

            roleCatalogueRoleSubquery.where(
                    builder.and(
                            builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.role), root.get(AbstractRoleAssignment_.role)),
                            builder.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.roleCatalogue).get(AbstractEntity_.id), roleCatalogueId)
                    ));
            predicates.add(builder.exists(roleCatalogueRoleSubquery));
        }
        //
        // is automatic role
        Boolean automaticRole = filter.getAutomaticRole();
        if (automaticRole != null) {
            if (automaticRole) {
                predicates.add(builder.isNotNull(root.get(AbstractRoleAssignment_.automaticRole)));
            } else {
                predicates.add(builder.isNull(root.get(AbstractRoleAssignment_.automaticRole)));
            }
        }
        //
        UUID automaticRoleId = filter.getAutomaticRoleId();
        if (automaticRoleId != null) {
            predicates.add(builder.equal(
                    root.get(AbstractRoleAssignment_.automaticRole).get(AbstractEntity_.id),
                    automaticRoleId)
            );
        }
        //
        UUID roleCompositionId = filter.getRoleCompositionId();
        if (roleCompositionId != null) {
            predicates.add(builder.equal(root.get(AbstractRoleAssignment_.roleComposition).get(AbstractEntity_.id), roleCompositionId));
        }
        //
        // is direct role
        Boolean directRole = filter.getDirectRole();
        if (directRole != null) {
            if (directRole) {
                predicates.add(builder.isNull(root.get("directRole")));
            } else {
                predicates.add(builder.isNotNull(root.get("directRole")));
            }
        }
        // Role-system
        UUID roleSystemId = filter.getRoleSystemId();
        if (roleSystemId != null) {
            predicates.add(builder.equal(root.get(AbstractRoleAssignment_.roleSystem), roleSystemId));
        }
        return predicates;
    }


    @Override
    protected D toDto(E entity, D dto) {
        dto = super.toDto(entity, dto);
        if (dto == null) {
            return null;
        }

        IdmAutomaticRole automaticRole = entity.getAutomaticRole();
        if (automaticRole != null) {
            dto.setAutomaticRole(automaticRole.getId());
            BaseDto baseDto = null;
            Map<String, BaseDto> embedded = dto.getEmbedded();
            if (automaticRole instanceof IdmAutomaticRoleAttribute) {
                baseDto = lookupService.getDtoService(IdmAutomaticRoleAttributeDto.class).get(automaticRole.getId());
            } else {
                baseDto = lookupService.getDtoService(IdmRoleTreeNodeDto.class).get(automaticRole.getId());
            }
            embedded.put(AbstractRoleAssignment_.automaticRole.getName(), baseDto);
            dto.setEmbedded(embedded);
        }

        UUID roleSystemId = entity.getRoleSystem();
        if (roleSystemId != null) {
            Map<String, BaseDto> embedded = dto.getEmbedded();
            if (roleSystemService instanceof AbstractReadDtoService) {
                @SuppressWarnings("rawtypes")
                BaseDto baseDto = ((AbstractReadDtoService) roleSystemService).get(roleSystemId);
                embedded.put(AbstractRoleAssignment_.roleSystem.getName(), baseDto);
                dto.setEmbedded(embedded);
            }
        }

        return dto;
    }

    @Override
    protected List<IdmFormInstanceDto> getFormInstances(D result, BasePermission... permission) {
        IdmFormInstanceDto formInstanceDto = getRoleAttributeValues(result);
        if (formInstanceDto != null) {
            // Validate the form instance
            formInstanceDto.setValidationErrors(getFormService().validate(formInstanceDto));
            return Lists.newArrayList(formInstanceDto);
        }
        return null;
    }

    @Override
    protected E toEntity(D dto, E entity) {
        E resultEntity = super.toEntity(dto, entity);
        // set additional automatic role
        if (resultEntity != null && dto.getAutomaticRole() != null) {
            // it isn't possible use lookupService entity lookup
            IdmAutomaticRole automaticRole = automaticRoleRepository.findById(dto.getAutomaticRole()).orElse(null);
            resultEntity.setAutomaticRole(automaticRole);
        }
        return resultEntity;
    }

    public IdmFormInstanceDto getRoleAttributeValues(D dto) {
        Assert.notNull(dto, "DTO is required.");
        // If given identity-role contains one formInstance, then will be returned
        List<IdmFormInstanceDto> eavs = dto.getEavs();
        if (eavs != null && eavs.size() == 1) {
            return eavs.get(0);
        }

        UUID roleId = dto.getRole();
        if (roleId != null) {
            IdmRoleDto role = DtoUtils.getEmbedded(dto, AbstractRoleAssignment_.role, IdmRoleDto.class);
            // Has role filled attribute definition?
            UUID formDefinition = role.getIdentityRoleAttributeDefinition();
            if (formDefinition != null) {
                IdmFormDefinitionDto formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
                // thin dto can be given -> owner is normal dto
                return this.getFormService().getFormInstance(dto, formDefinitionDto);
            }
        }
        return null;
    }

    public List<InvalidFormAttributeDto> validateFormAttributes(D identityRole) {
        IdmFormInstanceDto formInstanceDto = this.getRoleAttributeValues(identityRole);
        if (formInstanceDto != null) {
            return this.getFormService().validate(formInstanceDto);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findByAutomaticRole(UUID automaticRoleId, Pageable pageable) {
        return toDtoPage(roleAssignmentRepository.findByAutomaticRole_Id(automaticRoleId, pageable));
    }

    @Transactional(readOnly = true)
    public Page<D> findExpiredRoles(LocalDate expirationDate, Pageable page) {
        Assert.notNull(expirationDate, "Expiration date is required.");
        //
        return toDtoPage(roleAssignmentRepository.findExpiredRoles(expirationDate, page));
    }

    public Page<D> findDirectExpiredRoles(LocalDate expirationDate, Pageable page) {
        Assert.notNull(expirationDate, "Expiration date is required.");
        //
        return toDtoPage(roleAssignmentRepository.findDirectExpiredRoles(expirationDate, page));
    }

    public List<UUID> findDirectExpiredRoleIds(LocalDate expirationDate) {
        Assert.notNull(expirationDate, "Expiration date is required.");
        //
        return roleAssignmentRepository.findDirectExpiredRoleIds(expirationDate);
    }

    public D getDuplicated(D one, D two, Boolean skipSubdefinition) {
        Assert.notNull(one, "The first assinged role to compare is required.");
        Assert.notNull(two, "The second assinged role to compare is required.");
        //
        if (!one.getRole().equals(two.getRole())) {
            // Role isn't same
            return null;
        }

        // Role-system isn't same.
        if (one.getRoleSystem() == null) {
            if (two.getRoleSystem() != null) {
                return null;
            }
        } else if (!one.getRoleSystem().equals(two.getRoleSystem())) {
            return null;
        }


        D manually = null;
        D automatic = null;

        if (isRoleAutomaticOrComposition(one)) {
            automatic = one;
            manually = two;
        }

        if (isRoleAutomaticOrComposition(two)) {
            if (automatic != null) {
                // Automatic role is set from role ONE -> Both identity roles are automatic
                if (one.getDirectRole() == null
                        || two.getDirectRole() == null
                        || one.getRoleComposition() == null
                        || two.getRoleComposition() == null) {
                    // role was not created by business role definition
                    return null;
                }
                if (Objects.equals(one.getDirectRole(), two.getDirectRole())
                        && Objects.equals(one.getRoleComposition(), two.getRoleComposition())) {
                    // #2034 compositon is duplicate
                    return getIdentityRoleForRemove(one, two);
                }
                // automatic roles or composition is not duplicate
                return null;
            }
            automatic = two;
            manually = one;
        }

        /// Check duplicity for validity
        D validityDuplicity = null;
        if (automatic == null) {
            // Check if ONE role is duplicate with TWO and change order
            boolean duplicitOne = isIdentityRoleDatesDuplicit(one, two);
            boolean duplicitTwo = isIdentityRoleDatesDuplicit(two, one);

            if (duplicitOne && duplicitTwo) {
                // Both roles are same call method for decide which role will be removed
                validityDuplicity = getIdentityRoleForRemove(one, two);
            } else if (duplicitOne) {
                // Only role ONE is duplicit with TWO
                validityDuplicity = one;
            } else if (duplicitTwo) {
                // Only role TWO is duplicit with ONE
                validityDuplicity = two;
            }
        } else {
            // In case that we have only manually and automatic compare only from one order
            if (isIdentityRoleDatesDuplicit(manually, automatic)) {
                validityDuplicity = manually;
            }

        }

        // Check subdefinition can be skipped
        // and must be checked after validity
        if (BooleanUtils.isNotTrue(skipSubdefinition)) {
            // Validity must be same and subdefinition also. Then is possible remove role.
            // Subdefinition must be exactly same and isn't different between manually and automatic identity role
            if (validityDuplicity != null && equalsSubdefinitions(one, two)) {
                return validityDuplicity;
            }
        } else {
            // Check for subdefintion is skipped return only duplicity
            return validityDuplicity;
        }

        // No duplicity founded
        return null;
    }

    /**
     * Check if given {@link IdmIdentityRoleDto} is automatic or business role.
     *
     * @param identityRole
     * @return
     */
    public boolean isRoleAutomaticOrComposition(D identityRole) {
        return identityRole.getAutomaticRole() != null || identityRole.getDirectRole() != null;
    }

    /**
     * Method decides identity role that will be removed if both roles are same.
     * In default behavior is for removing choosen the newer. Method is protected for easy
     * overriding.
     *
     * @param one
     * @param two
     * @return
     */
    protected D getIdentityRoleForRemove(D one, D two) {
        // Both roles are same, remove newer
        if (one.getCreated().isAfter(two.getCreated())) {
            return one;
        }
        return two;
    }

    /**
     * Check if role ONE is duplicit by date with role TWO. For example if is role ONE fully in interval of validite the
     * role TWO.
     *
     * @param one
     * @param two
     * @return
     */
    private boolean isIdentityRoleDatesDuplicit(D one, D two) {
        LocalDate validTillForFirst = getDateForValidTill(one);
        // Validity role is in interval in a second role
        if (isDatesInRange(one.getValidFrom(), validTillForFirst, two.getValidFrom(), two.getValidTill())) {
            return true;
        }

        // Both role are valid
        if (one.isValid() && two.isValid()) {
            LocalDate validTillForTwo = two.getValidTill();
            if ((validTillForFirst == null && validTillForTwo == null) ||
                    (validTillForFirst != null && validTillForTwo != null && validTillForFirst.isEqual(validTillForTwo))) {
                // Valid tills from both identity roles are same
                return true;
            } else if (validTillForFirst != null && validTillForTwo == null) {
                // Second identity role has filled valid till but first not.
                // This mean that role TWO has bigger validity till than ONE
                return false;
            } else if (validTillForFirst != null && validTillForFirst.isBefore(validTillForTwo)) {
                // Valid till from manually role is before automatic, manually role could be removed
                return true;
            }
        }
        return false;
    }

    @Override
    public void unassignAllSubRoles(UUID identityRoleId, EntityEvent<D> parentEvent) {
        F filter = getFilter();
        filter.setDirectRoleId(identityRoleId);
        find(filter, null)
        .forEach(subIdentityRole -> {
            final AbstractRoleAssignmentEvent<D> subEvent = getEventForAssignment(subIdentityRole, AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE);
            //IdentityRoleEvent subEvent = new IdentityRoleEvent(AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE, subIdentityRole);
            //
            publish(subEvent, parentEvent);
            // Notes identity-accounts to ACM
            notingIdentityAccountForDelayedAcm(parentEvent, subEvent);
        });
    }

    /**
     * Method for noting identity-accounts for delayed account management
     *
     * @param event
     * @param subEvent
     */
    @SuppressWarnings("unchecked")
    private void notingIdentityAccountForDelayedAcm(EntityEvent<D> event,
            EntityEvent<D> subEvent) {
        Assert.notNull(event, "Event is required.");
        Assert.notNull(subEvent, "Sub event is required.");

        if (!event.getProperties().containsKey(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM)) {
            event.getProperties().put(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM, new HashSet<UUID>());
        }

        Set<UUID> identityAccounts = (Set<UUID>) subEvent.getProperties()
                .get(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM);
        if (identityAccounts != null) {
            ((Set<UUID>) event.getProperties().get(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM))
                    .addAll(identityAccounts);
        }

        if (!event.getProperties().containsKey(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING)) {
            event.getProperties().put(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING, new HashSet<UUID>());
        }

        Set<UUID> accounts = (Set<UUID>) subEvent.getProperties()
                .get(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING);
        if (accounts != null) {
            ((Set<UUID>) event.getProperties().get(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING))
                    .addAll(accounts);
        }
    }

    protected Map<String, Serializable> setupFlags(String... flags) {
        return Arrays.stream(Optional.ofNullable(flags).orElse(new String[0]))
                .collect(Collectors.toMap(s -> s, s -> Boolean.TRUE));
    }

    protected abstract LocalDate getDateForValidTill(D one);


    /**
     * Check if given dates is in range/interval the second ones.
     *
     * @param validFrom
     * @param validTill
     * @param rangeFrom
     * @param rangeTill
     * @return
     */
    private boolean isDatesInRange(LocalDate validFrom, LocalDate validTill, LocalDate rangeFrom, LocalDate rangeTill) {
        boolean leftIntervalSideOk = false;
        boolean rightIntervalSideOk = false;

        if (rangeFrom == null || (validFrom != null && (rangeFrom.isBefore(validFrom) || rangeFrom.isEqual(validFrom)))) {
            leftIntervalSideOk = true;
        }

        if (rangeTill == null || (validTill != null && (rangeTill.isAfter(validTill) || rangeTill.isEqual(validTill)))) {
            rightIntervalSideOk = true;
        }

        return leftIntervalSideOk && rightIntervalSideOk;
    }

    /**
     * Compare subdefinition. Return true if subdefinition are same. If {@link IdmIdentityRoleDto} doesn't contain subdefinition
     * return true.
     *
     * @param one
     * @param two
     * @return
     */
    private boolean equalsSubdefinitions(D one, D two) {

        List<IdmFormInstanceDto> eavsOne = one.getEavs();
        List<IdmFormInstanceDto> eavsTwo = two.getEavs();

        // Size of form instance doesn't match
        if (eavsOne.size() != eavsTwo.size()) {
            return false;
        }

        // Form instances are empty, subdefiniton are equals
        if (eavsOne.isEmpty()) {
            return true;
        }

        // Now is possible only one form instance for identity role
        // Get form instance from both identity roles
        IdmFormInstanceDto formInstanceOne = eavsOne.get(0);
        IdmFormInstanceDto formInstanceTwo = eavsTwo.get(0);

        List<Serializable> oneValues = Collections.emptyList();
        List<Serializable> twoValues = Collections.emptyList();
        if (formInstanceOne != null) {
            oneValues = eavsOne.get(0) //
                    .getValues() //
                    .stream() //
                    .map(IdmFormValueDto::getValue) //
                    .collect(Collectors.toList()); //
        }
        if (formInstanceTwo != null) {
            twoValues = eavsTwo.get(0) //
                    .getValues() //
                    .stream() //
                    .map(IdmFormValueDto::getValue) //
                    .collect(Collectors.toList()); //
        }

        // Values doesn't match
        if (oneValues.size() != twoValues.size()) {
            return false;
        }

        // Compare collections
        return CollectionUtils.isEqualCollection(oneValues, twoValues);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

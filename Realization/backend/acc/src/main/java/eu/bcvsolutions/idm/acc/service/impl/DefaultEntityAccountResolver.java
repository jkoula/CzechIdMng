package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.EntityAccountResolver;
import eu.bcvsolutions.idm.acc.service.api.EntityAccountService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class DefaultEntityAccountResolver implements EntityAccountResolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityAccountResolver.class);
    @NotNull
    private final List<EntityAccountService<EntityAccountDto, EntityAccountFilter>> availableServices;
    private final AccAccountService accountService;

    public DefaultEntityAccountResolver(@Autowired(required = false) List<EntityAccountService> availableServices, AccAccountService accountService) {
        this.accountService = accountService;
        this.availableServices = new ArrayList<>();
        availableServices.forEach(this.availableServices::add);
    }

    @Override
    public List<Pair<AccAccountDto, AbstractDto>> resolveEntityAccount(UUID accountId, UUID systemId) {
        return availableServices.stream().flatMap(service -> {
            final EntityAccountFilter entityAccountFilter = ReflectionUtils.instantiateUsingNoArgConstructor(service.getFilterClass(), null);
            if (entityAccountFilter == null) {
                // probably no no args constructor present, skipping
                LOG.warn("No no-args constructor present for filter class [{}], skipping", service.getFilterClass());
                return null;
            }
            entityAccountFilter.setAccountId(accountId);
            entityAccountFilter.setSystemId(systemId);
            //
            return service.find(entityAccountFilter, null).stream()
                    // Get account and owner
                   .map(entityAccount -> Pair.of(service.getAccount(entityAccount), service.getOwner(entityAccount)))
                    // If service did not provide account dto (eg. it did not override getAccount method), then we will try to get it from account service
                    .map(pair -> pair.getLeft() == null ? Pair.of(accountService.get(accountId), pair.getRight()) : pair)
                    // Filter out nulls
                    .filter(pair -> pair.getLeft() != null && pair.getRight() != null);
        }).collect(Collectors.toList());
    }
}

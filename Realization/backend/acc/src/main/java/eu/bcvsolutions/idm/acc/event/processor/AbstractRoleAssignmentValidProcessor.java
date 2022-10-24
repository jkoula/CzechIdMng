package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentValidProcessor<A extends AbstractRoleAssignmentDto, T extends AbstractRoleValidRequestDto<A>> extends AbstractEntityEventProcessor<T> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractRoleAssignmentValidProcessor.class);

    private final IdmRoleAssignmentService<A, ?> roleAssignmentService;
    private final ApplicationContext applicationContext;
    private ProvisioningService provisioningService;

    protected AbstractRoleAssignmentValidProcessor(IdmRoleAssignmentService<A, ?> roleAssignmentService, ApplicationContext applicationContext) {
        super(IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID);
        this.roleAssignmentService = roleAssignmentService;
        this.applicationContext = applicationContext;
        //
        Assert.notNull(applicationContext, "Context is required.");
        Assert.notNull(roleAssignmentService, "Service is required.");
    }

    @Override
    public EventResult<T> process(EntityEvent<T> event) {
        // IdentityRole and IdentityContract must exist - referential integrity.
        //
        // object identityRole is never null
        UUID identityRoleId = event.getContent().getRoleAssignmentUuid();
        A identityRole = roleAssignmentService.get(identityRoleId);
        //
        if (identityRole == null) {
            LOG.warn("[AbstractRoleAssignmentValidProcessor] Identity role doesn't exist for identity role valid request id: [{}]", event.getContent().getId());
            return new DefaultEventResult<>(event, this);
        }
        //
        AbstractDto toProvision = getDtoToProvision(identityRole);
        if (toProvision != null) {
            LOG.info("[AbstractRoleAssignmentValidProcessor] Start with provisioning for identity role valid request id : [{}]", event.getContent().getId());
            boolean requiredProvisioning = getProvisioningService().accountManagement(toProvision);
            if (requiredProvisioning) {
                // do provisioning, for newly valid role
                getProvisioningService().doProvisioning(toProvision);
            }
        } else {
            LOG.warn("[AbstractRoleAssignmentValidProcessor] Identity contract doesn't exist for identity role valid request id: [{}]", event.getContent().getId());
        }
        //
        return new DefaultEventResult<>(event, this);
    }

    /**
     * provisioningService has dependency everywhere - so we need lazy init ...
     *
     * @return instance of {@link ProvisioningService} obtained from application context
     */
    private ProvisioningService getProvisioningService() {
        if (provisioningService == null) {
            provisioningService = applicationContext.getBean(ProvisioningService.class);
        }
        return provisioningService;
    }

    protected abstract AbstractDto getDtoToProvision(A roleAssignment);
}

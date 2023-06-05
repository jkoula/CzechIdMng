package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.impl.AbstractAccountManagementService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentDeleteAccountProcessor<O extends AbstractDto> extends AbstractEntityEventProcessor<AbstractRoleAssignmentDto> {

    protected AbstractRoleAssignmentDeleteAccountProcessor() {
        super(AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE);
    }

    @Override
    public EventResult<AbstractRoleAssignmentDto> process(EntityEvent<AbstractRoleAssignmentDto> event) {
        getAccountManagementService().deleteEntityAccount(event);
        //
        return new DefaultEventResult<>(event, this);
    }

    protected abstract AbstractAccountManagementService<O, ? extends EntityAccountDto> getAccountManagementService();

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public int getOrder() {
        return -ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
    }
}

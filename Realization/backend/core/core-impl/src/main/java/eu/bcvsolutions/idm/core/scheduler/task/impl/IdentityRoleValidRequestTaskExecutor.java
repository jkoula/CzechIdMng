package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.service.RoleAssignmentValidRequestService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Create new account for roles that was newly valid.
 *
 * @author Ondřej Kopr
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Component(IdentityRoleValidRequestTaskExecutor.TASK_NAME)
public class IdentityRoleValidRequestTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleValidRequestTaskExecutor.class);
    public static final String TASK_NAME = "core-identity-role-valid-request-long-running-task";

    private List<RoleAssignmentValidRequestService<AbstractRoleValidRequestDto<?>>> validRequestServices;

    @Autowired
    public IdentityRoleValidRequestTaskExecutor() {
    }

    public List<RoleAssignmentValidRequestService<AbstractRoleValidRequestDto<?>>> getValidRequestServices() {
        return validRequestServices;
    }

    /**
     * Setter injection is used here, because {@link eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager} needs to
     * have no arg constructor.
     * 
     * @param vrs
     */
    @Autowired
    public void setValidRequestServices(List<RoleAssignmentValidRequestService> vrs) {
        this.validRequestServices = new ArrayList<>();
        vrs.forEach(validRequestServices::add);
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public Boolean process() {
        boolean canContinue = true;
        counter = 0L;

        final Set<AbstractRoleValidRequestDto<?>> list = validRequestServices.stream().flatMap(serv -> serv.findAllValid().stream()).collect(Collectors.toSet());

        // init count
        if (count == null) {
            count = (long)list.size();
        }

        LOG.info("Account management starts for all newly valid roles from now. Count [{}]", count);
        for (RoleAssignmentValidRequestService<AbstractRoleValidRequestDto<?>> validRequestService : validRequestServices) {
            final List<AbstractRoleValidRequestDto<?>> allValid = validRequestService.findAllValid();
            for(AbstractRoleValidRequestDto<?> validRequestDto : allValid) {
                validRequestService.publishOrIncrease(validRequestDto);
                counter++;
                canContinue = updateState();
                if (!canContinue) {
                    break;
                }
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}

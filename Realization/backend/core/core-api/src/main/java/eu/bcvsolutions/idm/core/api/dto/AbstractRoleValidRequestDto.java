package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.entity.OperationResult;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleValidRequestDto<A extends AbstractRoleAssignmentDto> extends AbstractDto{
    private int currentAttempt;
    private OperationResult result;

    public int getCurrentAttempt() {
        return currentAttempt;
    }

    public OperationResult getResult() {
        return result;
    }

    public void setCurrentAttempt(int currentAttempt) {
        this.currentAttempt = currentAttempt;
    }

    public void setResult(OperationResult result) {
        this.result = result;
    }

    public void increaseAttempt() {
        this.currentAttempt++;
    }

    public abstract UUID getRoleAssignmentUuid();
}

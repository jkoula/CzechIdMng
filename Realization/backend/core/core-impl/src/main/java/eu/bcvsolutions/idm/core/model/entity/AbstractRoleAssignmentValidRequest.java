package eu.bcvsolutions.idm.core.model.entity;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@MappedSuperclass
public class AbstractRoleAssignmentValidRequest extends AbstractEntity {
    @Embedded
    private OperationResult result;
    @Column(name = "current_attempt")
    private int currentAttempt = 0;

    public OperationResult getResult() {
        return result;
    }

    public void setResult(OperationResult result) {
        this.result = result;
    }

    public int getCurrentAttempt() {
        return currentAttempt;
    }

    public void setCurrentAttempt(int currentAttempt) {
        this.currentAttempt = currentAttempt;
    }

    public void increaseAttempt() {
        this.currentAttempt++;
    }
}

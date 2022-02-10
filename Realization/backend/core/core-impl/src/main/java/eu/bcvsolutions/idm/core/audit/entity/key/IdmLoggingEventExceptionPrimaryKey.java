package eu.bcvsolutions.idm.core.audit.entity.key;

import eu.bcvsolutions.idm.core.audit.entity.IdmLoggingEvent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IdmLoggingEventExceptionPrimaryKey implements Serializable {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private IdmLoggingEvent event;

    @Column(name = "i", nullable = true)
    private Long id;

    public IdmLoggingEvent getEvent() {
        return event;
    }

    public void setEvent(IdmLoggingEvent eventId) {
        this.event = eventId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdmLoggingEventExceptionPrimaryKey)) return false;
        IdmLoggingEventExceptionPrimaryKey that = (IdmLoggingEventExceptionPrimaryKey) o;
        return Objects.equals(getEvent(), that.getEvent()) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvent(), getId());
    }
}

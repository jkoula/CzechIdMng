package eu.bcvsolutions.idm.core.audit.entity;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.audit.entity.key.IdmLoggingEventExceptionPrimaryKey;

/**
 * Entity logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "logging_event_exception", indexes = {
		})
public class IdmLoggingEventException implements BaseEntity {
	
	private static final long serialVersionUID = -1409152284267694057L;

	@EmbeddedId
	private IdmLoggingEventExceptionPrimaryKey id = new IdmLoggingEventExceptionPrimaryKey();
	
	@Column(name = "trace_line", length = DefaultFieldLengths.NAME, nullable = false)
	private String traceLine;

	public Long getId() {
		return id.getId();
	}

	@Override
	public void setId(Serializable id) {
		this.id.setId((Long) id);
	}

	public IdmLoggingEvent getEvent() {
		return id.getEvent();
	}

	public void setEvent(IdmLoggingEvent event) {
		id.setEvent(event);
	}

	public String getTraceLine() {
		return traceLine;
	}

	public void setTraceLine(String traceLine) {
		this.traceLine = traceLine;
	}

	public void setId(IdmLoggingEventExceptionPrimaryKey id) {
		this.id = id;
	}
}

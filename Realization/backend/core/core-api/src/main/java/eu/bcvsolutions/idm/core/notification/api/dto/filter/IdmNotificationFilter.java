package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import java.util.UUID;

import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;

/**
 * Filter for notifications (notification, email ...)
 * 
 * TODO: add topic!
 * TODO: data filter
 * 
 * @author Radek Tomiška
 *
 */
public class IdmNotificationFilter extends QuickFilter {

	private String sender; // senders username
	private String recipient; // recipients username
	private NotificationState state;
	private ZonedDateTime from;
	private ZonedDateTime till;
	private Boolean sent;
	private Class<? extends BaseEntity> notificationType;
	private UUID parent;
	private String topic;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public ZonedDateTime getFrom() {
		return from;
	}

	public void setFrom(ZonedDateTime from) {
		this.from = from;
	}

	public ZonedDateTime getTill() {
		return till;
	}

	public void setTill(ZonedDateTime till) {
		this.till = till;
	}
	
	public NotificationState getState() {
		return state;
	}
	
	public void setState(NotificationState state) {
		this.state = state;
	}
	
	public Boolean getSent() {
		return sent;
	}
	
	public void setSent(Boolean sent) {
		this.sent = sent;
	}
	
	public Class<? extends BaseEntity> getNotificationType() {
		return notificationType;
	}
	
	public void setNotificationType(Class<? extends BaseEntity> notificationType) {
		this.notificationType = notificationType;
	}
	
	public void setParent(UUID parent) {
		this.parent = parent;
	}
	
	public UUID getParent() {
		return parent;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}

package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for missing entity synchronization situation
 * 
 * @author Svanda
 *
 */
public enum SynchronizationMissingEntityActionType {

	CREATE_ENTITY(SynchronizationActionType.CREATE_ENTITY),
	IGNORE(SynchronizationActionType.IGNORE),
	IGNORE_AND_DO_NOT_LOG(SynchronizationActionType.IGNORE_AND_DO_NOT_LOG);

	private SynchronizationActionType action;

	private SynchronizationMissingEntityActionType(SynchronizationActionType action) {
		this.action = action;
	}

	public SynchronizationActionType getAction() {
		return this.action;
	}
}

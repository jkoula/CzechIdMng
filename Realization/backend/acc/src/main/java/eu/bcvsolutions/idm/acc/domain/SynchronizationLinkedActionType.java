package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for linked synchronization situation 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationLinkedActionType {

	UPDATE_ENTITY(SynchronizationActionType.UPDATE_ENTITY),
	UPDATE_ACCOUNT(SynchronizationActionType.UPDATE_ACCOUNT), // produce only entity save event (call provisioning)
	UNLINK(SynchronizationActionType.UNLINK),
	UNLINK_AND_REMOVE_ROLE(SynchronizationActionType.UNLINK_AND_REMOVE_ROLE),
	IGNORE(SynchronizationActionType.IGNORE),
	IGNORE_AND_DO_NOT_LOG(SynchronizationActionType.IGNORE_AND_DO_NOT_LOG);
	
	private SynchronizationActionType action;
	
	private SynchronizationLinkedActionType(SynchronizationActionType action) {
		this.action = action;
	}
	
	public SynchronizationActionType getAction(){
		return this.action;
	}
	
}

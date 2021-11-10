package eu.bcvsolutions.idm.rpt.dto;

/**
 * Enum for report for comparison values in IdM and system.
 *
 * @author Ondrej Husnik
 * @since 12.0.0
 */
public enum RptChangesOnSystemState {

	NO_CHANGE,
	CHANGED,
	ADDED,
	NO_ENTITY_FOR_ACCOUNT,
	NO_ACCOUNT_FOR_ENTITY, 
	FAILED
		
}

package eu.bcvsolutions.idm.core.api.audit.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;

/**
 * This manager implements logging feature suitable for an external SIEM-like tool.
 * 
 * 
 * @author Ondrej Husnik
 * @since 11.2.0
 */
public interface SiemLoggerManager {
	
	/* Constants defining level of logging*/
	final String CONFIGURATION_PROPERTY_PREFIX = "idm.sec.core.logger";
	final String LOG_ON_LEVELS_PROPERTY[] = {"INFO", "DEBUG", "TRACE", "ALL"};
	
	/** !!!BEWARE!!! in case of extending logged entities and the hierarchy keys, take into account 
	 * the maximum length of the logger name specified in the logback.xml file by the parameter %logger{<length>} of the log format. 
	 * If the overall length of the key exceeds the maximum set length, it gets shortened 
	 * and the parser, processing these logs, stops working.  
	 **/
	//root level
	final String ROOT_LEVEL_KEY = "AUDIT";
	// first level
	final String IDENTITY_LEVEL_KEY = "IDENTITY";
	final String ROLE_LEVEL_KEY = "ROLE";
	final String ROLE_PERMISSION_LEVEL_KEY = "ROLE_PERMISSION";
	final String ROLE_ASSIGNMENT_LEVEL_KEY = "ROLE_ASSIGNMENT";
	final String ROLE_REQUEST_LEVEL_KEY = "ROLE_REQUEST";
	final String LRT_LEVEL_KEY = "LRT";
	final String CONFIGURATION_LEVEL_KEY = "CONFIGURATION"; 
	final String LOGIN_LEVEL_KEY = "LOGIN";
	// second level
	final String LOGIN_SUBLEVEL_KEY = "LOGIN";
	final String LOGOUT_SUBLEVEL_KEY = "LOGOUT";
	final String SWITCH_SUBLEVEL_KEY = "USER_SWITCH";
	
	/*Keys of the logged attributes*/
	final String ACTION_ATTRIBUTE = "action";
	final String RESULT_ATTRIBUTE = "result";
	final String TARGET_NAME_ATTRIBUTE = "targetName";
	final String TARGET_UUID_ATTRIBUTE = "targetUUID";
	final String PERFORMED_BY_NAME_ATTRIBUTE = "performedByName";
	final String PERFORMED_BY_UUID_ATTRIBUTE = "performedByUUID";
	final String SUBJECT_NAME_ATTRIBUTE = "subjectName";
	final String SUBJECT_UUID_ATTRIBUTE = "subjectUUID";
	final String TRANSACTION_UUID_ATTRIBUTE = "transactionUUID";
	final String DETAIL_ATTRIBUTE = "detail";
	
	/*Status result*/
	final String SUCCESS_ACTION_STATUS = "SUCCESS";
	final String FAILED_ACTION_STATUS = "FAIL";
	
	final char PARAMETER_DELIMITER_ATTRIBUTE = ':';
	final char LEVEL_DELIMITER_ATTRIBUTE = '.';
	
	
	/**
	 * Logging method specified for entities propagated by events.
	 * Contains some logic to log interesting entities only.
	 * Provides extraction of some meaningful data for logging.
	 * 
	 * @param <E>
	 * @param event
	 * @param status
	 * @param reason
	 */
	<E extends Serializable> void log(EntityEvent<E> event, String status, String reason);
	
	/**
	 * Logging method accepting some attributes in the form of DTOs.
	 * Serves as shortcut method.
	 * 
	 * @param <DTO>
	 * @param action
	 * @param status
	 * @param targetDto
	 * @param subjectDto
	 * @param transactionUuid
	 * @param reason
	 */
	<DTO extends BaseDto> void log(String action, String status, DTO targetDto, DTO subjectDto, String transactionUuid, String reason);
	
	/**
	 * Logging method writing accepting all necessary attributes.
	 * Provides writing to the logger object. 
	 * 
	 * @param action
	 * @param status
	 * @param targetName
	 * @param targetUuid
	 * @param subjectName
	 * @param subjectUuid
	 * @param transactionUuid
	 * @param reason
	 */
	void log(String action, String status, String targetName, String targetUuid, String subjectName, String subjectUuid, String transactionUuid, String reason);
	
	
	
	/**
	 * Builder of the action key.
	 * Creates hierarchy of the logged entities.
	 * 
	 * @param levels
	 * @return
	 */
	String buildAction(String... levels);
}

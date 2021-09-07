package eu.bcvsolutions.idm.core.api.audit.service;


import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * This manager implements logging feature suitable for an external SIEM-like tool.
 * 
 * 
 * @author Ondrej Husnik
 * @since 11.2.0
 */
public interface SiemLoggerManager {
	
	/* Constants defining level of logging*/
	String CONFIGURATION_PROPERTY_PREFIX = "idm.sec.core.logger";
	String[] LOG_ON_LEVELS_PROPERTY = {"INFO", "DEBUG", "TRACE", "ALL"};
	
	/** !!!BEWARE!!! in case of extending logged entities and the hierarchy keys, take into account 
	 * the maximum length of the logger name specified in the logback.xml file by the parameter %logger{<length>} of the log format. 
	 * If the overall length of the key exceeds the maximum set length, it gets shortened 
	 * and the parser, processing these logs, stops working.  
	 **/
	//root level
	String ROOT_LEVEL_KEY = "AUDIT";
	// first level
	String IDENTITY_LEVEL_KEY = "IDENTITY";
	String ROLE_LEVEL_KEY = "ROLE";
	String ROLE_PERMISSION_LEVEL_KEY = "ROLE_PERMISSION";
	String ROLE_ASSIGNMENT_LEVEL_KEY = "ROLE_ASSIGNMENT";
	String ROLE_REQUEST_LEVEL_KEY = "ROLE_REQUEST";
	String LRT_LEVEL_KEY = "LRT";
	String CONFIGURATION_LEVEL_KEY = "CONFIGURATION"; 
	String LOGIN_LEVEL_KEY = "LOGIN";
	// second level
	String LOGIN_SUBLEVEL_KEY = "LOGIN";
	String LOGOUT_SUBLEVEL_KEY = "LOGOUT";
	String SWITCH_SUBLEVEL_KEY = "USER_SWITCH";
	
	/*Keys of the logged attributes*/
	String ACTION_ATTRIBUTE = "action";
	String RESULT_ATTRIBUTE = "result";
	String TARGET_NAME_ATTRIBUTE = "targetName";
	String TARGET_UUID_ATTRIBUTE = "targetUUID";
	String PERFORMED_BY_NAME_ATTRIBUTE = "performedByName";
	String PERFORMED_BY_UUID_ATTRIBUTE = "performedByUUID";
	String SUBJECT_NAME_ATTRIBUTE = "subjectName";
	String SUBJECT_UUID_ATTRIBUTE = "subjectUUID";
	String TRANSACTION_UUID_ATTRIBUTE = "transactionUUID";
	String DETAIL_ATTRIBUTE = "detail";
	
	/*Status result*/
	String SUCCESS_ACTION_STATUS = "SUCCESS";
	String FAILED_ACTION_STATUS = "FAIL";
	
	char PARAMETER_DELIMITER_ATTRIBUTE = ':';
	char LEVEL_DELIMITER_ATTRIBUTE = '.';
	
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
	
	/**
	 * Optimization method skipping the log process according to the configuration.
	 *
	 * @param action
	 * @return
	 */
	boolean skipLogging(String action);
}

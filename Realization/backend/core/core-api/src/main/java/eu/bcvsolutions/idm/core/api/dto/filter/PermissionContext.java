package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Context (~filter) for load permission together with loaded dto.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface PermissionContext extends BaseDataFilter {

	/**
	 * Load permissions into DTO.
	 */
	String PARAMETER_ADD_PERMISSIONS = "addPermissions";
	/**
	 * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
	 * @since 10.3.0
	 */
	String PARAMETER_EVALUATE_PERMISSION = "_permission";
	/**
	 * Operator (AND / OR) to evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
	 * @since 11.1.0
	 */
	String PARAMETER_EVALUATE_PERMISSSION_OPERATOR = "_permission_operator";
	String OPERATOR_AND = "AND";
	String OPERATOR_OR = "OR";
	
	/**
	 * Load permission together with loaded dto.
	 * 
	 * @return true - permissions will be loaded
	 */
    default boolean getAddPermissions() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_PERMISSIONS, false);
    }

    /**
     * Load permission together with loaded dto.
     * 
     * @param value true - permissions will be loaded
     */
    default void setAddPermissions(boolean value) {
    	set(PARAMETER_ADD_PERMISSIONS, value);
    }
    
    /**
     * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @return base permission to evaluate
     * @since 10.3.0
     */
    default BasePermission getEvaluatePermission() {
    	String rawPermission = getParameterConverter().toString(getData(), PARAMETER_EVALUATE_PERMISSION);
    	//
    	return PermissionUtils.toPermission(rawPermission);
    }

    /**
     * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @param base permission to evaluate
     * @since 10.3.0
     */
    default void setEvaluatePermission(BasePermission permission) {
    	set(PARAMETER_EVALUATE_PERMISSION, permission);
    }
    
    /**
     * Evaluate permissions, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @return base permission to evaluate
     * @since 11.1.0
     */
    default List<BasePermission> getEvaluatePermissions() {
    	List<String> rawPermissions = getParameterConverter().toStrings(getData(), PARAMETER_EVALUATE_PERMISSION);
    	//
    	return Lists.newArrayList(PermissionUtils.toPermissions(rawPermissions));
    }

    /**
     * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @param base permission to evaluate
     * @since 11.1.0
     */
    default void setEvaluatePermissions(List<BasePermission> permissions) {
    	put(PARAMETER_EVALUATE_PERMISSION, permissions);
    }
    
    /**
     * Operator (AND / OR) to evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @return operator AND / OR
     * @since 11.1.0
     */
    default String getEvaluatePermissionOperator() {
		return getParameterConverter().toString(getData(), PARAMETER_EVALUATE_PERMISSSION_OPERATOR);
	}
	
    /**
     * Operator (AND / OR) to evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @param operator operator AND / OR - AND operator will be used as default and as a fallback
     * @since 11.1.0
     */
    default void setEvaluatePermissionOperator(String operator) {
		set(PARAMETER_EVALUATE_PERMISSSION_OPERATOR, operator);
	}
    
    /**
     * Use OR operator to evaluate permission, when DTO is loaded.
	 * 
     * @return true - OR, false - AND
     * @since 11.1.0
     */
    @JsonIgnore
    default boolean usePermissionOperatorOr() {
    	return OPERATOR_OR.equals(getEvaluatePermissionOperator());
    }
}
import _ from 'lodash';
//
import ComponentLoader from '../utils/ComponentLoader';

/**
* Service for load module components
*
* @author Radek Tomiška
*/
export default class ComponentService {

  getComponentDescriptor(moduleId) {
    return ComponentLoader.getComponentDescriptor(moduleId);
  }

  /**
   * Find component by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component
   */
  getComponent(componentId) {
    return ComponentLoader.getComponent(componentId);
  }

  /**
   * Find component definition by id. Search in all components (defined in component-descriptor) and all modules
   *
   * @param  {string} componentId
   * @return {object} Component definition
   */
  getComponentDefinition(componentId) {
    return ComponentLoader.getComponentDefinition(componentId);
  }

  /**
   * Finds component definitions by the given type
   *
   * @param  {string} componentType
   * @return {immutable.map} immutable map of component definitions with the given type, key is component id
   */
  getComponentDefinitions(componentType) {
    return ComponentLoader.getComponentDefinitions(componentType);
  }

  /**
   * Returns registered component by type and entity type
   *
   * @param  {string} type       e.g. ENTITY_INFO_COMPONENT_TYPE
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getComponentByEntityType(type, entityType) {
    return this.getComponentByAttributeValue(type, entityType, "entityType");
  }

  getComponentByAttributeValue(type, attributeValue, attributeName) {
    if (!type || !attributeValue || ! attributeName) {
      LOGGER.warn('[ComponentService] Compontent type and antity type is required');
      return null;
    }
    //
    const components = this.getComponentDefinitions(type)
        .filter(component => {
          if (!component[attributeName]) {
            return false;
          }
          // multiple types
          if (_.isArray(component[attributeName])) {
            for (const entityTypeItem of component[attributeName]) {
              if (entityTypeItem.toLowerCase() === attributeValue.toLowerCase()) {
                return true;
              }
            }
            return false;
          }
          // single value
          return component[attributeName].toLowerCase() === attributeValue.toLowerCase();
        })
        .sort((one, two) => {
          return (one.priority || 0) < (two.priority || 0);
        });
    //
    if (!components || components.size === 0) {
      return null;
    }
    //
    return components.first();
  }

  /**
   * Returns registered EntityInfo component by type and entity type
   *
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getEntityInfoComponent(entityType) {
    return this.getComponentByEntityType(ComponentService.ENTITY_INFO_COMPONENT_TYPE, entityType);
  }

  /**
   * Returns registered SelectBox component by type and entity type
   *
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getEntitySelectBoxComponent(entityType) {
    return this.getComponentByEntityType(ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE, entityType);
  }

  /**
   * Returns registered password change components sorted by order and remove disabled components
   *
   * @param  {string} entityType e.g. identity
   * @return {object}            component
   */
  getPasswordChangeComponents() {
    return this.getComponentDefinitions(ComponentService.PASSWORD_CHANGE_COMPONENTS_TYPE);
  }

  /**
   * Returns registered icon component
   *
   * @param  {string} iconType icon identifier
   * @return {object}            component
   */
  getIconComponent(iconType) {
    if (!iconType) {
      return null;
    }
    return this.getComponentByEntityType(ComponentService.ICON_COMPONENT_TYPE, iconType);
  }

  /**
   * Returns registered icon component
   *
   * @param  {string} connectorType identifier
   * @return {object} component
   */
  getConnectorTypeComponent(connectorType) {
    if (!connectorType) {
      return null;
    }
    return this.getComponentByEntityType(ComponentService.CONNECTOR_TYPE, connectorType);
  }

  /**
   * Returns registered monitroing result button component.
   *
   * @param  {string} monitoringType monitoring evaluator simple java type
   * @return {object}            component
   * @since 11.2.0
   */
  getMonitoringResultButtonComponent(monitoringType) {
    if (!monitoringType) {
      return null;
    }
    return this.getComponentByEntityType(ComponentService.MONITORING_RESULT_BUTTON_COMPONENT_TYPE, monitoringType);
  }

  /**
   * Returns registered universal search type component by entity type.
   * @since 12.0.0
   */
  getUniversalSearchTypeComponent(entityType) {
    if (!entityType) {
      return null;
    }
    return this.getComponentByEntityType(ComponentService.UNIVERSAL_SEARCH_TYPE, entityType);
  }

  getComponentForConceptType(conceptType) {
    if (!conceptType) {
      return null
    }
    return this.getComponentByEntityType(ComponentService.ROLE_CONCEPT_MANAGER, conceptType);
  }

  getRoleAssignmentComponents() {
    return this.getComponentDefinitions(ComponentService.ROLE_CONCEPT_MANAGER)
        .sort((one, two) => {
          return (one.priority || 0) < (two.priority || 0);
        });
  }

  getManagerForConceptByOwnerType(selectedOwnerType) {
    const component = this.getComponentByAttributeValue(ComponentService.ROLE_CONCEPT_MANAGER, selectedOwnerType, "ownerType");
    const ManagerType = component.manager;
    return new ManagerType();
  }

  getConcepComponentByOwnerType(selectedOwnerType) {
    return this.getComponentByAttributeValue(ComponentService.ROLE_CONCEPT_MANAGER, selectedOwnerType, "ownerType");
  }

  getApplicantInfoComponent(applicantType) {
    console.log("uuuu applicantType", applicantType)
    return this.getComponentByEntityType(ComponentService.APPLICANT_INFO_COMPONENT_TYPE, applicantType);
  }

}
// reserved component types
ComponentService.ENTITY_INFO_COMPONENT_TYPE = 'entity-info';
ComponentService.DASHBOARD_COMPONENT_TYPE = 'dashboard';
ComponentService.IDENTITY_DASHBOARD_COMPONENT_TYPE = 'identity-dashboard';
ComponentService.IDENTITY_DASHBOARD_BUTTON_COMPONENT_TYPE = 'identity-dashboard-button';
ComponentService.FORM_ATTRIBUTE_RENDERER = 'form-attribute-renderer';
ComponentService.ENTITY_SELECT_BOX_COMPONENT_TYPE = 'entity-select-box';
ComponentService.PASSWORD_CHANGE_COMPONENTS_TYPE = 'password-change-component';
ComponentService.ICON_COMPONENT_TYPE = 'icon';
ComponentService.CONNECTOR_TYPE = 'connector-type';
ComponentService.MODAL_COMPONENT_TYPE = 'modal';
ComponentService.MONITORING_RESULT_BUTTON_COMPONENT_TYPE = 'monitoring-result-button';
ComponentService.UNIVERSAL_SEARCH_TYPE = 'universal-search-type';
ComponentService.ROLE_CONCEPT_MANAGER = 'role-concept-manager';
ComponentService.APPLICANT_INFO_COMPONENT_TYPE = 'applicant-info';


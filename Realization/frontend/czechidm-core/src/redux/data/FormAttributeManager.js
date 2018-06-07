import EntityManager from './EntityManager';
import { FormAttributeService } from '../../services';
import ComponentService from '../../services/ComponentService';
import FormDefinitionManager from './FormDefinitionManager';
import * as Utils from '../../utils';

/**
 * Eav form attributes
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class FormAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormAttributeService();
    this.componentService = new ComponentService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormAttribute';
  }

  getCollectionType() {
    return 'formAttributes';
  }

  /**
   * Returns form value component by form attribute's face and persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {object} component or null
   */
  getFormComponent(attribute) {
    if (!attribute) {
      return null;
    }
    //
    return this.componentService.getComponentDefinitions(ComponentService.FORM_ATTRIBUTE_RENDERER).find(component => {
      if (!component.persistentType) {
        // persistent type is required
        return false;
      }
      // component and attribute face type can be empty => default face by persistent type is used
      const _attributeFaceType = attribute.faceType || attribute.persistentType;
      const _componentFaceType = component.faceType || component.persistentType;
      // persistent and face type has to fit
      return attribute.persistentType.toLowerCase() === component.persistentType.toLowerCase()
          && _attributeFaceType.toLowerCase() === _componentFaceType.toLowerCase();
    });
  }

  /**
   * Returns prefix for localization
   * ''
   * @param  {object} formDefinition
   * @param  {string} formAttribute optional - if attribute is not given, then formDefinition prefix is returned
   * @return {string}
   */
  static getLocalizationPrefix(formDefinition, formAttribute, withModule = true) {
    if (!formDefinition && !formAttribute) {
      return undefined;
    }
    const _formDefinition = formDefinition || formAttribute._embedded.formDefinition;
    if (!_formDefinition) {
      return undefined;
    }
    //
    const definitionPrefix = FormDefinitionManager.getLocalizationPrefix(_formDefinition, withModule);
    if (!formAttribute) {
      // definition prefix only
      return definitionPrefix;
    }
    //
    return `${definitionPrefix}.attributes.${Utils.Ui.spinalCase(formAttribute.code)}`;
  }
}

import React from 'react';
import _ from 'lodash';
import { Basic, Advanced, Domain } from 'czechidm-core';

import { SystemManager, SystemMappingManager } from '../../redux';

const systemManager = new SystemManager();
const systemMappingManager = new SystemMappingManager();

/**
 * Select mapped atribute.
 *
 * @author Ondrej Husnik
 * @since 12.0.0
 */
export default class SystemMappingAttributeFilteredRenderer extends Advanced.AbstractFormAttributeRenderer {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      systemId: null,
      systemMappingId: null
    };
  }

  supportsMultiple() {
    return false; // ~ multiple mapping attributes can be selected in this face
  }

  supportsConfidential() {
    return false;
  }

  /**
   * Sets the selected system value for filtering values in the mapping selection
   */
  onSystemChange(system) {
    const systemId = system ? system.id : null;
    this.setState({
      systemId
    }, () => {
      // clear selected system
      this.refs.systemMapping.setValue(null);
    });
  }

  /**
   * Sets the selected mapping value for filtering values in the attribute selection
   */
  onSystemMappingChange(mapping) {
    const systemMappingId = mapping ? mapping.id : null;
    this.setState({
      systemMappingId
    }, () => {
      // clear selected systemMapping
      this.refs.mappingAttributes.setValue(null);
    });
  }

  /**
   * Implements validation ivocation
   */
  isValid() {
    const systemComponent = this.refs.system;
    const mappingComponent = this.refs.systemMapping;
    let result = true;

    if (!systemComponent.isValid()) {
      systemComponent.setState({
        showValidationError: true
      });
      result = result && false;
    }
    if (!mappingComponent.isValid()) {
      mappingComponent.setState({
        showValidationError: true
      });
      result = result && false;
    }
    return result;
  }

  /**
   * Creates form value with and its ouptut value.
   * @return {[type]} [description]
   */
  toFormValue() {
    const { values } = this.props;
    const systemComponent = this.refs.system;
    const mappingComponent = this.refs.systemMapping;
    const attributeComponent = this.refs.mappingAttributes;
    //
    if (!systemComponent || !mappingComponent || !attributeComponent) {
      // not supported compoenents
      return undefined;
    }
    const systemValue = systemComponent.getValue();
    const mappingValue = mappingComponent.getValue();
    const attributeValues = attributeComponent.getValue();

    const resultObj = {system: systemValue, systemMapping: mappingValue, mappingAttributes: attributeValues};
    const result = JSON.stringify(resultObj);

    let formValue = null;
    if (values && values.length > 0) {
      formValue = values[0];
    }
    return this.fillFormValue(this.prepareFormValue(formValue, 0), result);
  }

  /**
   * Auxiliary method fillong string value into the form.
   */
  fillFormValue(formValue, rawValue) {
    formValue.stringValue = rawValue;
    if (formValue.stringValue === '') {
      // empty string is sent as null => value will not be saved on BE
      formValue.stringValue = null;
    }
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.stringValue;
    //
    return formValue;
  }

  getInputValue(formValue) {
    if (formValue === null) {
      return null;
    }
    const inputValue = formValue.stringValue ? formValue.stringValue : formValue.value;
    //
    try {
      const result = JSON.parse(inputValue);
      return result;
    } catch (error) {
      return null;
    }
  }

  /**
   * Method turning input textual representation of component values into object.
   */
  toInputValues(formValues) {
    if (formValues === null) {
      return null;
    }
    const singleValue = _.isArray(formValues) && formValues.length > 0 ? formValues[0] : formValues;
    //
    return this.getInputValue(singleValue);
  }

  renderSingleInput(originalValues) {
    const { manager, values } = this.props;
    const { systemId, systemMappingId } = this.state;
    const showOriginalValue = !!originalValues;

    const inputValueObj = this.toInputValues(showOriginalValue ? originalValues : values);
    const systemIdValue = inputValueObj ? inputValueObj.system : null;
    const mappingIdValue = inputValueObj ? inputValueObj.systemMapping : null;
    const attributeIdValues = inputValueObj ? inputValueObj.mappingAttributes : null;

    const forceSearchMappings = new Domain.SearchParameters()
      .setFilter('systemId', systemId || systemIdValue || Domain.SearchParameters.BLANK_UUID)
      .setFilter('operationType', 'PROVISIONING');

    const forceSearchMappingAttributes = new Domain.SearchParameters()
      .setFilter('systemId', systemId || systemIdValue || Domain.SearchParameters.BLANK_UUID)
      .setFilter('systemMappingId', systemMappingId || mappingIdValue || Domain.SearchParameters.BLANK_UUID)
      .setFilter('operationType', 'PROVISIONING')
      .setFilter('entityType', 'IDENTITY');

    return (
      <div>
        <Basic.SelectBox
          ref="system"
          value={ systemIdValue }
          manager={ systemManager }
          label={ this.i18n('acc:entity.System._type') }
          onChange={ this.onSystemChange.bind(this) }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          required />
        <Basic.SelectBox
          ref="systemMapping"
          value={ mappingIdValue }
          manager={ systemMappingManager }
          forceSearchParameters={ forceSearchMappings }
          label={ this.i18n('acc:entity.SystemMapping._type') }
          onChange={ this.onSystemMappingChange.bind(this) }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          required />
        <Basic.SelectBox
          ref="mappingAttributes"
          value={ attributeIdValues }
          manager={ manager }
          forceSearchParameters={ forceSearchMappingAttributes }
          label={ this.getLabel() }
          placeholder={ this.getPlaceholder() }
          helpBlock={ this.getHelpBlock() }
          multiSelect
          required={ this.isRequired() }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }/>
      </div>
    );
  }

  renderMultipleInput(originalValues) {
    return this.renderSingleInput(originalValues);
  }
}

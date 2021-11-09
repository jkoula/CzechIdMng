import React from 'react';
import _ from 'lodash';
import { Basic, Advanced, Domain } from 'czechidm-core';

import { SystemManager, SystemMappingManager } from '../../redux';

const systemManager = new SystemManager();
const systemMappingManager = new SystemMappingManager();

/**
 * [SystemAttributeMappingSelect description]
 * @extends Advanced
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
    return true;
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
  toFormValues() {
    const { values } = this.props;
    const systemComponent = this.refs.system;
    const mappingComponent = this.refs.systemMapping;
    const attributeComponent = this.refs.mappingAttributes;
    const filledFormValues = [];
    //
    if (!systemComponent || !mappingComponent || !attributeComponent) {
      // not supported compoenents
      return filledFormValues;
    }
    const systemValue = systemComponent.getValue();
    const mappingValue = mappingComponent.getValue();
    const attributeValues = attributeComponent.getValue();
    if (!systemValue || !mappingValue) {
      // co dal? asi koncime neni co...
    }

    const resultObj = {system: systemValue, systemMapping: mappingValue, mappingAttributes: attributeValues};
    const result = JSON.stringify(resultObj);

    let formValue = null;
    if (values && values.length > 0) {
      formValue = values[0];
    }
    filledFormValues.push(this.fillFormValue(this.prepareFormValue(formValue, 0), result));
    return filledFormValues;
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
    return formValue.stringValue ? formValue.stringValue : formValue.value;
  }

  /**
   * Method turning input textual representation of component values into object.
   */
  toInputValues(formValues) {
    if (formValues === null) {
      return null;
    }
    const singleValue = _.isArray(formValues) && formValues.length > 0 ? formValues[0] : formValues;
    const inputValue = this.getInputValue(singleValue);
    try {
      const result = JSON.parse(inputValue);
      return result;
    } catch (error) {
      return null;
    }
  }

  renderSingleInput(originalValues) {
    const { attribute, manager, values } = this.props;
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
          label="System"
          onChange={ this.onSystemChange.bind(this) }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          required />
        <Basic.SelectBox
          ref="systemMapping"
          value={ mappingIdValue }
          manager={ systemMappingManager }
          forceSearchParameters={ forceSearchMappings }
          label="System mapping"
          onChange={ this.onSystemMappingChange.bind(this) }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }
          required />
        <Basic.SelectBox
          ref="mappingAttributes"
          value={ attributeIdValues }
          manager={ manager }
          forceSearchParameters={ forceSearchMappingAttributes }
          label="Mapping attributes"
          multiSelect={ attribute.multiple }
          required={ this.isRequired() }
          readOnly={ showOriginalValue ? true : this.isReadOnly() }/>
      </div>
    );
  }

  renderMultipleInput(originalValues) {
    return this.renderSingleInput(originalValues);
  }
}

import React from 'react';
import Joi from 'joi';
//
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import SelectBoxFormAttributeRenderer from './SelectBoxFormAttributeRenderer';
import ReadDtoServiceSelect from '../ReadDtoServiceSelect/ReadDtoServiceSelect';

/**
 * Read dto service select component.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class ReadDtoServiceSelectFormAttributeRenderer extends SelectBoxFormAttributeRenderer {

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  supportsMultiple() {
    return false;
  }

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    let validation = Joi.string().max(2000);
    if (!this.isRequired()) {
      validation = validation.concat(Joi.string().allow(null).allow(''));
    }
    return validation;
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
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

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.stringValue ? formValue.stringValue : formValue.value;
  }

  renderSingleInput(originalValues) {
    const { attribute, values, validationErrors, className, style } = this.props;
    const showOriginalValue = !!originalValues;
    //
    return (
      <ReadDtoServiceSelect
        ref={ AbstractFormAttributeRenderer.INPUT }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        value={ this.toInputValue(showOriginalValue ? originalValues : values) }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        validation={ this.getInputValidation() }
        required={ this.isRequired() }
        confidential={ attribute.confidential }
        validationErrors={ validationErrors }
        validationMessage={ attribute.validationMessage }
        className={ className }
        style={ style}/>
    );
  }
}

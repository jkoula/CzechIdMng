import React from 'react';
import moment from 'moment';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';

/**
 * DateTime form value component
 * - based on DateTimePicker - mode (date / datetime) is supported
 *
 * @author Radek Tomiška
 */
export default class DateTimeFormAttributeRenderer extends AbstractFormAttributeRenderer {

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {[type]} formComponent
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.dateValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.dateValue;
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
    return formValue.dateValue ? formValue.dateValue : formValue.value;
  }

  getMinDate(attribute, readOnly = false) {
    const minDays = this.getMin(attribute, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(minDays) || readOnly) {
      return null;
    }
    //
    return moment().add(minDays, 'days');
  }

  getMaxDate(attribute, readOnly = false) {
    const maxDays = this.getMax(attribute, readOnly);
    // not configured
    if (Utils.Ui.isEmpty(maxDays) || readOnly) {
      return null;
    }
    //
    return moment().add(maxDays, 'days');
  }

  renderSingleInput(originalValues) {
    const { attribute, values, validationErrors, className, style } = this.props;
    const showOriginalValue = !!originalValues;
    //
    return (
      <Basic.DateTimePicker
        ref={ AbstractFormAttributeRenderer.INPUT }
        mode={ attribute.persistentType.toLowerCase() }
        required={ this.isRequired() }
        minDate={ this.getMinDate(attribute, showOriginalValue ? true : this.isReadOnly()) }
        maxDate={ this.getMaxDate(attribute, showOriginalValue ? true : this.isReadOnly()) }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        value={ this.toInputValue(showOriginalValue ? originalValues : values) }
        helpBlock={ this.getHelpBlock() }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        validationErrors={ validationErrors }
        validationMessage={ attribute.validationMessage }
        className={ className }
        style={ style }/>
    );
  }

}

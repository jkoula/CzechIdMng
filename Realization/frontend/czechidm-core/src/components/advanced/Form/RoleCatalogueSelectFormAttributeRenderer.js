import React from 'react';
//
import AbstractFormAttributeRenderer from './AbstractFormAttributeRenderer';
import SelectBoxFormAttributeRenderer from './SelectBoxFormAttributeRenderer';
import RoleCatalogueSelect from '../RoleCatalogueSelect/RoleCatalogueSelect';

/**
 * Tree select component
 * - TODO: supports multiple attributes
 * - TODO: validation
 *
 * @author Radek Tomiška
 */
export default class RoleCatalogueSelectFormAttributeRenderer extends SelectBoxFormAttributeRenderer {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state
    };
  }

  /**
   * Returns true, when confidential mode is supported
   *
   * @return {boolean}
   */
  supportsConfidential() {
    return false;
  }

  renderSingleInput(originalValues) {
    const { attribute, values, uiKey } = this.props;
    const showOriginalValue = originalValues ? true : false;
    //
    return (
      <RoleCatalogueSelect
        ref={ AbstractFormAttributeRenderer.INPUT }
        uiKey={ uiKey || `form-attribute-${attribute.code}` }
        manager={ this.getManager() }
        header={ this.getLabel(null, showOriginalValue) }
        label={ this.getLabel(null, showOriginalValue) }
        placeholder={ this.getPlaceholder() }
        helpBlock={ this.getHelpBlock() }
        value={ !attribute.multiple ? this.toInputValue(showOriginalValue ? originalValues : values) : this.toInputValues(showOriginalValue ? originalValues : values) }
        readOnly={ showOriginalValue ? true : this.isReadOnly() }
        required={ this.isRequired() }
        multiSelect={ attribute.multiple }/>
    );
  }
}

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
//
import * as Utils from '../../../utils';
import * as Basic from '../../basic';
import { ConfigurationManager } from '../../../redux';

const configurationManager = new ConfigurationManager();

/**
* Read Dto service select.
*
* @author Radek Tomiška
* @since 11.1.0
*/
export default class ReadDtoServiceSelect extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      showLoading: true,
      options: []
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._loadOptions();
  }

  getComponentKey() {
    return 'component.advanced.ReadDtoServiceSelect';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getValue() {
    return this.refs.input.getValue();
  }

  setValue(value, cb) {
    this.setState({ value }, () => {
      this.refs.input.setValue(value, cb);
    });
  }

  isValid() {
    return this.refs.input.isValid();
  }

  validate(showValidationError, cb) {
    const { readOnly, rendered } = this.props;
    //
    if (readOnly || !rendered) {
      return true;
    }
    //
    return this.refs.input.validate(true, cb);
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        this.refs.input.setState({ showValidationError: json.showValidationError }, cb);
      } else if (cb) {
        cb();
      }
    });
  }

  /**
   * Focus input field
   */
  focus() {
    this.refs.input.focus();
  }

  /**
   * Tree node field label
   *
   * @return  {string}
   */
  getLabel() {
    const { label } = this.props;
    if (label !== undefined) {
      return label;
    }
    return this.i18n('content.system.available-services.header');
  }

  /**
   * Select box field placeholder
   *
   * @return  {string}
   */
  getPlaceholder() {
    const { placeholder } = this.props;
    if (placeholder !== undefined) {
      return placeholder;
    }
    return null;
  }

  /**
   * Select box field help block
   *
   * @return  {string}
   */
  getHelpBlock() {
    const { helpBlock } = this.props;
    if (helpBlock !== undefined) {
      return helpBlock;
    }
    return null;
  }

  _loadOptions(props = null) {
    const _props = props || this.props;
    const { useFirst, rendered } = _props;
    //
    if (!rendered) {
      // component is not rendered ... loading is not needed
      return;
    }
    //
    this.setState({
      showLoading: true
    }, () => {
      this.context.store.dispatch(configurationManager.fetchReadDtoServices((json, error) => {
        if (!error) {
          this._setOptions(json, useFirst);
        } else {
          this._handleError(error);
        }
      }));
    });
  }

  _handleError(error) {
    this.addErrorMessage({
      level: 'error',
      key: 'error-read-dto-services-load'
    }, error);
    this.setState({
      options: [],
      showLoading: false
    }, () => {
      const { value } = this.state;
      this.refs.input.setValue(value);
    });
  }

  _setOptions(options, useFirst) {
    let { value } = this.state;
    const values = _.concat([], value).filter(v => v !== null && v !== undefined && v !== '');
    const _options = [];
    // constuct operation
    options.forEach(item => {
      _options.push({
        value: item.id,
        niceLabel: `${ item.tableName || 'N/A' } (${ Utils.Ui.getSimpleJavaType(item.dtoClass) })`
      });
    });
    if (values.length === 0 && useFirst && _options.length > 0) {
      value = _options[0].value;
    }
    //
    this.setState({
      options: _options,
      showLoading: false
    }, () => {
      this.refs.input.setValue(value);
    });
  }

  render() {
    const {
      required,
      rendered,
      validationErrors,
      validationMessage,
      multiSelect,
      onChange,
      searchable
    } = this.props;
    const { options, value, disabled, readOnly } = this.state;
    const showLoading = this.props.showLoading || this.state.showLoading;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <span>
        <Basic.EnumSelectBox
          ref="input"
          value={ value }
          label={ this.getLabel() }
          placeholder={ this.getPlaceholder() }
          helpBlock={ this.getHelpBlock() }
          readOnly={ readOnly || disabled }
          required={ required }
          validationErrors={ validationErrors }
          validationMessage={ validationMessage }
          showLoading={ showLoading }
          options={ options }
          multiSelect={ multiSelect }
          onChange={ onChange }
          searchable={ searchable }
        />
      </span>
    );
  }
}

ReadDtoServiceSelect.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  /**
   * CodeList code
   */
  code: PropTypes.string.isRequired,
  /**
   * Ui key - identifier for loading data
   */
  uiKey: PropTypes.string,
  /**
   * Selectbox label
   */
  label: PropTypes.string,
  /**
   * Selectbox placeholder
   */
  placeholder: PropTypes.string,
  /**
   * Selectbox help block
   */
  helpBlock: PropTypes.string,
  /**
   * Use the first searched value, if value is empty
   */
  useFirst: PropTypes.bool,
  /**
   * The component is in multi select mode - available just if code list definition is available (simple input is rendered otherwise).
   */
  multiSelect: PropTypes.bool,
  /**
   * On chage seleceted value callback
   */
  onChange: PropTypes.func,
  /**
   * If this is false, then selectbox will not be searchable
   */
  searchable: PropTypes.bool
};

ReadDtoServiceSelect.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  uiKey: 'read-dto-service-select',
  useFirst: false,
  multiSelect: false,
  searchable: true
};

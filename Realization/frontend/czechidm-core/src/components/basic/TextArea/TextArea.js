import PropTypes from 'prop-types';
import Joi from 'joi';
//
import TextField from '../TextField/TextField';

class TextArea extends TextField {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      isTrimmableWarning: this.isAnyLineTrimmable(props.value)
    };
  }

  onChange(event) {
    super.onChange(event);
    this.setState({
      isTrimmableWarning: this.isAnyLineTrimmable(event.target.value)
    });
  }

  /**
   * Checks whether any of individual rows in the TextArea contains
   * leading or trailing whitespaces
   *
  * @return {bool}
  */
  isAnyLineTrimmable(area) {
    if (!area && (typeof area !== 'string')) {
      return false;
    }
    const split = area.split(/\r?\n/);
    let line;
    for (line of split) {
      if (this.isTrimmable(line)) {
        return true;
      }
    }
    return false;
  }

  getValidationDefinition(required) {
    const { min, max } = this.props;
    let validation = super.getValidationDefinition(min ? true : required);

    if (min && max) {
      validation = validation.concat(Joi.string().min(min).max(max).disallow(''));
    } else if (min) {
      validation = validation.concat(Joi.string().min(min));
    } else if (max) {
      if (!required) {
        // if set only max is necessary to set allow null and empty string
        validation = validation.concat(Joi.string().max(max).allow(null).allow(''));
      } else {
        // if set prop required it must not be set allow null or empty string
        validation = validation.concat(Joi.string().max(max));
      }
    }

    return validation;
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  /**
   * Returns soft validation result invoking warning only
   * @return {validationResult object} Object containing setting of validationResult
   */
  softValidationResult() {
    const {type, warnIfTrimmable} = this.props;
    const {isTrimmableWarning} = this.state;

    // Leading/trailing white-spaces warning
    // omits password fields from validation
    if (type !== 'password' && warnIfTrimmable && isTrimmableWarning) {
      return {
        status: 'warning',
        class: 'has-warning has-feedback',
        isValid: true,
        message: this.i18n('validationError.string.isTrimmable')
      };
    }
    return null;
  }

  /**
   *  Sets value and calls validations
   */
  setValue(value, cb) {
    this.setState({
      isTrimmableWarning: this.isAnyLineTrimmable(value),
      value
    }, this.validate.bind(this, false, cb));
  }
}

TextArea.propTypes = {
  ...TextField.propTypes,
  rows: PropTypes.number,
  min: PropTypes.number,
  max: PropTypes.number,
  warnIfTrimmable: PropTypes.bool
};

TextArea.defaultProps = {
  ...TextField.defaultProps,
  rows: 3,
  warnIfTrimmable: false,
  multiline: true
};

export default TextArea;

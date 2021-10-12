import React from 'react';
import PropTypes from 'prop-types';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import ButtonMaterial from './ButtonMaterial';
/**
 * Basic button.
 *
 * @author Radek Tomiška
 */
class BasicButton extends AbstractComponent {

  focus() {
    // TODO ... but is not used till now
  }

  render() {
    // map to material button only ...TODO: focus to material button
    return (
      <ButtonMaterial
        ref="button"
        { ...this.props }/>
    );
  }
}

BasicButton.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Button level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'link', 'primary']),
  /**
   * When showLoading is true, then showLoadingIcon is shown
   */
  showLoadingIcon: PropTypes.bool,
  /**
   *  When showLoading is true, this text will be shown
   */
  showLoadingText: PropTypes.string,
  /**
   * Title position
   */
  titlePlacement: PropTypes.oneOf(['top', 'bottom', 'right', 'left']),
  /**
   * Title show delay
   */
  titleDelayShow: PropTypes.number,
  /**
   * Button icon
   */
  icon: PropTypes.string,
  /**
   * On click node callback
   */
  onClick: PropTypes.func,
  /**
   * On double click node callback
   */
  onDoubleClick: PropTypes.func,
  /**
   * Button size (by bootstrap).
   *
   * @since 10.3.0
   */
  buttonSize: PropTypes.oneOf(['default', 'xs', 'sm', 'lg']),
  /**
   * Button color
   *
   * @since 12.0.0
   */
  color: PropTypes.string
};
BasicButton.defaultProps = {
  ...AbstractComponent.defaultProps,
  type: 'button',
  level: 'default',
  buttonSize: 'default',
  hidden: false,
  showLoadingIcon: false,
  showLoadingText: null,
  titlePlacement: 'right',
  icon: null,
  color: null
};

export default BasicButton;

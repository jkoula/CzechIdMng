import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import clsx from 'clsx';
import _ from 'lodash';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import Tooltip from '../Tooltip/Tooltip';
//
const useStyles = makeStyles((theme) => {
  return {
    successIcon: {
      color: theme.palette.success.main,
      '&:hover': {
        color: theme.palette.success.dark,
      }
    },
    success: {
      color: theme.palette.success.contrastText,
      backgroundColor: theme.palette.success.main,
      '&:hover': {
        backgroundColor: theme.palette.success.dark,
      }
    },
    infoIcon: {
      color: theme.palette.info.main,
      '&:hover': {
        color: theme.palette.info.dark,
      }
    },
    info: {
      color: theme.palette.info.contrastText,
      backgroundColor: theme.palette.info.main,
      '&:hover': {
        backgroundColor: theme.palette.info.dark,
      }
    },
    warningIcon: {
      color: theme.palette.warning.main,
      '&:hover': {
        color: theme.palette.warning.dark,
      }
    },
    warning: {
      color: theme.palette.warning.contrastText,
      backgroundColor: theme.palette.warning.main,
      '&:hover': {
        backgroundColor: theme.palette.warning.dark,
      }
    }
  };
});

/**
 * Button with Material-UI.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
function ButtonMaterial(props) {
  const {
    level,
    buttonSize,
    text,
    className,
    children,
    showLoading,
    showLoadingIcon,
    showLoadingText,
    disabled,
    hidden,
    type,
    rendered,
    title,
    titlePlacement,
    titleDelayShow,
    style,
    onClick,
    onDoubleClick,
    icon,
    endIcon,
    tabIndex,
    fullWidth,
    color
  } = props;
  //
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  const _level = Utils.Ui.toLevel(level);
  const classNames = classnames(
    'basic-button',
    { hidden },
    className
  );
  let _showLoadingText = text || children;
  if (showLoadingText !== null) {
    _showLoadingText = showLoadingText;
  }
  //
  let _color;
  let _statusClasses = null;
  if (color) {
    _color = color;
  } else {
    _color = 'default';
    if (!disabled && !showLoading) {
      if (_level === 'error' || _level === 'danger') {
        _color = 'secondary';
      } else if (_level === 'link' || _level === 'primary') {
        _color = 'primary';
      } else if (_level === 'success') {
        _statusClasses = classes.success;
      } else if (_level === 'info') {
        _statusClasses = classes.info;
      } else if (_level === 'warning') {
        _statusClasses = classes.warning;
      }
    }
  }
  //
  let _size = 'medium';
  if (buttonSize === 'xs' || (className && _.isString(className) && _.includes(className, 'btn-xs'))) {
    _size = 'small';
  } else if (buttonSize === 'lg' || (className && _.isString(className) && _.includes(className, 'btn-lg'))) {
    _size = 'large';
  }
  //
  let component = null;
  if (!text && (!children || children.length === 0) && icon) {
    if (!disabled && !showLoading) {
      if (_level === 'success') {
        _statusClasses = classes.successIcon;
      } else if (_level === 'info') {
        _statusClasses = classes.infoIcon;
      } else if (_level === 'warning') {
        _statusClasses = classes.warningIcon;
      }
    }
    //
    component = (
      <IconButton
        type={ type || 'button' }
        color={ _color }
        style={ style }
        className={ clsx(classNames, _statusClasses) }
        size={ _size }
        disabled={ disabled || showLoading }
        onClick={ onClick }
        onDoubleClick={ onDoubleClick }
        tabIndex={ tabIndex }>
        {
          showLoading && showLoadingIcon
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          <Icon value={ icon } disabled={ disabled }/>
        }
      </IconButton>
    );
  } else {
    component = (
      <Button
        type={ type || 'button' }
        startIcon={
          showLoading && showLoadingIcon
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          <Icon value={ icon } disabled={ disabled }/>
        }
        endIcon={ endIcon }
        color={ _color }
        size={ _size }
        className={ clsx(classNames, _statusClasses) }
        style={ style }
        variant={ _level === 'link' ? null : 'contained' }
        disabled={ disabled || showLoading }
        onClick={ onClick }
        onDoubleClick={ onDoubleClick }
        tabIndex={ tabIndex }
        disableElevation={ false }
        title={ title }
        fullWidth={ fullWidth }>
        {
          showLoading && _showLoadingText
          ?
          _showLoadingText
          :
          <span>
            { text }
            { children }
          </span>
        }
      </Button>
    );
  }
  // no title
  if (!title) {
    return component;
  }
  // append title
  return (
    <Tooltip value={ title } delayShow={ titleDelayShow } placement={ titlePlacement }>
      <span>
        { component }
      </span>
    </Tooltip>
  );
}

ButtonMaterial.propTypes = {
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
ButtonMaterial.defaultProps = {
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

export default ButtonMaterial;

import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import _ from 'lodash';
//
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import ComponentService from '../../../services/ComponentService';

const componentService = new ComponentService();
//
export const TYPE_GLYPHICON = 'glyph';
export const TYPE_FONT_AWESOME = 'fa'; // https://fortawesome.github.io/Font-Awesome/examples/
export const TYPE_FONT_AWESOME_FAR = 'far';
export const TYPE_FONT_AWESOME_FAS = 'fas';
export const TYPE_COMPONENT = 'component';
const TYPE_ELEMENT = 'element';

/**
 * Returns resolved type and icon from given parameters
 *
 * @param  {string} type  icon type
 * @param  {string} icon  requested icon - could contain type definition e.g. `fa:group`
 * @param  {string} value parameter icon alias
 * @return {{_type, _icon}}  object represents resolved type and icon
 * @author Radek Tomiška
 */
function resolveParams(type, icon, value) {
  // value could contain type definition
  //
  const _iconValue = icon || value;
  if (!_iconValue) {
    return {};
  }
  if (!_.isString(_iconValue)) {
    return {
      _type: TYPE_ELEMENT,
      _icon: _iconValue
    };
  }
  const _iconValues = _iconValue.split(':');
  let _type = type;
  let _icon = _iconValue;
  if (_iconValues.length === 2) {
    _type = _iconValues[0];
    _icon = _iconValues[1];
  }
  return {
    _type,
    _icon
  };
}

/**
 * Theme decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => {
  return {
    default: {
      // nothing
    },
    info: {
      color: theme.palette.info.main
    },
    warning: {
      color: theme.palette.warning.main
    },
    error: {
      color: theme.palette.error.main
    },
    success: {
      color: theme.palette.success.main
    },
    primary: {
      color: theme.palette.primary.main
    },
    secondary: {
      color: theme.palette.secondary.main
    }
  };
});

/**
 * Icon
 * - it's a little advanced icon now (component usage).
 *
 * @author Radek Tomiška
 */
export default function BasicIcon(props) {
  const {
    rendered,
    type,
    icon,
    value,
    ...other
  } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  // value could contain type definition
  const { _type, _icon } = resolveParams(type, icon, value);
  if (_type === TYPE_COMPONENT) {
    const component = componentService.getIconComponent(_icon);
    if (component) {
      const IconComponent = component.component;
      if (_.isString(IconComponent)) {
        // recursive - basic icon
        return (
          <BasicIcon value={ IconComponent } { ...other } />
        );
      }
      return (
        <IconComponent { ...other }/>
      );
    }
    return (
      <span title="Icon not found in component library">
        {' '}
        { _icon }
        {' '}
      </span>
    );
  }
  //
  // Basic icon will be rendered
  const {
    className,
    showLoading,
    color,
    style,
    disabled,
    title,
    onClick,
    level,
    iconSize
  } = props;
  //
  // without icon defined returns null
  if (!_icon) {
    return null;
  }
  //
  let classNames = classnames(
    classes[Utils.Ui.toLevel(level)],
  );
  if (_type === TYPE_ELEMENT) {
    // FIXME: support other props
    classNames = classnames(
      classNames,
      'basic-icon',
      { disabled: disabled === true },
      { 'fa-2x': iconSize === 'sm' },
      { 'fa-6x': iconSize === 'lg' },
      className,
    );
    return (
      <span
        title={ title }
        className={ classNames }
        style={ style }>
        { _icon }
      </span>
    );
  }
  //
  if (showLoading) {
    classNames = classnames(
      classNames,
      'basic-icon',
      'fa',
      'fa-refresh',
      'fa-spin',
      className
    );
  } else {
    classNames = classnames(
      classNames,
      'basic-icon',
      { glyphicon: _type === TYPE_GLYPHICON },
      { [`glyphicon-${ _icon}`]: _type === TYPE_GLYPHICON },
      { fa: _type === TYPE_FONT_AWESOME },
      { far: _type === TYPE_FONT_AWESOME_FAR },
      { fas: _type === TYPE_FONT_AWESOME_FAS },
      { [`fa-${ _icon}`]: _type === TYPE_FONT_AWESOME || _type === TYPE_FONT_AWESOME_FAR || _type === TYPE_FONT_AWESOME_FAS },
      { disabled: disabled === true },
      { 'fa-2x': iconSize === 'sm' },
      { 'fa-4x': iconSize === 'md' },
      { 'fa-6x': iconSize === 'lg' },
      className,
    );
  }
  const _style = _.merge({}, style);
  if (color) {
    _style.color = color;
  }
  return (
    <span
      title={ title }
      className={ classNames }
      aria-hidden="true"
      style={ _style }
      onClick={ disabled ? null : onClick }/>
  );
}

BasicIcon.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * glyphicon or font-awesome, default glyph
   */
  type: PropTypes.oneOf([TYPE_GLYPHICON, TYPE_FONT_AWESOME]),
  /**
   * glyphicon or font-awesome (by type) suffix name
   */
  icon: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool // false => no icon
  ]),
  /**
   * glyphicon or font-awesome (by type) suffix name - alias to icon property, has lower priority
   */
  value: PropTypes.string,
  /**
   * Adds css, not clickable.
   */
  disabled: PropTypes.bool,
  /**
   * Standard onClick callback.
   */
  onClick: PropTypes.func,
  /**
   * Icon level (~color) / css / class.
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary']),
  /**
   * Icon size sm = 2x, lg = 6x.
   *
   * @since 10.8.0
   */
  iconSize: PropTypes.oneOf(['default', 'sm', 'md', 'lg'])
};

BasicIcon.defaultProps = {
  ...AbstractComponent.defaultProps,
  type: TYPE_GLYPHICON,
  disabled: false,
  level: 'default'
};

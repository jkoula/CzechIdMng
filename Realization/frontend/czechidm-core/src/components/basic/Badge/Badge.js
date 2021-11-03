import React from 'react';
import PropTypes from 'prop-types';
//
import Badge from '@material-ui/core/Badge';
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Theme decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => {
  return {
    default: {
      color: theme.palette.text.primary,
      backgroundColor: theme.palette.background.default,
    },
    info: {
      color: theme.palette.info.contrastText,
      backgroundColor: theme.palette.info.main
    },
    warning: {
      color: theme.palette.warning.contrastText,
      backgroundColor: theme.palette.warning.main
    },
    error: {
      color: theme.palette.error.contrastText,
      backgroundColor: theme.palette.error.main
    },
    success: {
      color: theme.palette.success.contrastText,
      backgroundColor: theme.palette.success.main
    },
    primary: {
      color: theme.palette.primary.contrastText,
      backgroundColor: theme.palette.primary.main
    },
    secondary: {
      color: theme.palette.secondary.contrastText,
      backgroundColor: theme.palette.secondary.main
    },
    inverse: {
      color: '#ffffff',
      backgroundColor: '#333333'
    }
  };
});

/**
 * Badge box.
 *
 * @author Radek Tomiška
 */
export default function BasicBadge(props) {
  const { level, title, text, value, className, rendered, showLoading, style, children, onClick } = props;
  const _text = text || value;
  const classes = useStyles();
  //
  if (!rendered || !_text) {
    return null;
  }
  //
  let _style = style;
  if (onClick) {
    _style = {
      cursor: 'pointer',
      ...style
    };
  }
  //
  return (
    <Badge
      onClick={ onClick }
      className={ className }
      badgeContent={
        showLoading
        ?
        <Icon type="fa" icon="refresh" showLoading/>
        :
        _text
      }
      title={ title }
      style={ _style }
      showZero
      classes={{ badge: classes[Utils.Ui.toLevel(level)] }}>
      {
        children
        ||
        <span style={{ visibility: 'hidden' }}>i</span>
      }
    </Badge>
  );
}

BasicBadge.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Badge level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error']),
  /**
   * Badge text content
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ]),
  /**
   * Badge text content (text alias - text has higher priority)
   */
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ])
};

BasicBadge.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

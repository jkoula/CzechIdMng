import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
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
      // nothing
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
function Badge(props) {
  const { level, title, text, value, className, rendered, showLoading, style, ...others } = props;
  const _text = text || value;
  const classes = useStyles();
  //
  if (!rendered || !_text) {
    return null;
  }
  //
  const classNames = classnames(
    'badge',
    classes[Utils.Ui.toLevel(level)],
    className
  );
  //
  let _style = style;
  if (others.onClick) {
    _style = {
      cursor: 'pointer',
      ...style
    };
  }
  return (
    <span
      className={ classNames }
      title={ title }
      style={ _style }
      { ...others }>
      {
        showLoading
        ?
        <Icon type="fa" icon="refresh" showLoading/>
        :
        _text
      }
    </span>
  );
}

Badge.propTypes = {
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

Badge.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export default Badge;

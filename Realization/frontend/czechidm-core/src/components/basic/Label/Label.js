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
    root: {
      borderRadius: theme.shape.borderRadius
    },
    default: {
      // nothing
    },
    info: {
      backgroundColor: theme.palette.info.main
    },
    warning: {
      backgroundColor: theme.palette.warning.main
    },
    error: {
      backgroundColor: theme.palette.error.main
    },
    success: {
      backgroundColor: theme.palette.success.main
    },
    primary: {
      backgroundColor: theme.palette.primary.main
    },
    secondary: {
      backgroundColor: theme.palette.secondary.main
    }
  };
});

/**
 * Label box.
 *
 * @author Radek Tomiška
 */
export default function Label(props) {
  const { level, title, text, value, className, rendered, showLoading, ...others } = props;
  const _text = text || value;
  const classes = useStyles();
  //
  if (!rendered || !_text) {
    return null;
  }
  //
  const classNames = classnames(
    'label',
    classes.root,
    classes[Utils.Ui.toLevel(level)],
    className
  );
  return (
    <span className={ classNames } title={ title } { ...others }>
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

Label.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Label level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary', 'secondary']),
  /**
   * Label text content
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ]),
  /**
   * Label text content (text alias - text has higher priority)
   */
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ])
};

Label.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

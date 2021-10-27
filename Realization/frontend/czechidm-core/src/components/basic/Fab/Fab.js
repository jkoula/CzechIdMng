import React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
//
import Fab from '@material-ui/core/Fab';
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Fab decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => ({
  primary: {
    backgroundColor: theme.palette.primary.main,
  },
  secondary: {
    backgroundColor: theme.palette.secondary.main,
  },
  error: {
    backgroundColor: theme.palette.error.main,
  },
  warning: {
    backgroundColor: theme.palette.warning.main,
  },
  success: {
    backgroundColor: theme.palette.success.main,
  },
  info: {
    backgroundColor: theme.palette.info.main,
  }
}));

function BasicAvatar(props) {
  const { rendered, level, color, size, className, children } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  const _level = Utils.Ui.toLevel(level);
  //
  return (
    <Fab
      size={ size }
      color={ color }
      className={ clsx(className, { [classes[_level]]: _level !== 'default' }) }>
      { children }
    </Fab>
  );
}

BasicAvatar.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Button level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary']),
  color: PropTypes.string,
  size: PropTypes.string
};

BasicAvatar.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default',
  color: null,
};

export default BasicAvatar;

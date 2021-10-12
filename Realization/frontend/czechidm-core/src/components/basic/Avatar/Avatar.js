import React from 'react';
import PropTypes from 'prop-types';
import Avatar from '@material-ui/core/Avatar';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Avatar decorator.
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
  danger: {
    backgroundColor: theme.palette.secondary.main,
  },
  error: {
    backgroundColor: theme.palette.secondary.main,
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
  const { rendered, level, src, alt, children } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  return (
    <Avatar
      alt={ alt }
      src={ src }
      className={ level ? classes[level] : null }>
      { children }
    </Avatar>
  );
}

BasicAvatar.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Button level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary'])
};

BasicAvatar.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export default BasicAvatar;

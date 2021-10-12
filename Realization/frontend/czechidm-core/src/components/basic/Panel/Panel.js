import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Div from '../Div/Div';

const useStyles = makeStyles((theme) => {
  return {
    root: {
      backgroundColor: theme.palette.type === 'dark' ? '#333' : '#fff'
    },
    default: {
      backgroundColor: theme.palette.background.paper
    },
    info: {
      borderColor: theme.palette.info.main,
      '& .panel-heading': {
        color: theme.palette.info.contrastText,
        borderColor: theme.palette.info.main,
        backgroundColor: theme.palette.info.light
      }
    },
    warning: {
      borderColor: theme.palette.warning.main,
      '& .panel-heading': {
        color: theme.palette.warning.contrastText,
        borderColor: theme.palette.warning.main,
        backgroundColor: theme.palette.warning.light
      }
    },
    error: {
      borderColor: theme.palette.error.main,
      '& .panel-heading': {
        color: theme.palette.error.contrastText,
        borderColor: theme.palette.error.main,
        backgroundColor: theme.palette.error.light
      }
    },
    success: {
      borderColor: theme.palette.success.main,
      '& .panel-heading': {
        color: theme.palette.success.contrastText,
        borderColor: theme.palette.success.main,
        backgroundColor: theme.palette.success.light
      }
    },
    primary: {
      borderColor: theme.palette.primary.main,
      '& .panel-heading': {
        color: theme.palette.primary.contrastText,
        borderColor: theme.palette.primary.main,
        backgroundColor: theme.palette.primary.light
      }
    },
    secondary: {
      borderColor: theme.palette.secondary.main,
      '& .panel-heading': {
        color: theme.palette.secondary.contrastText,
        borderColor: theme.palette.secondary.main,
        backgroundColor: theme.palette.secondary.light
      }
    }
  };
});

/**
 * Basic panel decorator.
 *
 * @author Radek Tomiška
 */
export default function Panel(props) {
  const { className, rendered, showLoading, level, style, children, onClick } = props;
  const classes = useStyles();
  //
  if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
    return null;
  }
  //
  const classNames = classnames(
    'basic-panel',
    'panel',
    `panel-${ level }`,
    classes.root,
    classes[Utils.Ui.toLevel(level)],
    className,
  );
  //
  return (
    <Div className={ classNames } style={ style } onClick={ onClick } showLoading={ showLoading }>
      { children }
    </Div>
  );
}

Panel.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Panel level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'primary'])
};
Panel.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

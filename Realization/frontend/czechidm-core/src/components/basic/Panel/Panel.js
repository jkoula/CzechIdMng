import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { useSelector } from 'react-redux';
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
  const userContext = useSelector((state) => state.security.userContext);
  //
  if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
    return null;
  }
  //
  // try to find panel header and its uiKey => resolve panel is collapsed or not
  // lookout: is required here, because panel is rerendered, after profile is persisted and loaded into redux again
  let _collapsed = false;
  const _children = React.Children.map(children, child => {
    if (React.isValidElement(child)) {
      if (child.type
        && child.type.__PanelHeader__
        && child.props.uiKey
        && userContext
        && userContext.profile
        && userContext.profile.setting
        && userContext.profile.setting[child.props.uiKey]) { // or personalized by profile
        _collapsed = !!userContext.profile.setting[child.props.uiKey].collapsed;
      }
    }
    return child;
  });
  //
  const classNames = classnames(
    'basic-panel',
    'panel',
    `panel-${ level }`,
    { collapsed: _collapsed },
    classes.root,
    classes[Utils.Ui.toLevel(level)],
    className,
  );
  //
  return (
    <Div className={ classNames } style={ style } onClick={ onClick } showLoading={ showLoading }>
      { _children }
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

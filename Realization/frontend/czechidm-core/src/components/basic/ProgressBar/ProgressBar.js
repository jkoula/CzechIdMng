import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import LinearProgress from '@material-ui/core/LinearProgress';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import Typography from '../Typography/Typography';
//
const useStyles = makeStyles((theme) => {
  return {
    primary: {
      '& .MuiLinearProgress-barColorPrimary': {
        backgroundColor: theme.palette.success.main,
        color: theme.palette.success.main
      }
    },
    secondary: {
      '& .MuiLinearProgress-barColorPrimary': {
        backgroundColor: theme.palette.error.main,
        color: theme.palette.error.main
      }
    }
  };
});

/**
 * Wrapped progress bar.
 *
 * @author Radek Tomiška
 */
export default function BasicProgressBar(props) {
  const {
    rendered,
    showLoading,
    label,
    min,
    max,
    now,
    active,
    style,
    className,
    bsStyle,
    children
  } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  if (showLoading) {
    return (
      <Icon value="fa:refresh" showLoading/>
    );
  }
  // add component className
  const classNames = classnames(
    'basic-progress-bar',
    className
  );
  //
  let _max = max;
  if (_max === null && now > 0) {
    _max = now * 2;
  }
  //
  let percent = 0;
  if ((_max - min) > 0) {
    percent = (now / (_max - min)) * 100;
  }
  //
  const _style = style || {};
  _style.display = _style.display || 'flex';
  _style.alignItems = _style.alignItems || 'center';
  //
  return (
    <div
      style={ _style }
      classNames={ classNames }>
      <div style={{ flex: 1 }}>
        <LinearProgress
          variant={ max === null && active ? 'indeterminate' : 'determinate'}
          value={ percent }
          className={ classes[bsStyle === 'warning' || bsStyle === 'error' ? 'secondary' : 'primary'] }/>
      </div>
      {
        !label
        ||
        <div style={{ whiteSpace: 'nowrap', minWidth: 50, textAlign: 'right' }}>
          <Typography variant="body2" color="textSecondary">
            { label }
          </Typography>
        </div>
      }
      { children }
    </div>
  );
}

BasicProgressBar.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Start count
   */
  min: PropTypes.number,
  /**
   * End count
   */
  max: PropTypes.number,
  /**
   * Actual counter
   */
  now: PropTypes.number,
  /**
   * Label
   */
  label: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  /**
   * Adds animation -  the stripes right to left. Not available in IE9 and below.
   */
  active: PropTypes.bool,
  bsStyle: PropTypes.string
};

BasicProgressBar.defaultProps = {
  ...AbstractComponent.defaultProps,
  min: 0,
  now: 0,
  active: true
};

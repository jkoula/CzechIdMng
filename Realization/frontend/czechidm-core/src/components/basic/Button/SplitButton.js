import React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import _ from 'lodash';
import Button from '@material-ui/core/Button';
import ButtonGroup from '@material-ui/core/ButtonGroup';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import Popper from '@material-ui/core/Popper';
import MenuList from '@material-ui/core/MenuList';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Quick continue button.
 *
 * @author Radek Tomiška
 */
const useStyles = makeStyles((theme) => (
  {
    successIcon: {
      color: theme.palette.success.main,
      '&:hover': {
        color: theme.palette.success.dark,
      }
    },
    success: {
      color: '#fff',
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
      color: '#fff',
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
      color: '#fff',
      backgroundColor: theme.palette.warning.main,
      '&:hover': {
        backgroundColor: theme.palette.warning.dark,
      }
    }
  }
));

export default function BasicSplitButton(props) {
  const {
    level,
    buttonSize,
    icon,
    rendered,
    showLoading,
    showLoadingIcon,
    showLoadingText,
    disabled,
    title,
    className,
    onClick
  } = props;
  //
  const classes = useStyles();
  const [open, setOpen] = React.useState(false);
  const anchorRef = React.useRef(null);
  //
  if (!rendered) {
    return null;
  }
  //
  const handleToggle = () => {
    setOpen((prevOpen) => !prevOpen);
  };

  const handleClose = (event) => {
    if (anchorRef.current && anchorRef.current.contains(event.target)) {
      return;
    }
    setOpen(false);
  };
  //
  let _level = level;
  if (_level) {
    _level = _level.toLowerCase();
  }
  let _color = 'default';
  let _statusClasses = null;
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
  //
  let _size = 'medium';
  if (buttonSize === 'xs' || (className && _.isString(className) && _.includes(className, 'btn-xs'))) {
    _size = 'small';
  } else if (buttonSize === 'lg' || (className && _.isString(className) && _.includes(className, 'btn-lg'))) {
    _size = 'large';
  }
  //
  return (
    <span>
      <ButtonGroup
        variant="contained"
        color={ _color }
        ref={ anchorRef }
        aria-label="split button"
        disabled={ disabled || showLoading }
        size={ _size }
        className={ clsx(className, _statusClasses) }>
        <Button
          onClick={ onClick }
          className={ clsx(className, _statusClasses) }
          startIcon={ showLoading && showLoadingIcon ? <Icon type="fa" icon="refresh" showLoading/> : <Icon value={ icon } /> }
          size={ _size }>
          { showLoading && showLoadingText ? showLoadingText : title }
        </Button>
        <Button
          color={ _color }
          className={ clsx(className, _statusClasses) }
          size="small"
          aria-controls={ open ? 'split-button-menu' : undefined }
          aria-expanded={ open ? 'true' : undefined }
          aria-label="select split action"
          aria-haspopup="menu"
          onClick={ handleToggle }>
          <ArrowDropDownIcon />
        </Button>
      </ButtonGroup>
      <Popper open={ open } anchorEl={ anchorRef.current } role={ undefined } transition disablePortal>
        {({ TransitionProps, placement }) => (
          <Grow
            { ...TransitionProps }
            style={{
              transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom',
            }}>
            <Paper>
              <ClickAwayListener onClickAway={ handleClose }>
                <MenuList id="split-button-menu">
                  { props.children }
                </MenuList>
              </ClickAwayListener>
            </Paper>
          </Grow>
        )}
      </Popper>
    </span>
  );
}

BasicSplitButton.propTypes = {
  ...AbstractComponent.propTypes,
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.element]).isRequired,
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'link']),
};
BasicSplitButton.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

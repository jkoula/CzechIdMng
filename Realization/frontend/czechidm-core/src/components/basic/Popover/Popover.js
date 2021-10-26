import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
import Popover from '@material-ui/core/Popover';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Loading from '../Loading/Loading';
import Panel from '../Panel/Panel';
import PanelHeader from '../Panel/PanelHeader';
import PanelBody from '../Panel/PanelBody';
import Typography from '../Typography/Typography';

const useStyles = makeStyles((theme) => ({
  popover: {
    cursor: 'pointer',
    '& .basic-alert': {
      minWidth: 400
    }
  },
  popoverHover: {
    pointerEvents: 'none',
  },
  paper: {
    minWidth: 300,
    padding: 3 // ~ backward compatible
  },
  typography: {
    display: 'inline-block',
    fontSize: 'inherit',
    color: theme.palette.primary.main,
  }
}));

/**
 * Material popover.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
function MaterialPopover(props) {
  const {
    id,
    open,
    onOpen,
    onClose,
    level,
    rendered,
    children,
    value,
    text,
    title,
    placement,
    showLoading,
    trigger,
    className,
    icon,
    rootClose,
    onEnter,
    onExit
  } = props;
  const classes = useStyles();
  const [ anchorEl, setAnchorEl ] = React.useState(null);
  const [ _id ] = React.useState(id);
  const hasClickTrigger = (trigger === 'click' || _.includes(trigger, 'click'));
  const hasHoverTrigger = (trigger === 'hover' || _.includes(trigger, 'hover'));
  //
  if (!rendered || (!children)) {
    return null;
  }
  //
  const handlePopoverOpen = (event) => {
    setAnchorEl(event.currentTarget);
    onOpen();
  };
  //
  const handlePopoverClose = () => {
    setAnchorEl(null);
    onClose();
  };
  //
  const _onClose = (event, reason) => {
    if (reason === 'backdropClick' && !rootClose) {
      return;
    }
    if (onExit) {
      onExit();
    }
    handlePopoverClose(event);
  };
  //
  const classNames = classnames(
    'basic-popover',
    classes.popover,
    { [classes.popoverHover]: hasHoverTrigger },
    className
  );
  let content;
  if (showLoading) {
    content = (
      <Loading isStatic show/>
    );
  } else {
    content = value || text;
  }
  if (title) {
    content = (
      <Panel level={ level }>
        <PanelHeader text={ title } icon={ icon } showLoading={ showLoading }/>
        <PanelBody>
          { content }
        </PanelBody>
      </Panel>
    );
  }
  //
  return (
    <>
      <Typography
        onClick={ hasClickTrigger && !hasHoverTrigger ? handlePopoverOpen : undefined }
        aria-owns={ open ? _id : undefined }
        aria-haspopup="true"
        onMouseEnter={ hasHoverTrigger ? handlePopoverOpen : undefined }
        onMouseLeave={ hasHoverTrigger ? handlePopoverClose : undefined }
        className={ classes.typography }>
        { children }
      </Typography>
      <Popover
        disableEnforceFocus={ hasHoverTrigger }
        disableAutoFocus={ hasHoverTrigger}
        disableScrollLock={ hasHoverTrigger }
        disableRestoreFocus={ hasHoverTrigger }
        id={ _id }
        open={ open }
        onEnter={ onEnter }
        onClose={ _onClose }
        anchorEl={ anchorEl }
        anchorOrigin={{
          vertical: placement === 'top' ? 'top' : 'bottom',
          horizontal: placement === 'right' ? 'right' : 'left',
        }}
        transformOrigin={{
          vertical: placement === 'top' ? 'bottom' : 'top',
          horizontal: placement === 'right' ? 'right' : 'left',
        }}
        className={ classNames }
        classes={{
          paper: classes.paper,
        }}>
        { content }
      </Popover>
    </>
  );
}
MaterialPopover.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * Popover identifier
   */
  id: PropTypes.string,
  /**
   * Popover position
   */
  placement: PropTypes.oneOf(['top', 'bottom', 'right', 'left']),
  /**
   * Popover header
   */
  title: PropTypes.string,
  /**
   * Popover level / css / class
   */
  level: PropTypes.oneOf(['default', 'warning']),
  /**
   * Popover value / text
   */
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Popover value / text - alias to value
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Specify which action or actions trigger popover visibility.
   *
   * - hover has higher priority
   */
  trigger: PropTypes.arrayOf(PropTypes.oneOf(['click', 'hover'])),
  /**
   * Specify whether the overlay should trigger onHide when the user clicks outside the overlay
   */
  rootClose: PropTypes.bool
};
MaterialPopover.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  level: 'default',
  placement: 'bottom',
  trigger: 'hover',
  rootClose: true
};

/**
 * Overlay with popover.
 *
 * @author Radek Tomiška
 */
export default class BasicPopover extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      open: undefined
    };
  }

  /**
   * Close / hide popover
   */
  close() {
    this.setState({
      open: false
    });
  }

  open() {
    this.setState({
      open: true
    });
  }

  /**
   * Close / hide popover
   */
  hide() {
    this.close();
  }

  onClick(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }

  render() {
    const {
      id,
      level,
      rendered,
      children,
      value,
      text,
      title,
      placement,
      showLoading,
      trigger,
      className,
      icon,
      rootClose,
      onEnter,
      onExit } = this.props;
    const { open } = this.state;
    //
    if (!rendered || (!children)) {
      return null;
    }
    //
    return (
      <MaterialPopover
        open={ open }
        onOpen={ this.open.bind(this) }
        onClose={ this.close.bind(this) }
        id={ id }
        level={ level }
        rendered={ rendered }
        value={ value }
        text={ text }
        title={ title }
        placement={ placement }
        showLoading={ showLoading }
        trigger={ trigger }
        className={ className }
        icon={ icon }
        rootClose={ rootClose }
        onEnter={ onEnter }
        onExit={ onExit }>
        { children }
      </MaterialPopover>
    );
  }
}

BasicPopover.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * Popover identifier
   */
  id: PropTypes.string,
  /**
   * Popover level / css / class
   */
  level: PropTypes.oneOf(['default', 'warning']),
  /**
   * Popover position
   */
  placement: PropTypes.oneOf(['top', 'bottom', 'right', 'left']),
  /**
   * Popover value / text
   */
  title: PropTypes.string,
  /**
   * Popover value / text
   */
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Popover value / text - alias to value
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  /**
   * Specify which action or actions trigger popover visibility
   */
  trigger: PropTypes.oneOf(['click', 'hover']),
  /**
   * Specify whether the overlay should trigger onHide when the user clicks outside the overlay
   */
  rootClose: PropTypes.bool
};

BasicPopover.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  level: 'default',
  placement: 'bottom',
  trigger: 'hover',
  rootClose: true
};

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
//
import Dialog from '@material-ui/core/Dialog';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';
import BasicModalHeader from './ModalHeader';
import BasicModalBody from './ModalBody';
import BasicModalFooter from './ModalFooter';

const SUPPORTED_SIZES = ['lg', 'large', 'sm', 'small'];

/**
 * Wrapped material-ui dialog.
 *
 * TODO: enforceFocus - select boxes are in body ...
 * FIXME: onEnter - Use the TransitionProps prop instead.
 *
 * @author Radek Tomiška
 */
export default function BasicModal(props) {
  const {
    rendered,
    bsSize,
    showLoading,
    affixFooter,
    onEnter,
    onExit,
    onHide,
    show,
    keyboard,
    fullWidth,
    children
  } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  let _maxWidth = bsSize;
  if (bsSize === 'large') {
    _maxWidth = 'lg';
  } else if (bsSize === 'small') {
    _maxWidth = 'sm';
  } else if (!_maxWidth || _maxWidth === 'default') {
    _maxWidth = 'md';
  }
  //
  const _onExit = (event, reason) => {
    if (onExit) {
      onExit();
    }
    if (onHide && (reason !== 'backdropClick' || keyboard)) {
      onHide();
    }
  };
  let hasHeader = false;
  let hasBody = false;
  //
  const _children = React.Children.map(children, child => {
    if (React.isValidElement(child)) {
      // scroll paper will be available, only if header and body is in child elements
      if (!hasHeader && child.type && child.type.__ModalHeader__) {
        hasHeader = true;
      }
      if (!hasBody && child.type && child.type.__ModalBody__) {
        hasBody = true;
      }
      return React.cloneElement(child, { onClose: _onExit });
    }
    return child;
  });
  //
  return (
    <Dialog
      open={ show }
      onEnter={ onEnter }
      onClose={ _onExit }
      scroll={ affixFooter && hasHeader && hasBody ? 'paper' : 'body' }
      className={ affixFooter ? 'basic-modal-scroll-paper' : 'basic-modal-scroll-body' }
      maxWidth={ _maxWidth }
      fullWidth={ fullWidth }
      aria-labelledby="scroll-dialog-header"
      aria-describedby="scroll-dialog-desc"
      disableEscapeKeyDown={ !keyboard }>
      {
        showLoading
        ?
        <BasicModalBody>
          <Loading isStatic showLoading/>
        </BasicModalBody>
        :
        _children
      }
    </Dialog>
  );
}

BasicModal.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Callback fired before the Modal transitions in
   */
  onEnter: PropTypes.func,
  /**
   * Callback fired after the Modal transitions out
   */
  onExit: PropTypes.func,
  /**
   * Component size variations.
   */
  bsSize: PropTypes.oneOf(_.concat(SUPPORTED_SIZES, 'default')),
  /**
   * Footer (~with buttons) will be affixed on the bottom.
   */
  affixFooter: PropTypes.bool
  /**
   * ... and other react bootstap modal props
   */
};

BasicModal.defaultProps = {
  ...AbstractComponent.defaultProps,
  bsSize: 'default',
  enforceFocus: false,
  affixFooter: true,
  keyboard: true,
  fullWidth: true
};

BasicModal.Header = BasicModalHeader;
BasicModal.Body = BasicModalBody;
BasicModal.Footer = BasicModalFooter;

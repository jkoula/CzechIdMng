import React from 'react';
//
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

const useStyles = makeStyles((theme) => ({
  desc: {
    marginBottom: theme.spacing(0),
  }
}));

/**
 * Wrapped material-ui dialog body.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function BasicModalBody(props) {
  const { className, style, children } = props;
  const classes = useStyles();
  //
  return (
    <DialogContent
      style={ style }
      className={ className }
      dividers>
      <DialogContentText
        id="scroll-dialog-desc"
        className={ classes.desc }
        tabIndex={ -1 }>
        { children }
      </DialogContentText>
    </DialogContent>
  );
}

BasicModalBody.propTypes = {
  ...AbstractComponent.propTypes,
};

BasicModalBody.defaultProps = {
  ...AbstractComponent.defaultProps,
  noPadding: false
};

BasicModalBody.__ModalBody__ = true;

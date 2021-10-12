import React from 'react';
//
import DialogActions from '@material-ui/core/DialogActions';

/**
 * Wrapped material-ui dialog actions.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function BasicModalFooter(props) {
  const { children } = props;
  //
  return (
    <DialogActions>
      { children }
    </DialogActions>
  );
}

BasicModalFooter.__ModalFooter__ = true;

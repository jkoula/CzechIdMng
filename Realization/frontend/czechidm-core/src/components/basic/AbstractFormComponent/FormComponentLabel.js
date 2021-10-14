import React from 'react';
import classNames from 'classnames';
import _ from 'lodash';
//
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';

/**
 * Form component label decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => {
  return {
    root: {
      color: theme.palette.text.secondary,
      backgroundColor: theme.palette.background.paper,
      marginLeft: -5,
      padding: '0 5px',
      fontSize: '1rem',
      transform: 'translate(14px, -6px) scale(0.75)',
      zIndex: 5,
      pointerEvents: 'none',
      transformOrigin: 'top left',
      top: 0,
      left: 0,
      position: 'absolute',
      display: 'block',
      fontWeight: 400,
      lineHeight: 1,
      letterSpacing: '0.00938em',
      borderRadius: 3
    },
    disabled: {
      color: theme.palette.text.disabled
    }
  };
});

export default function FormComponentLabel(props) {
  const { rendered, className, label, helpIcon, readOnly } = props;
  const classes = useStyles();
  //
  if (!rendered || !label || (_.isArray(label) && label.length === 0)) {
    return null;
  }
  //
  return (
    <label
      className={ classNames(classes.root, readOnly ? classes.disabled : null, className) }>
      { label }
      { helpIcon }
    </label>
  );
}

FormComponentLabel.propTypes = {
  ...AbstractContextComponent.propTypes
};

FormComponentLabel.defaultProps = {
  ...AbstractContextComponent.defaultProps,
};

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
      backgroundColor: theme.palette.background.paper
    }
  };
});

export default function FormComponentLabel(props) {
  const { rendered, className, label, helpIcon } = props;
  const classes = useStyles();
  //
  if (!rendered || !label || (_.isArray(label) && label.length === 0)) {
    return null;
  }
  //
  return (
    <label
      className={ classNames(classes.root, className) }>
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

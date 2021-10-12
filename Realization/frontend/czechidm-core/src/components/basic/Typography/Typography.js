import React from 'react';
import Typography from '@material-ui/core/Typography';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Typography decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function BasicTypography(props) {
  const { rendered, ...others } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  return (
    <Typography { ...others }/>
  );
}

BasicTypography.propTypes = {
  ...AbstractComponent.propTypes,
};

BasicTypography.defaultProps = {
  ...AbstractComponent.defaultProps
};

import React from 'react';
import PropTypes from 'prop-types';
import Collapse from '@material-ui/core/Collapse';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

/**
 * Colappse panel.
 *
 * @author Radek Tomiška
 */
export default function BasicCollapse(props) {
  const { rendered, showLoading, children, ...others } = props;
  if (!rendered) {
    return null;
  }
  //
  return (
    <Collapse in={ others.in } timeout={ 300 }>
      {
        showLoading
        ?
        <Loading isStatic showLoading/>
        :
        children
      }
    </Collapse>
  );
}

BasicCollapse.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * If collapse children is shown
   */
  in: PropTypes.bool,

  /**
   * ... and other react bootstap collapse props
   */
};

BasicCollapse.defaultProps = {
  ...AbstractComponent.defaultProps,
  in: false
};

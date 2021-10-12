import React from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Div from '../Div/Div';

/**
 * Basic panel decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function PanelBody(props) {
  const { className, rendered, showLoading, style, children } = props;
  if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
    return null;
  }
  const classNames = classnames(
    'panel-body',
    className
  );
  //
  return (
    <Div className={ classNames } style={ style } showLoading={ showLoading }>
      { children }
    </Div>
  );
}

PanelBody.propTypes = {
  ...AbstractComponent.propTypes
};
PanelBody.defaultProps = {
  ...AbstractComponent.defaultProps
};

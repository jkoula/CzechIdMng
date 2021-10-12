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
export default function PanelFooter(props) {
  const { rendered, className, showLoading, style, children } = props;
  if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
    return null;
  }
  const classNames = classnames(
    'panel-footer',
    className
  );

  return (
    <Div className={ classNames } style={ style } showLoading={ showLoading } showAnimation={ false }>
      { children }
    </Div>
  );
}

PanelFooter.propTypes = {
  ...AbstractComponent.propTypes
};
PanelFooter.defaultProps = {
  ...AbstractComponent.defaultProps
};

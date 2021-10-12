import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Div from '../Div/Div';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';

/**
 * Basic panel decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function PanelHeader(props) {
  const { className, rendered, showLoading, text, help, children, style, icon, buttons } = props;
  if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
    return null;
  }
  const classNames = classnames(
    'panel-heading',
    className
  );

  return (
    <Div className={ classNames } style={{ display: 'flex', alignItems: 'center', ...style }}>
      <Icon value={ showLoading ? 'fa:refresh' : icon } showLoading={ showLoading } style={{ marginRight: 5 }}/>
      <Div style={{ flex: 1 }}>
        {
          showLoading
          ||
          text
          ?
          <h2>{ text }</h2>
          :
          null
        }
        { children }
      </Div>
      {
        !buttons
        ||
        <Div>
          { buttons }
        </Div>
      }
      <HelpIcon content={ help }/>
    </Div>
  );
}

PanelHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Header text
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * link to help
   */
  help: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object
  ])
};
PanelHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

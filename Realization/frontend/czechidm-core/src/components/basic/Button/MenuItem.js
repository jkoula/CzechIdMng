import React from 'react';
//
import MenuItem from '@material-ui/core/MenuItem';
//
import Icon from '../Icon/Icon';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Split button item.
 *
 * @author Radek Tomiška
 */
export default function ButtonMenuItem(props) {
  const {
    rendered,
    eventKey,
    onClick,
    children,
    disabled,
    icon
  } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  return (
    <MenuItem
      onClick={ onClick }
      eventKey={ eventKey }
      disabled={ disabled }>
      <span>
        <Icon
          value={ icon }
          className="icon-left"
          style={ (children && React.Children.count(children) > 0) ? { marginRight: 5, width: 20, display: 'inline-block' } : {} }/>
        { children }
      </span>
    </MenuItem>
  );
}

ButtonMenuItem.propTypes = {
  ...AbstractComponent.propTypes
};
ButtonMenuItem.defaultProps = {
  ...AbstractComponent.defaultProps
};

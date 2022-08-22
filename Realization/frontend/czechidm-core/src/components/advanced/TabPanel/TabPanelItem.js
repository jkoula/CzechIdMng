import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Tab panel item.
 *
 * @author Radek Tomiška
 */
export default function TabPanelItem(props) {
  const { rendered, className, to, active, icon, iconColor, showLoading, title, children } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  const itemClassNames = classnames(
    className,
    { 'list-group-item': false },
    { active: active === true }
  );
  // icon resolving
  let iconContent = null;
  let _icon = (icon === undefined || icon === null) ? 'fa:circle-o' : icon;
  if (showLoading) {
    _icon = 'refresh';
  }
  if (_icon) {
    iconContent = (
      <Basic.Icon icon={ _icon } color={ iconColor } showLoading={ showLoading }/>

    );
  }
  //
  return (
    <li className={ itemClassNames } title={ title }>
      <Link to={ to } className={ classnames({ 'basic-item-active': active === true }) }>
        { iconContent }
        { children }
      </Link>
    </li>
  );
}

TabPanelItem.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  to: PropTypes.string,
  title: PropTypes.string,
  active: PropTypes.bool
};

TabPanelItem.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  to: null,
  title: PropTypes.string,
  active: PropTypes.bool
};

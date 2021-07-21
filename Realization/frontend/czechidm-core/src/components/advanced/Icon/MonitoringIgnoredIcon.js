import React from 'react';
import { faBellSlash } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Monitoring ignored icon.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class MonitoringIgnoredIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faBellSlash } />
      </span>
    );
  }
}

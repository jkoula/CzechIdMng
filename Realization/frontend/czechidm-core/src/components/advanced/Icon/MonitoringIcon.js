import React from 'react';
import classnames from 'classnames';
import { faBell } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Monitoring icon.
 *
 * FIXME: use badge
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringIcon extends AbstractIcon {

  renderIcon() {
    const { counter, level } = this.props;
    //
    return (
      <span className={ this.getClassName('fa-layers fa-fw') } style={ counter ? { fontSize: '1em' } : {} }>
        <FontAwesomeIcon icon={ faBell } transform={ counter ? 'down-2' : null } />
        {
          !counter
          ||
          <span
            className={
              classnames(
                'fa-layers-counter',
                `icon-${ level }`
              )
            }
            style={{
              fontSize: '2em',
              top: -4,
              right: -7
            }}>
            { counter }
          </span>
        }
      </span>
    );
  }
}

MonitoringIcon.defaultProps = {
  ...AbstractIcon.defaultProps,
  level: 'default'
};

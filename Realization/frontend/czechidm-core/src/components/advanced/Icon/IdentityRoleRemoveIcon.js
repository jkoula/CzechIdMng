import React from 'react';
import { faKey, faMinusSquare } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';
import Icon from '../../basic/Icon/Icon';

/**
 * Remove assigned identity role.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
export default class IdentityRoleRemoveIcon extends AbstractIcon {

  renderIcon() {
    const { disabled } = this.props;
    //
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faKey }/>
        <Icon
          level={ disabled ? 'default' : 'danger' }
          icon={
            <FontAwesomeIcon icon={ faMinusSquare } transform="down-5 right-8 shrink-6"/>
          }/>
      </span>
    );
  }
}

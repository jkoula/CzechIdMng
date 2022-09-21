import React from 'react';
//
import { Advanced } from 'czechidm-core';
import AccountOptionDecorator from './AccountOptionDecorator';
import AccountValueDecorator from './AccountValueDecorator';

/**
* Component for select accounts.
*
* @author Peter Štrunc
* @since 10.1.0
*/
export default class AccountSelect extends Advanced.EntitySelectBox {

  render() {
    const { rendered, entityType, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (entityType && entityType !== 'account') {
      LOGGER.warn(`IdentitySelect supports identity entity type only, given [${ entityType }] type will be ignored.`);
    }
    //
    return (
      <Advanced.EntitySelectBox
        ref="selectComponent"
        entityType="account"
        { ...others }/>
    );
  }
}

AccountSelect.propTypes = {
  ...Advanced.EntitySelectBox.propTypes
};
AccountSelect.defaultProps = {
  ...Advanced.EntitySelectBox.defaultProps,
  optionComponent: AccountOptionDecorator,
  valueComponent: AccountValueDecorator
};

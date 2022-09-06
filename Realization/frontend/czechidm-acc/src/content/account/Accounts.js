import React from 'react';
import { connect } from 'react-redux';

import { Basic, Domain } from 'czechidm-core';
import { AccountManager } from '../../redux';
import AccountTable from './AccountTable';
import AccountTypeEnum from '../../domain/AccountTypeEnum';

/**
 * List of accounts.
 *
 * @author Tomáš Doischer
 */
class Accounts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.accountManager = new AccountManager();
  }

  getContentKey() {
    return 'acc:content.accounts.all';
  }

  getNavigationKey() {
    return 'accounts-all';
  }

  render() {
    const defaultSearchParameters = new Domain.SearchParameters();
    
    return (
      <Basic.Div>
        <Basic.Panel>
          <AccountTable
            history={ this.context.history }
            uiKey="account-table"
            accountManager={ this.accountManager }
            filterOpened = { true }
            forceSearchParameters={ defaultSearchParameters }
            showRowSelection />
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

Accounts.propTypes = {
};
Accounts.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(Accounts);

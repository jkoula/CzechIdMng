import React from 'react';
//
import { Basic, Advanced } from 'czechidm-core';

/**
 * Default content (routes diff) for accounts.
 *
 * @author Tomáš Doischer
 */
export default class AccountRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.accounts';
  }

  getNavigationKey() {
    return 'accounts';
  }

  selectNavigationItem() {
    // nothing
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader({ header: this.i18n('acc:content.accounts.header') }) }

        <Advanced.TabPanel position="top" parentId="accounts" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

AccountRoutes.propTypes = {
};
AccountRoutes.defaultProps = {
};

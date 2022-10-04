import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import EntityAuditTable from 'czechidm-core/src/content/audit/EntityAuditTable';
import { Basic, Domain } from 'czechidm-core';
import AccountManager from '../../redux/AccountManager';
//
const manager = new AccountManager();

/**
* Account detail
*
* @author Roman Kucera
*/
class AccountAudit extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.accounts.detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
    this.selectNavigationItems(['sys-systems-main-menu', 'accounts', 'account-audit']);
  }

  getNavigationKey() {
    return 'account-audit';
  }

  render() {
    const { entity } = this.props; // ~ codeable support
    //
    if (!entity) {
      return (
        <Basic.Loading isStatic show/>
      );
    }
    //
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('withVersion', true)
      .setFilter('relatedOwnerId', entity.id);
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <EntityAuditTable
          uiKey={ `account-audit-table-${ entity.id }` }
          forceSearchParameters={ forceSearchParameters }/>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
  };
}

export default connect(select)(AccountAudit);

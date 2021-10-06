import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import AuditIdentityTable from './AuditIdentityTable';

/**
 * Audit for identities (under audit main menu).
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class AuditContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-identities';
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('title-identities') } />
        <AuditIdentityTable uiKey="audit-table-identities" />
      </div>
    );
  }
}

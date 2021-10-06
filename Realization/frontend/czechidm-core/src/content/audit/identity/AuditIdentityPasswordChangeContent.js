import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import AuditIdentityPasswordChangeTable from './AuditIdentityPasswordChangeTable';

/**
 * Audit for identity password change.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class AuditIdentityPasswordChangeContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-identity-password-change';
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('title-identity-password-change') } />
        <AuditIdentityPasswordChangeTable uiKey="audit-table-password-change-identities" />
      </div>
    );
  }
}

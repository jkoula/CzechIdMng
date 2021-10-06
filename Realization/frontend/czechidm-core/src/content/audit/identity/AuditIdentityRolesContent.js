import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import AuditIdentityRolesTable from './AuditIdentityRolesTable';

/**
 * Audit for identity roles.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class AuditIdentityRolesContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-identity-roles';
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('title-identity-roles') } />
        <AuditIdentityRolesTable uiKey="audit-table-role-identities" />
      </div>
    );
  }
}

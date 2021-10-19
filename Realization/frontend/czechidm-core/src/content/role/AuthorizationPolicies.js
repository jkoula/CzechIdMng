import React from 'react';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import AuthorizationPolicyTable from './AuthorizationPolicyTable';

/**
 * COnfigured authorization policies
 *
 * @author Radek Tomiška
 */
export default class AuthorizationPolicies extends Basic.AbstractContent {

  getContentKey() {
    return 'content.role.authorization-policies';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-authorization-policies', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader() }

        <Basic.Panel className="no-border last">
          <AuthorizationPolicyTable
            uiKey="role-authorization-policies-table"
            forceSearchParameters={ forceSearchParameters }
            match={ this.props.match }
            className="no-margin"
            columns={ ['authorizableType', 'basePermissions', 'evaluatorType', 'evaluatorProperties', 'description', 'disabled', 'seq'] }/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

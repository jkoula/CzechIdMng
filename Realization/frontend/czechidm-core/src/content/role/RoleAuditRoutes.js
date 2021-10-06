import React from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Role audit routes.
 *
 * @author Radek Tomiška
 * @since 11.3.0
 */
export default class RoleAuditRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  render() {
    return (
      <div style={{ paddingTop: 15 }}>
        <Advanced.TabPanel
          position="top"
          parentId="role-audit"
          match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </div>
    );
  }
}

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import EntityAuditTable from '../audit/EntityAuditTable';
import SearchParameters from '../../domain/SearchParameters';
import { RoleManager } from '../../redux/data';

const roleManager = new RoleManager();

/**
 * Role audit tab.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
class RoleAudit extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(roleManager.fetchEntityIfNeeded(entityId));
  }

  getNavigationKey() {
    return 'role-audit-detail';
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
    const forceSearchParameters = new SearchParameters()
      .setFilter('withVersion', true)
      .setFilter('relatedOwnerId', entity.id);
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <EntityAuditTable
          uiKey={ `role-audit-table-${ entity.id }` }
          forceSearchParameters={ forceSearchParameters }/>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: roleManager.getEntity(state, entityId)
  };
}

export default connect(select)(RoleAudit);

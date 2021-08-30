import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced } from 'czechidm-core';
import { SystemGroupManager } from '../../redux';
//
const manager = new SystemGroupManager();

/**
 * System group (cross-domain)
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
class SystemGroup extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="fa:layer-group"
          entity={ entity }
          showLoading={ !entity && showLoading }
          back="/system-groups">
          { manager.getNiceLabel(entity) } <small> { this.i18n('acc:content.systemGroup.edit.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="system-groups" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

SystemGroup.propTypes = {
};
SystemGroup.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(SystemGroup);

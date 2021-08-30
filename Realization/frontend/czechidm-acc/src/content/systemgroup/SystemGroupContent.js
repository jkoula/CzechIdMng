import React from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import SystemGroupDetail from './SystemGroupDetail';
import { SystemGroupManager } from '../../redux';

const manager = new SystemGroupManager();

/**
 * System group content
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
class SystemGroupContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};

    const { entityId } = this.props.match.params;

    if (this._isNew()) {
      this.context.store.dispatch(manager.receiveEntity(entityId, {type: 'CROSS_DOMAIN'}));
    } else {
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (entity, error) => {
        this.handleError(error);
      }));
    }
  }

  getContentKey() {
    return 'acc:content.SystemGroup';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('system-groups-detail', this.props.match.params);
  }

  componentDidMount() {
    super.componentDidMount();
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading, permissions } = this.props;
    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            !entity
            ||
            <SystemGroupDetail
              entity={entity}
              showLoading={showLoading}
              manager={ manager }
              permissions={ permissions }
              match={ this.props.match }/>
          }
        </div>
      </Basic.Row>
    );
  }
}

SystemGroupContent.propTypes = {
};

SystemGroupContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    permissions: manager.getPermissions(state, null, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(SystemGroupContent);

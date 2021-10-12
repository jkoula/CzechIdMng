import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import { SynchronizationLogManager } from '../../redux';

const manager = new SynchronizationLogManager();

/**
 * Sync log basic information (info card).
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export class SyncLogInfo extends Advanced.AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] })) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const entity = this.getEntity();
    if (entity
      && entity._embedded
      && entity._embedded.synchronizationConfig
      && entity._embedded.synchronizationConfig._embedded
      && entity._embedded.synchronizationConfig._embedded.systemMapping
      && entity._embedded.synchronizationConfig._embedded.systemMapping._embedded
      && entity._embedded.synchronizationConfig._embedded.systemMapping._embedded.objectClass) {
      const systemId = entity._embedded.synchronizationConfig._embedded.systemMapping._embedded.objectClass.system;
      return `/system/${ encodeURIComponent(systemId) }/synchronization-logs/${ encodeURIComponent(entity.id) }/detail`;
    }
    return null;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fas:exchange-alt';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.SynchronizationLog._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    //
    return [
      {
        label: this.i18n('acc:entity.SynchronizationConfig._type'),
        value: !entity._embedded ||
          <Advanced.EntityInfo
            entityType="syncConfig"
            entity={ entity._embedded.synchronizationConfig }
            entityIdentifier={ entity.synchronizationConfig }
            face="link" />
      },
      {
        label: this.i18n('acc:entity.SynchronizationLog.started'),
        value: (<Advanced.DateValue value={ entity.started } showTime/>)
      }
    ];
  }
}

SyncLogInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority.
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically.
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool
};
SyncLogInfo.defaultProps = {
  ...Advanced.AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(SyncLogInfo);

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { MonitoringManager, DataManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import LongRunningTaskName from '../LongRunningTask/LongRunningTaskName';

const manager = new MonitoringManager();

/**
 * Monitoring evaluator basic information (info card).
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export class MonitoringInfo extends AbstractEntityInfo {

  componentDidMount() {
    super.componentDidMount();
    //
    if (SecurityManager.hasAuthority('MONITORING_READ')) {
      this.context.store.dispatch(manager.fetchSupportedEvaluators());
    }
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  getNiceLabel(entity) {
    const { supportedEvaluators } = this.props;
    const _entity = entity || this.getEntity();
    //
    let value = this.getManager().getNiceLabel(_entity, supportedEvaluators, false);
    if (value.length > 60) {
      value = `${ value.substr(0, 60) }...`;
    }
    if (!value) {
      return this.i18n('entity.Monitoring._type');
    }

    return value;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Monitoring._type');
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:monitoring';
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const entity = this.getEntity();
    //
    return `/monitoring/monitorings/${ encodeURIComponent(entity.id) }`;
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
    const { supportedEvaluators } = this.props;
    //
    const content = [
      {
        label: this.i18n('entity.Monitoring.evaluatorType.label'),
        value: (
          <LongRunningTaskName entity={ entity } supportedTasks={ supportedEvaluators }/>
        )
      },
      {
        label: this.i18n('entity.Monitoring.instanceId.label'),
        value: entity.instanceId
      },
      {
        label: this.i18n('entity.Monitoring.checkPeriod.label'),
        value: `${ entity.checkPeriod }s`
      }
    ];
    //
    if (entity.description) {
      content.push(
        {
          label: this.i18n('entity.Monitoring.description.label'),
          value: entity.description
        }
      );
    }
    //
    return content;
  }
}

MonitoringInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
MonitoringInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const { entityIdentifier, entity } = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  //
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    _permissions: manager.getPermissions(state, null, entityId),
    supportedEvaluators: DataManager.getData(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
  };
}
export default connect(select)(MonitoringInfo);

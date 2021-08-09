import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { MonitoringManager } from '../../redux';
import MonitoringDetail from './MonitoringDetail';
//
const manager = new MonitoringManager();

/**
 * Monitoring evaluator detail tabs.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
class MonitoringDetailRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.monitorings';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${ entityId }]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    return !!Utils.Ui.getUrlParameter(this.props.location, 'new');
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        {
          this._getIsNew()
          ?
          <Helmet title={ this.i18n('create.header') } />
          :
          <Helmet title={ this.i18n('edit.title') } />
        }
        {
          (this._getIsNew() || !entity)
          ||
          <Advanced.DetailHeader
            entity={ entity }
            icon="component:monitoring"
            showLoading={ showLoading }
            back="/monitoring/monitorings">
            { this.i18n('edit.header', { escape: false, name: manager.getNiceLabel(entity) }) }
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <MonitoringDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="monitorings" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }

      </Basic.Div>
    );
  }
}

MonitoringDetailRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
MonitoringDetailRoutes.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(MonitoringDetailRoutes);

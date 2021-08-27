import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import ComponentService from '../../../services/ComponentService';
import {
  ConfigurationManager,
  SecurityManager,
  MonitoringResultManager,
  LongPollingManager
} from '../../../redux';
import NavigationItem from './NavigationItem';
import NavigationSeparator from './NavigationSeparator';
//
const monitoringResultManager = new MonitoringResultManager();
const componentService = new ComponentService();

/**
 * Monitoring icon in navigation.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
class NavigationMonitoring extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      longPollingInprogress: false,
      requestControllers: [] // Contains array of requests controllers for abort a request.
    };
    this.canSendLongPollingRequest = false;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (SecurityManager.hasAuthority('MONITORINGRESULT_READ')) { // show monitoring icon in navigation
      this._fetchLastMonitoringResults(() => {
        if (!this.state.longPollingInprogress && this._isLongPollingEnabled()) {
          // Long-polling request can be send.
          this.setState({
            longPollingInprogress: true
          }, () => {
            this.canSendLongPollingRequest = true;
            this._sendLongPollingRequest();
          });
        }
      });
    }
  }

  componentWillUnmount() {
    super.componentWillUnmount();
    //
    const { requestControllers } = this.state;
    // Stop request of check requests (next long-polling request will be not created).
    this.canSendLongPollingRequest = false;
    // Send abort signal to all registered requests in this component.
    requestControllers.forEach(controller => {
      controller.abort();
    });
  }

  getComponentKey() {
    return 'component.advanced.NavigationMonitoring';
  }

  _fetchLastMonitoringResults(cb = null) {
    this.context.store.dispatch(monitoringResultManager.fetchLastMonitoringResults(cb));
  }

  _sendLongPollingRequest() {
    LongPollingManager.sendLongPollingRequest.bind(this, 'mockId', monitoringResultManager.getService())();
  }

  _isLongPollingEnabled() {
    const { _longPollingEnabled } = this.props;
    const hasAuthority = SecurityManager.hasAuthority('MONITORINGRESULT_READ');
    return _longPollingEnabled && hasAuthority;
  }

  _refreshAll() {
    this._fetchLastMonitoringResults();
  }

  render() {
    const {
      rendered,
      lastMonitoringResults,
      location,
      _lastMonitoringResultTotal
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let counter = null;
    if (_lastMonitoringResultTotal) {
      counter = _lastMonitoringResultTotal;
    } else if (lastMonitoringResults) {
      counter = lastMonitoringResults.length;
    }
    //
    return (
      <li>
        <a
          href="#"
          className="dropdown-toggle"
          data-toggle="dropdown"
          role="button"
          aria-haspopup="true"
          aria-expanded="false">
          <span>
            <Basic.Icon
              value="component:monitoring-results"
              counter={ counter }
              level={ (lastMonitoringResults && lastMonitoringResults.find(result => result.level === 'ERROR')) ? 'error' : 'warning'}/>
          </span>
        </a>
        <ul className="dropdown-menu">
          <li className="monitoring-result">
            {
              !lastMonitoringResults || lastMonitoringResults.length === 0
              ?
              <Basic.Alert
                level="info"
                text={ this.i18n('noData') }/>
              :
              lastMonitoringResults.map(result => {
                if (!result.result || !result.result.model) {
                  // just for sure
                  return null;
                }
                // manual level has higher priority
                if (result.level) {
                  result.result.model.level = result.level;
                }
                const message = this.getFlashManager().convertFromResultModel(result.result.model);
                const monitoringResultButton = componentService.getMonitoringResultButtonComponent(Utils.Ui.getSimpleJavaType(result.evaluatorType));
                //
                return (
                  <Basic.FlashMessage
                    className="monitoring-result-message"
                    message={ message }
                    buttons={
                      monitoringResultButton
                      ?
                      [
                        <monitoringResultButton.component
                          monitoringResult={ result }
                          buttonSize="xs"/>
                      ]
                      :
                      [
                        <Basic.Button
                          icon="fa:angle-double-right"
                          level={ result.result.model.level }
                          buttonSize="xs"
                          text={ this.i18n('component.advanced.EntityInfo.link.detail.label') }
                          onClick={ () => {
                            this.context.history.push(`/monitoring/monitoring-results/${ result.id }`);
                          }}
                          rendered={
                            location && location.pathname !== `/monitoring/monitoring-results/${ result.id }`
                          }/>
                      ]
                    }/>
                );
              })
            }
          </li>
          <NavigationSeparator rendered={ location && location.pathname !== '/monitoring/monitoring-results' }/>
          <NavigationItem
            id="nav-item-monitoring-detail"
            key="nav-item-monitoring-detail"
            to="/monitoring/monitoring-results"
            titlePlacement="bottom"
            icon="fa:angle-double-right"
            text={ this.i18n('link.all.label') }
            rendered={ location && location.pathname !== '/monitoring/monitoring-results' }/>
        </ul>
      </li>
    );
  }
}

NavigationMonitoring.propTypes = {
  rendered: PropTypes.bool
};

NavigationMonitoring.defaultProps = {
  rendered: true
};

function select(state) {
  const ui = state.data.ui[MonitoringResultManager.UI_KEY_LAST_MONITORING_RESULTS];
  //
  return {
    lastMonitoringResults: monitoringResultManager.getEntities(state, MonitoringResultManager.UI_KEY_LAST_MONITORING_RESULTS),
    _longPollingEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.long-polling.enabled', true),
    _lastMonitoringResultTotal: ui ? ui.total : null,
  };
}

export default connect(select)(NavigationMonitoring);

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import Menu from '@material-ui/core/Menu';
import MenuList from '@material-ui/core/MenuList';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import NotificationsIcon from '@material-ui/icons/Notifications';
import Badge from '@material-ui/core/Badge';
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
      requestControllers: [], // Contains array of requests controllers for abort a request.
      anchorEl: null
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
    this.context.store.dispatch(monitoringResultManager.fetchLastMonitoringResults((json, error) => {
      if (error) {
        if (error.statusCode === 401 || error.statusCode === 403) {
          this.addErrorMessage(error, { hidden: true });
        } else {
          this.addError(error);
        }
      } else if (cb) {
        cb();
      }
    }));
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

  _handleClick(event) {
    this.setState({
      anchorEl: event.currentTarget
    });
  }

  _handleClose() {
    this.setState({
      anchorEl: null
    });
  }

  render() {
    const {
      rendered,
      lastMonitoringResults,
      location,
      _lastMonitoringResultTotal
    } = this.props;
    const { anchorEl } = this.state;
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
      <>
        <IconButton
          role="button"
          color="inherit"
          aria-controls="monitoring-menu"
          aria-haspopup="true"
          onClick={ (event) => {
            this._handleClick(event);
          }}>
          <Badge badgeContent={ counter} color="error">
            <NotificationsIcon/>
          </Badge>
        </IconButton>
        <Menu
          id="monitoring-menu"
          anchorEl={ anchorEl }
          keepMounted
          open={ Boolean(anchorEl) }
          onClose={ this._handleClose.bind(this) }>
          <div className="monitoring-result">
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
                          buttonSize="xs"
                          onClick={ () => {
                            this._handleClose();
                          }}/>
                      ]
                      :
                      [
                        <Basic.Button
                          icon="fa:angle-double-right"
                          level={ result.result.model.level }
                          buttonSize="xs"
                          text={ this.i18n('component.advanced.EntityInfo.link.detail.label') }
                          onClick={ () => {
                            this._handleClose();
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
          </div>
          {
            location && location.pathname !== '/monitoring/monitoring-results'
            ?
            <MenuList>
              <Divider />
              <Basic.MenuItem
                id="nav-item-monitoring-detail"
                key="nav-item-monitoring-detail"
                icon="fa:angle-double-right"
                onClick={ () => {
                  this._handleClose();
                  this.context.history.push('/monitoring/monitoring-results');
                }}
                titlePlacement="bottom">
                { this.i18n('link.all.label') }
              </Basic.MenuItem>
            </MenuList>
            :
            null
          }
        </Menu>
      </>
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

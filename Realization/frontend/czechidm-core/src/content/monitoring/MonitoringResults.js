import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import MonitoringResultTable from './MonitoringResultTable';
import { MonitoringResultManager } from '../../redux';
//
const manager = new MonitoringResultManager();

/**
 * Monitoring results.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringResults extends Basic.AbstractContent {

  getContentKey() {
    return 'content.monitoring-results';
  }

  getNavigationKey() {
    return 'monitoring-results';
  }

  getManager() {
    return manager;
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <MonitoringResultTable
          history={ this.context.history }
          location={ this.props.location }
          match={ this.props.match }
          uiKey="monitoring-result-table"
          filterOpened
          defaultSearchParameters={ this.getManager().getDefaultSearchParameters().setFilter('lastResult', true) }/>
      </Basic.Div>
    );
  }
}

import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import MonitoringTable from './MonitoringTable';

/**
 * Configured monitoring evaluators
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class Monitorings extends Basic.AbstractContent {

  getContentKey() {
    return 'content.monitorings';
  }

  getNavigationKey() {
    return 'monitorings';
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <MonitoringTable
          history={ this.context.history }
          location={ this.props.location }
          match={ this.props.match }
          uiKey="monitoring-table"
          filterOpened />
      </Basic.Div>
    );
  }
}

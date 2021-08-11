import React from 'react';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import MonitoringResultTable from './MonitoringResultTable';

/**
 * Monitoring evaluator results.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class MonitoringDetailResults extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.monitoring-results';
  }

  getNavigationKey() {
    return 'monitoring-detail-results';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('monitoring', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <MonitoringResultTable
          filterOpened={ false }
          history={ this.context.history }
          location={ this.props.location }
          columns={ ['result', 'created', 'evaluatorProperties', 'owner', 'value', 'instanceId'] }
          match={ this.props.match }
          uiKey={ `monitoring-detail-result-table-${ this.props.match.params.entityId }` }
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"/>
      </Basic.Div>
    );
  }
}

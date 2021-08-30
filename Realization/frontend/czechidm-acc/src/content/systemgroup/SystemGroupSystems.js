import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Domain } from 'czechidm-core';
import SystemGroupSystemTable from './SystemGroupSystemTable';
import { SystemGroupSystemManager } from '../../redux';

/**
 * Table of system groups-system relations.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroupSystems extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.manager = new SystemGroupSystemManager();
  }

  getContentKey() {
    return 'acc:content.systemGroupSystem';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('system-group-systems', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemGroupId', this.props.match.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('detail')} />

        <Basic.ContentHeader text={ this.i18n('detail') } style={{ marginBottom: 0 }}/>
        <SystemGroupSystemTable
          uiKey="system-group-systems-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          manager={ this.manager }
          match={ this.props.match }/>
      </div>
    );
  }
}

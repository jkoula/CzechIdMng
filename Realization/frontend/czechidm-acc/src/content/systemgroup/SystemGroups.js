import React from 'react';
//
import {Basic} from 'czechidm-core';
import {SystemGroupManager} from '../../redux';
import SystemGroupTable from './SystemGroupTable';

/**
 * Table of system groups.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroups extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new SystemGroupManager();
  }

  getContentKey() {
    return 'acc:content.systemGroup';
  }

  getNavigationKey() {
    return 'system-groups';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <SystemGroupTable uiKey="system-groups-table" manager={this.manager}/>
        </Basic.Panel>

      </div>
    );
  }
}

SystemGroups.propTypes = {
};
SystemGroups.defaultProps = {
};

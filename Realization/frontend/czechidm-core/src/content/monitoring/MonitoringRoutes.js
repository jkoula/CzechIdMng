import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Monitoring entry point.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.monitorings';
  }

  render() {
    return (
      <Basic.Div>
        {
          this.renderPageHeader({
            icon: 'component:monitoring',
            header: this.i18n('navigation.menu.monitoring.header'),
            title: this.i18n('navigation.menu.monitoring.title')
          })
        }

        <Advanced.TabPanel position="top" parentId="monitoring" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

import React from 'react';
import * as Basic from '../../../components/basic';
import { SmsManager } from '../../../redux';
import SmsTable from './SmsTable';

/**
 * List of sms logs.
 *
 * @author Peter Sourek
 * @author Radek Tomiška
 */
export default class Sms extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.manager = new SmsManager();
  }

  getContentKey() {
    return 'content.sms';
  }

  getNavigationKey() {
    return 'notification-sms';
  }

  render() {
    return (
      <Basic.Div>

        { this.renderPageHeader() }

        <Basic.Panel>
          <SmsTable uiKey="sms-table" manager={ this.manager } filterOpened/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

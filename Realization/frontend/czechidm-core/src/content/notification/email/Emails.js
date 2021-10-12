import React from 'react';
import * as Basic from '../../../components/basic';
import { EmailManager } from '../../../redux';
import EmailTable from './EmailTable';

/**
 * List of email in audit log.
 *
 * @author Radek Tomiška
 */
export default class Emails extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.emailManager = new EmailManager();
  }

  getContentKey() {
    return 'content.emails';
  }

  getNavigationKey() {
    return 'notification-emails';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <EmailTable uiKey="email_table" emailManager={ this.emailManager } filterOpened/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

Emails.propTypes = {
};
Emails.defaultProps = {
};

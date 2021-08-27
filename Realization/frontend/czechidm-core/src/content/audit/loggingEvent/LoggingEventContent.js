import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import LoggingEventTable from './LoggingEventTable';

/**
 * Content for logging event, contains logging event tables.
 *
 * @author Ondřej Kopr
 */

class LoggingEventContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit.logging-event';
  }

  getNavigationKey() {
    return 'audit-logging-events';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <LoggingEventTable uiKey="audit-event-table"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

LoggingEventContent.propTypes = {
};

LoggingEventContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(LoggingEventContent);

import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import AuditTable from './AuditTable';

/**
 * Audet contenr entry point.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class AuditContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-entities';
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <AuditTable uiKey="audit-table"/>
      </Basic.Div>
    );
  }
}

AuditContent.propTypes = {
};

AuditContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(AuditContent);

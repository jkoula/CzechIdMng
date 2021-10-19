import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { SecurityManager } from '../../redux';
//
const securityManager = new SecurityManager();

/**
 * Cas - process logout response.
 *
 * @author Radek Tomiška
 */
class CasLogoutResponse extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    const { query } = this.props.location;
    //
    let statusCode = null;
    if (query) {
      statusCode = query['status-code'];
    }
    //
    this.state = {
      statusCode: (statusCode || 'LOG_OUT_SUCCESS').toUpperCase()
    };
  }

  getContentKey() {
    return 'content.logout';
  }

  render() {
    const { statusCode } = this.state;
    const _level = statusCode === 'LOG_OUT_SUCCESS' ? 'success' : 'warning';
    //
    return (
      <Basic.Container component="main" maxWidth="md">
        <Basic.Alert
          level={ _level }
          title={ this.i18n(`error.${ statusCode }.title`) }
          text={ this.i18n(`error.${ statusCode }.message`) }
          buttons={[
            <Basic.Button
              level="success"
              style={{ marginRight: 5 }}
              onClick={ () => {
                this.context.history.push('/login');
              }}>
              { this.i18n('button.login.label') }
            </Basic.Button>,
            <Basic.Button
              level={ _level }
              rendered={ statusCode === 'CAS_LOGOUT_SERVER_NOT_AVAILABLE'}
              onClick={ () => {
                this.context.history.push('/logout');
              }}>
              { this.i18n('button.logout') }
            </Basic.Button>
          ]}/>
      </Basic.Container>
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    userContext: state.security.userContext,
    casEnabled: true
  };
}

export default connect(select)(CasLogoutResponse);

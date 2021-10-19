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
 * Cas - process login response.
 *
 * @author Radek Tomiška
 */
class CasLoginResponse extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    const { query } = this.props.location;
    //
    let token = null;
    let statusCode = null;
    if (query) {
      token = query.cidmst;
      statusCode = query['status-code'];
    }
    if (token) {
      // try remote login to getauthorities, profile, etc.
      context.store.dispatch(securityManager.receiveLogin(
        _.merge({}, props.userContext, {
          tokenCIDMST: token,
          isTryRemoteLogin: true,
          isAuthenticated: false
        }), () => {
          context.history.replace('/login');
        }
      ));
    } else {
      context.store.dispatch(securityManager.receiveLogin(
        _.merge({}, props.userContext, {
          isTryRemoteLogin: true,
          isAuthenticated: false,
          isExpired: false // ~ prevent to App redirect to login again => cycle with remote login
        })
      ));
    }
    //
    this.state = {
      showError: !token,
      statusCode: (statusCode || 'LOG_IN_FAILED').toUpperCase()
    };
  }

  getContentKey() {
    return 'content.login';
  }

  render() {
    const { showError, statusCode } = this.state;
    //
    if (!showError) {
      return (
        <div>
          <Helmet title={ this.i18n('title') } />
          <Basic.Loading className="global" showLoading />
        </div>
      );
    }
    //
    return (
      <Basic.Container component="main" maxWidth="md">
        <Basic.Alert
          level="warning"
          title={ this.i18n(`error.${ statusCode }.title`) }
          text={ this.i18n(`error.${ statusCode }.message`) }
          buttons={[
            <Basic.Button
              level="success"
              rendered={ statusCode !== 'CAS_TICKET_VALIDATION_FAILED' }
              style={{ marginRight: 5 }}
              onClick={ () => {
                this.context.history.push('/login');
              }}>
              { this.i18n('content.logout.button.login.label') }
            </Basic.Button>,
            <Basic.Button
              level="warning"
              style={{ marginRight: 5 }}
              rendered={ statusCode === 'CAS_TICKET_VALIDATION_FAILED' }
              title={ this.i18n('button.logout.title') }
              onClick={ () => {
                this.context.history.push('/logout');
              }}>
              { this.i18n('button.logout.value') }
            </Basic.Button>,
            <Basic.Button
              level="warning"
              rendered={ statusCode === 'CAS_LOGIN_SERVER_URL_NOT_CONFIGURED' || statusCode === 'CAS_LOGIN_SERVER_NOT_AVAILABLE' }
              onClick={ () => {
                this.context.history.push('/login?nocas=true');
              }}>
              { this.i18n('button.loginIdm.label') }
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

export default connect(select)(CasLoginResponse);

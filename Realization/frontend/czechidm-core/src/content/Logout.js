import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import { SecurityManager, ConfigurationManager } from '../redux';
import { RestApiService } from '../services';

const securityManager = new SecurityManager();

/**
 * Logout page.
 *
 * @author Radek Tomiška
 */
class Logout extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    const { casEnabled, showLogoutContent } = props;
    this.state = { showLogoutContent: false };
    //
    // logout immediately, when component will mount
    context.store.dispatch(securityManager.logout(() => {
      if (casEnabled) {
        window.location.replace(RestApiService.getUrl('/authentication/cas-logout-request'));
      } else if (showLogoutContent) {
        this.setState({ showLogoutContent: true });
      } else {
        context.history.replace('/login');
      }
    }));
  }

  getContentKey() {
    return 'content.logout';
  }

  render() {
    const { showLogoutContent } = this.state;
    //
    // stay on logout page
    if (showLogoutContent) {
      return (
        <Basic.Container component="main" maxWidth="md">
          <Basic.Alert
            level="success"
            title={ this.i18n(`error.LOG_OUT_SUCCESS.title`) }
            text={ this.i18n(`error.LOG_OUT_SUCCESS.message`) }
            buttons={[
              <Basic.Button
                level="success"
                onClick={ () => {
                  this.context.history.push('/login');
                }}>
                { this.i18n('button.login.label') }
              </Basic.Button>
            ]}/>
        </Basic.Container>
      );
    }
    // logout in process
    return (
      <Basic.Loading isStatic showLoading />
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    userContext: state.security.userContext,
    navigation: state.config.get('navigation'),
    casEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.cas.enabled', false),
    showLogoutContent: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.logout.content', false)
  };
}

export default connect(select)(Logout);

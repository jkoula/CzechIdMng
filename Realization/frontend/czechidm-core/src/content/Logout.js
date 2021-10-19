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
    const { casEnabled } = props;
    //
    // logout immediately, when component will mount
    context.store.dispatch(securityManager.logout(() => {
      if (casEnabled) {
        window.location.replace(RestApiService.getUrl('/authentication/cas-logout-request'));
      } else {
        context.history.replace('/login');
      }
    }));
  }

  render() {
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
    casEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.cas.enabled', false)
  };
}

export default connect(select)(Logout);

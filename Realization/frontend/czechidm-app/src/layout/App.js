import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import classNames from 'classnames';
import { Route } from 'react-router-dom';
import _ from 'lodash';
//
import { ThemeProvider } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import Dashboard from 'czechidm-core/src/content/Dashboard';
import Footer from './Footer';
//
const securityManager = new Managers.SecurityManager();

/**
 * Application entry point.
 *
 * @author Radek Tomiška
 */
export class App extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      token: null
    };
    context.history = props.history;
  }

  /**
  * Look out: This method is aplication entry point
  */
  componentDidUpdate() {
    const { location, userContext, appReady } = this.props;
    if (userContext.isExpired || (userContext.id == null && !userContext.isAuthenticated) && location.pathname !== '/login') {
      // preserve path before logout
      this.context.store.dispatch(securityManager.receiveLogin(
        _.merge({}, userContext, {
          loginTargetPath: location.pathname,
          isTryRemoteLogin: true
        }), () => {
          this.context.history.replace('/login');
        }
      ));
    }
    //
    // select navigation
    if (location.pathname === '/') {
      this.selectNavigationItem('home');
    }
    if (appReady) {
      this._handleTokenRefresh();
    }
  }

  /**
   * Creates react-router Routes components for this component (url).
   * And add Dashboard route.
   */
  generateRouteComponents() {
    const basicRoutes = super.generateRouteComponents();
    const mockRoute = {component: Dashboard, access: [{ type: 'IS_AUTHENTICATED' }]};
    return [<Route key="dashboard" exact path="/" component={this._getComponent(mockRoute)}/>, ...basicRoutes];
  }

  _handleTokenRefresh() {
    const { userContext } = this.props;
    // handle token expiration extension
    if (userContext) {
      this.context.store.dispatch(securityManager.checkRefreshedToken());
    }
  }

  render() {
    const { userContext, bulk, appReady, navigationCollapsed, hideFooter, location, theme } = this.props;
    const titleTemplate = `%s | ${ this.i18n('app.name') }`;
    const classnames = classNames(
      { 'with-sidebar': Managers.SecurityManager.isAuthenticated(userContext) },
      { collapsed: navigationCollapsed }
    );
    this.context.theme = theme;
    // @todo-upgrade-10 - FlashMessages throw warning "Function components cannot be given refs.
    // Attempts to access this ref will fail. Did you mean to use React.forwardRef()?"
    return (
      <ThemeProvider theme={ theme }>
        <CssBaseline/>
        <Basic.FlashMessages ref="messages" />
        <div id="content-wrapper">
          {
            !appReady
            ?
            <Basic.Loading className="global" showLoading />
            :
            <div>
              <Helmet title={ this.i18n('navigation.menu.home') } titleTemplate={ titleTemplate } />
              <Advanced.Navigation location={ location }>
                <div id="content-container" className={ classnames }>
                  { /* Childrens are hiden, when token expires =>
                    all components are loaded (componentDidMount) after identity is logged again */ }
                  { this.getRoutes() }
                  <Footer rendered={ !hideFooter } />
                  { /* @deprecated - remove, when all bulk actions will be moved to BE */ }
                  <Advanced.ModalProgressBar
                    show={ bulk.showLoading }
                    text={ bulk.action.title }
                    count={ bulk.size }
                    counter={ bulk.counter }
                  />
                </div>
              </Advanced.Navigation>
            </div>
          }
        </div>
      </ThemeProvider>
    );
  }
}

App.propTypes = {
  /**
   * Logged user context
   */
  userContext: PropTypes.object,
  /**
   * Globally bulk action
   */
  bulk: PropTypes.object,
  appReady: PropTypes.bool,
  i18nReady: PropTypes.string,
  navigationCollapsed: PropTypes.bool,
  /**
   * Footer will be hidden
   */
  hideFooter: PropTypes.bool
};

App.defaultProps = {
  userContext: null,
  bulk: { action: {} },
  appReady: false,
  i18nReady: null,
  navigationCollapsed: false,
  hideFooter: false
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    userContext: state.security.userContext,
    bulk: state.data.bulk,
    appReady: state.config.get('appReady'),
    i18nReady: state.config.get('i18nReady'),
    theme: Managers.ConfigurationManager.getApplicationTheme(state),
    navigationCollapsed: state.security.userContext.navigationCollapsed,
    hideFooter: state.config.get('hideFooter')
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(App);

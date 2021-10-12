import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import Joi from 'joi';
import { Link } from 'react-router-dom';
//
import * as Basic from '../components/basic';
import { SecurityManager, DataManager } from '../redux';
import {
  getNavigationItems,
} from '../redux/config/actions';
import { HelpContent } from '../domain';

const securityManager = new SecurityManager();
const dataManager = new DataManager();

/**
 * Login box.
 *
 * @author Radek Tomiška
 */
class Login extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showTwoFactor: false,
      token: null
    };
  }

  getContentKey() {
    return 'content.login';
  }

  getNavigationKey() {
    return 'home';
  }

  hideFooter() {
    return true;
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.userContext !== this.props.userContext) {
      if (nextProps.userContext.isAuthenticated) {
        this._redirectLoggedUser();
      }
    }
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.refs.form.setData({});
    //
    const { userContext } = this.props;
    if (!SecurityManager.isAuthenticated(userContext) && userContext.isTryRemoteLogin) {
      this.context.store.dispatch(securityManager.remoteLogin((result, error) => {
        if (error && error.statusEnum) {
          if (error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
            this.context.history.replace(`/password/change`);
          }
          if (error.statusEnum === 'TWO_FACTOR_AUTH_REQIURED') {
            this.setState({
              showTwoFactor: true,
              token: error.parameters.token
            }, () => {
              if (this.refs.verificationCode) {
                this.refs.verificationCode.focus();
              }
            });
          }
        }
      }));
    } else {
      // identity is logged => redirect to dashboard (#UNSAFE_componentWillReceiveProps is not called on the start)
      this._redirectLoggedUser();
    }
  }

  _redirectLoggedUser() {
    // Redirection to requested page before login.
    const { location } = this.props;
    // If current url is login, then redirect to main page.
    if (location.pathname === '/login') {
      this.context.history.replace('/');
    } else {
      this.context.history.replace(location.pathname);
    }
  }

  handleSubmit(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    const username = formData.username;
    const password = formData.password;
    this.context.store.dispatch(securityManager.login(username, password, (result, error) => {
      if (error && error.statusEnum) {
        if (error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
          this.context.store.dispatch(dataManager.storeData(SecurityManager.PASSWORD_MUST_CHANGE, password));
          this.context.history.replace(`/password/change?username=${ username }`);
        }
        if (error.statusEnum === 'TWO_FACTOR_AUTH_REQIURED') {
          this.setState({
            showTwoFactor: true,
            token: error.parameters.token,
            username,
            password
          }, () => {
            if (this.refs.verificationCode) {
              this.refs.verificationCode.focus();
            }
          });
        }
      }
    }));
  }

  handleTwoFactor(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    const verificationCode = formData.verificationCode;
    const { token } = this.state;
    this.context.store.dispatch(securityManager.loginTwoFactor({ token, verificationCode }, (result, error) => {
      if (error) {
        if (error.statusEnum && error.statusEnum === 'MUST_CHANGE_IDM_PASSWORD') {
          this.setState({
            showTwoFactor: false
          }, () => {
            this.context.store.dispatch(dataManager.storeData(SecurityManager.PASSWORD_MUST_CHANGE, this.state.password));
            this.context.history.replace(`/password/change?username=${ this.state.username }`);
          });
        }
      } else {
        this.setState({
          showTwoFactor: false
        });
      }
    }));
  }

  getHelp() {
    let helpContent = new HelpContent();
    helpContent = helpContent.setHeader(this.i18n('content.login.twoFactor.help.header'));
    helpContent = helpContent.setBody(
      <div>
        { this.i18n('content.login.twoFactor.help.content', { escape: false }) }
      </div>
    );
    //
    return helpContent;
  }

  render() {
    const { userContext, navigation } = this.props;
    const { showTwoFactor } = this.state;
    const items = getNavigationItems(navigation, null, 'main', userContext, null, true);
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />

        <Basic.Div rendered={ showTwoFactor }>
          <form onSubmit={ this.handleTwoFactor.bind(this) }>
            <Basic.Container component="main" maxWidth="xs">
              <Basic.Panel className="login-container" showLoading={ userContext.showLoading } style={{ maxWidth: 450 }}>
                <Basic.PanelHeader
                  text={ this.i18n('content.login.twoFactor.header') }
                  help={ this.getHelp() }/>
                <Basic.PanelBody>
                  <Basic.AbstractForm ref="form" className="form-horizontal" style={{ padding: 0, backgroundColor: '#fff' }}>
                    <Basic.TextField
                      ref="verificationCode"
                      labelSpan="col-sm-5"
                      componentSpan="col-sm-7"
                      className="last"
                      label={ this.i18n('content.login.twoFactor.verificationCode.label') }
                      placeholder={ this.i18n('content.login.twoFactor.verificationCode.placeholder') }
                      required
                      validation={ Joi.number().integer().min(0).max(999999) }/>
                  </Basic.AbstractForm>
                </Basic.PanelBody>
                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={ () => this.setState({ showTwoFactor: false }) }>
                    { this.i18n('button.cancel') }
                  </Basic.Button>
                  <Basic.Button type="submit" level="success">
                    { this.i18n('button.verify.label') }
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </Basic.Container>
          </form>
        </Basic.Div>

        <Basic.Div rendered={ !showTwoFactor }>
          <form onSubmit={ this.handleSubmit.bind(this) }>
            <Basic.Container component="main" maxWidth="xs">
              <div className="login-container">
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                  <Basic.Avatar level="secondary">
                    <Basic.Icon icon="component:password" />
                  </Basic.Avatar>
                  <h1 style={{ marginTop: 7, marginBottom: 15, fontWeight: 'normal' }}>
                    { this.i18n('header') }
                  </h1>
                </div>
                <Basic.AbstractForm ref="form" className="form-horizontal" style={{ padding: 0 }} showLoading={ userContext.showLoading }>
                  <Basic.TextField
                    ref="username"
                    label={ this.i18n('username') }
                    required
                    fullWidth
                    size="normal"/>
                  <Basic.TextField
                    type="password"
                    ref="password"
                    label={ this.i18n('password') }
                    required
                    fullWidth
                    size="normal"/>

                  <Basic.Button
                    type="submit"
                    level="success"
                    fullWidth
                    variant="contained"
                    showLoading={ userContext.showLoading }>
                    { this.i18n('button.login') }
                  </Basic.Button>

                  <div style={{ marginTop: 15, display: 'flex', justifyContent: 'space-between' }}>
                    {
                      items.map(item => {
                        if (item.to) {
                          return (
                            <Link
                              to={ item.to }
                              key={ `nav-item-${ item.id }` }
                              onClick={ item.onClick }>
                              <Basic.Icon value={ item.icon } style={{ marginRight: 5 }}/>
                              { this.i18n(item.labelKey || item.label) }
                            </Link>
                          );
                        }
                        //
                        if (item.onClick) {
                          return (
                            <a
                              href="#"
                              key={ `nav-item-${ item.id }` }
                              onClick={ item.onClick }>
                              <Basic.Icon value={ item.icon } style={{ marginRight: 5 }}/>
                              { this.i18n(item.labelKey || item.label) }
                            </a>
                          );
                        }
                        //
                        return null;
                      })
                    }
                  </div>
                </Basic.AbstractForm>
              </div>
            </Basic.Container>
          </form>
        </Basic.Div>

        <div className="app-footer">
          <Link
            href={ `${ this.i18n('app.documentation.url') }/start`}
            target="_blank"
            rel="noopener noreferrer">
            { this.i18n('app.helpDesk') }
          </Link>

          <span style={{ margin: '0 10px' }}>|</span>

          <Link
            href="http://redmine.czechidm.com/projects/czechidmng"
            target="_blank"
            rel="noopener noreferrer">
            { this.i18n('app.serviceDesk') }
          </Link>

          <span style={{ margin: '0 10px' }}>|</span>

          <Link
            to="/about"
            title={ this.i18n('content.about.link') }>
            { this.i18n('content.about.link') }
          </Link>
        </div>
      </Basic.Div>
    );
  }
}

Login.propTypes = {
  ...Basic.AbstractContent.propTypes,
  userContext: PropTypes.object
};

Login.defaultProps = {
  ...Basic.AbstractContent.defaultProps,
  userContext: { isAuthenticated: false }
};

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    userContext: state.security.userContext,
    navigation: state.config.get('navigation')
  };
}

export default connect(select)(Login);

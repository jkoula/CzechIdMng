import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import * as Basic from '../../basic';
import { IdentityManager } from '../../../redux';
//
const identityManager = new IdentityManager();

/**
 * Advanced panel header - adds collapse feature.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
class PanelHeader extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    //
    const { userContext, uiKey } = props;
    let collapsed = props.collapsed; // default by props
    if (uiKey && userContext && userContext.profile && userContext.profile.setting) { // or personalized by profile
      if (userContext.profile.setting[uiKey]) {
        collapsed = !!userContext.profile.setting[uiKey].collapsed;
      }
    }
    this.state = {
      collapsed
    };
    //
    this.containerRef = React.createRef();
  }

  getComponentKey() {
    return 'component.advanced.PanelHeader';
  }

  componentDidMount() {
    const { collapsible } = this.props;
    const { collapsed } = this.state;
    //
    if (collapsible) {
      this._setCollapsed(collapsed, false);
    }
  }

  toogleCollapse(event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { collapsed } = this.state;
    this._setCollapsed(!collapsed);
  }

  _setCollapsed(collapsed = false, persistProfile = true) {
    //
    // find panel body
    const panelHeader = $(this.containerRef.current);
    const panel = panelHeader.closest('.panel'); // the first panel above
    if (!panel) {
      return;
    }
    if (!persistProfile) {
      // ui only
      if (collapsed) {
        panel.addClass('collapsed');
      } else {
        panel.removeClass('collapsed');
      }
    } else {
      this.setState({
        collapsed
      }, () => {
        const { userContext, uiKey } = this.props;
        //
        if (collapsed) {
          panel.addClass('collapsed');
          //
          if (uiKey && userContext && userContext.username) {
            this.context.store.dispatch(identityManager.collapsePanel(userContext.username, uiKey));
          }
        } else {
          panel.removeClass('collapsed');
          //
          if (uiKey && userContext && userContext.username) {
            this.context.store.dispatch(identityManager.expandPanel(userContext.username, uiKey));
          }
        }
      });
    }
  }

  render() {
    const { className, rendered, showLoading, text, help, children, style, buttons, collapsible } = this.props;
    const { collapsed } = this.state;
    //
    if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
      return null;
    }
    const classNames = classnames(
      'panel-heading',
      className
    );
    const _style = { cursor: 'pointer' };
    if (help) {
      _style.marginLeft = 5;
    }
    if (!collapsed) {
      _style.color = '#ccc';
    }
    //
    // lookout: html div is required for jQuery usage.
    return (
      <div
        ref={ this.containerRef }
        className={ classNames }
        style={{ display: 'flex', alignItems: 'center', ...style }}>
        <Basic.Div style={{ flex: 1 }}>
          <Basic.Icon type="fa" icon="refresh" showLoading rendered={ showLoading }/>
          {
            showLoading
            ||
            text
            ?
            <h2>{ text }</h2>
            :
            null
          }
          { children }
        </Basic.Div>
        {
          !buttons
          ||
          <Basic.Div>
            { buttons }
          </Basic.Div>
        }
        <Basic.Icon
          icon={ !collapsed ? 'fa:angle-double-up' : 'fa:angle-double-down' }
          style={ _style }
          onClick={ (event) => this.toogleCollapse(event) }
          rendered={ collapsible }
          title={ !collapsed ? this.i18n('button.collapse.title') : this.i18n('button.expand.title') }/>
        <Basic.HelpIcon content={ help }/>
      </div>
    );
  }
}

PanelHeader.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Required for colapsible feature
   */
  uiKey: PropTypes.string,
  /**
   * Header text
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element
  ]),
  /**
   * link to help
   */
  help: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object
  ]),
  /**
   * Collapsible panel.
   *
   * @since 11.2.0
   */
  collapsible: PropTypes.bool,
  /**
   * Panel is expanded / collapsed.
   *
   * @since 11.2.0
   */
  collapsed: PropTypes.bool
};
PanelHeader.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  collapsible: false,
  collapsed: false
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(PanelHeader);

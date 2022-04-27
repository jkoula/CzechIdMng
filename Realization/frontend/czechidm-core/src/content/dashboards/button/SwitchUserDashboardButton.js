import { connect } from 'react-redux';
//

import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { SecurityManager } from '../../../redux';
//
const securityManager = new SecurityManager();

/**
 * Quick button to switch logged identityl.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
class SwitchUserDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  getIcon() {
    return 'component:switch-user';
  }

  isRendered() {
    const { identity, permissions, userContext } = this.props;
    //
    return !identity.disabled
      && userContext.id !== identity.id
      && this.isDevelopment()
      && Utils.Permission.hasPermission(permissions, 'SWITCHUSER');
  }

  getLabel() {
    return this.i18n('component.advanced.IdentityInfo.link.switchUser.label');
  }

  onClick() {
    const { identity } = this.props;
    //
    this.context.store.dispatch(securityManager.switchUser(identity.username, (result) => {
      if (result) {
        this.addMessage({
          level: 'success',
          key: 'core-switch-user-success',
          message: this.i18n('content.identity.switch-user.message.success', { username: identity.username })
        });
        this.context.history.replace(`/`);
      }
      localStorage.removeItem("switchUser");
    }));
  }

  getLevel() {
    return 'secondary';
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext // required
  };
}

export default connect(select)(SwitchUserDashboardButton);

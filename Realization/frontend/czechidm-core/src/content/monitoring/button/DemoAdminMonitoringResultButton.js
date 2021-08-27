import * as Advanced from '../../../components/advanced';

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class DemoAdminMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'component:password';
  }

  getLabel() {
    return this.i18n('content.password.change.title');
  }

  onClick() {
    this.context.history.push('/identity/admin/password/change');
  }
}

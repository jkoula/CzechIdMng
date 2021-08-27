import * as Advanced from '../../../components/advanced';
import { LoggingEventManager, DataManager } from '../../../redux';
//
const manager = new LoggingEventManager();
const dataManager = new DataManager();

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class LoggingEventTaskMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'fa:history';
  }

  getLabel() {
    return this.i18n('content.audit.logging-event.button.show.label');
  }

  onClick(monitoringResult) {
    const level = this.getLevel(monitoringResult);
    if (level && level === 'ERROR') {
      // set search parameters in redux
      const searchParameters = manager.getDefaultSearchParameters().setFilter('levelString', 'ERROR');
      // to conctete table
      this.context.store.dispatch(manager.requestEntities(searchParameters, 'audit-event-table'));
      // prevent to show loading
      this.context.store.dispatch(dataManager.stopRequest('audit-event-table'));
    }
    this.context.history.push('/audit/logging-events');
  }
}

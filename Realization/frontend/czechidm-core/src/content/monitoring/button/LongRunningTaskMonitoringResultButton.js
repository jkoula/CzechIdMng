import * as Advanced from '../../../components/advanced';
import { LongRunningTaskManager, DataManager } from '../../../redux';
//
const manager = new LongRunningTaskManager();
const dataManager = new DataManager();

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class LongRunningTaskMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'component:long-running-tasks';
  }

  getLabel() {
    return this.i18n('content.scheduler.all-tasks.button.show.label');
  }

  onClick(monitoringResult) {
    const level = this.getLevel(monitoringResult);
    if (level && level === 'ERROR') {
      // set search parameters in redux
      const searchParameters = manager.getDefaultSearchParameters().setFilter('operationState', 'EXCEPTION');
      // to conctete table
      this.context.store.dispatch(manager.requestEntities(searchParameters, 'all-long-running-task-table'));
      // prevent to show loading
      this.context.store.dispatch(dataManager.stopRequest('all-long-running-task-table'));
    }
    this.context.history.push('/scheduler/all-tasks');
  }
}

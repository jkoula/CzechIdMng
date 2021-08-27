import * as Advanced from '../../../components/advanced';
import { EntityEventManager, DataManager } from '../../../redux';
//
const manager = new EntityEventManager();
const dataManager = new DataManager();

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class EntityEventTaskMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'fa:circle-o';
  }

  getLabel() {
    return this.i18n('content.entityEvents.button.show.label');
  }

  onClick(monitoringResult) {
    const level = this.getLevel(monitoringResult);
    if (level && level === 'ERROR') {
      // set search parameters in redux
      const searchParameters = manager.getDefaultSearchParameters().setFilter('states', 'EXCEPTION');
      // to conctete table
      this.context.store.dispatch(manager.requestEntities(searchParameters, 'entity-event-table'));
      // prevent to show loading
      this.context.store.dispatch(dataManager.stopRequest('entity-event-table'));
    }
    this.context.history.push('/audit/entity-events');
  }
}

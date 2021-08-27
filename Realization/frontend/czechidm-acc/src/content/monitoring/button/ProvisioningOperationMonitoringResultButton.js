import { Advanced, Managers } from 'czechidm-core';
import { ProvisioningOperationManager } from '../../../redux';
//
const manager = new ProvisioningOperationManager();
const dataManager = new Managers.DataManager();

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class ProvisioningOperationTaskMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'fa:circle-o';
  }

  getLabel() {
    return this.i18n('acc:content.provisioningOperations.button.show.label');
  }

  onClick(monitoringResult) {
    const level = this.getLevel(monitoringResult);
    if (level && level === 'ERROR') {
      // set search parameters in redux
      const searchParameters = manager.getDefaultSearchParameters().setFilter('resultState', 'EXCEPTION');
      // to conctete table
      this.context.store.dispatch(manager.requestEntities(searchParameters, 'provisioning-operation-audit-table'));
      // prevent to show loading
      this.context.store.dispatch(dataManager.stopRequest('provisioning-operation-audit-table'));
    }
    this.context.history.push('/provisioning');
  }
}

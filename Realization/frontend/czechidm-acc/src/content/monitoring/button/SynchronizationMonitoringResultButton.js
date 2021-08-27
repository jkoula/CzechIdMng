import { Advanced, Utils } from 'czechidm-core';

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class SynchronizationMonitoringResultButton extends Advanced.AbstractMonitoringResultButton {

  getIcon() {
    return 'component:synchronization';
  }

  _isValid(monitoringResult) {
    return monitoringResult
      && Utils.Ui.isNotEmpty(monitoringResult.ownerType)
      && Utils.Ui.isNotEmpty(monitoringResult.ownerId)
      && monitoringResult.result
      && monitoringResult.result.model
      && monitoringResult.result.model.parameters
      && Utils.Ui.isNotEmpty(monitoringResult.result.model.parameters.systemId);
  }

  getLabel(monitoringResult) {
    if (!this._isValid(monitoringResult)) {
      return this.i18n('acc:content.systems.button.show.label');
    }
    //
    return this.i18n('acc:content.system.systemSynchronizationConfigDetail.button.show.label');
  }

  onClick(monitoringResult) {
    if (!this._isValid(monitoringResult)) {
      // result properties are missiong - link to system agenda as fallback
      this.context.history.push('/systems');
      return;
    }
    //
    const systemId = monitoringResult.result.model.parameters.systemId;
    const ownerId = monitoringResult.ownerId;
    //
    if (Utils.Ui.getSimpleJavaType(monitoringResult.ownerType) === 'SysSyncLog') {
      this.context.history.push(`/system/${ systemId }/synchronization-logs/${ ownerId }/detail`);
    } else {
      this.context.history.push(`/system/${ systemId }/synchronization-configs/${ ownerId }/detail`);
    }
  }
}

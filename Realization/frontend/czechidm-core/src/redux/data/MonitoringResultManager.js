import EntityManager from './EntityManager';
import { MonitoringResultService } from '../../services';
import SearchParameters from '../../domain/SearchParameters';

/**
 * Monitoring results.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringResultManager extends EntityManager {

  constructor() {
    super();
    this.service = new MonitoringResultService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'MonitoringResult';
  }

  getCollectionType() {
    return 'monitoringResults';
  }

  fetchLastMonitoringResults(cb = null) {
    return (dispatch) => {
      dispatch(this.fetchEntities(
        new SearchParameters('last-results').setFilter('level', ['WARNING', 'ERROR']).setSize(5),
        MonitoringResultManager.UI_KEY_LAST_MONITORING_RESULTS,
        cb
      ));
    };
  }

  /**
   * Executes related monitoring with the same setting as result again.
   *
   * @param  {string}   id
   * @param  {string}   uiKey
   * @param  {Function} cb
   * @return {action}
   * @since 11.2.0
   */
  execute(id, uiKey, cb) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().execute(id)
        .then(() => {
          dispatch(this.dataManager.stopRequest(uiKey));
          if (cb) {
            cb();
          }
        })
        .catch(error => {
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }
}

MonitoringResultManager.UI_KEY_LAST_MONITORING_RESULTS = 'navigation-last-monitoring-results';

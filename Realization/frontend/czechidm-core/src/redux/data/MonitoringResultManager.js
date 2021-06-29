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
}

MonitoringResultManager.UI_KEY_LAST_MONITORING_RESULTS = 'navigation-last-monitoring-results';

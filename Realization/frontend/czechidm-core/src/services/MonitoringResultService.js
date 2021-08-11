import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
//
import SearchParameters from '../domain/SearchParameters';
import * as Utils from '../utils';

/**
 * Monitoring results.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringResultService extends AbstractService {

  getApiPath() {
    return '/monitoring-results';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    //
    return `${ Utils.Ui.getSimpleJavaType(entity.evaluatorType) }`;
  }

  supportsPatch() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'MONITORINGRESULT';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters()
      .setName(SearchParameters.NAME_QUICK)
      .clearSort()
      .setSort('created', 'desc');
  }

  sendLongPollingRequest(mockId, signal) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(mockId) }/check-last-monitoring-results`, null, signal)
      .then(response => response.json())
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Executes related monitoring with the same setting as result again.
   *
   * @param  {string} id
   * @return {promise}
   * @since 11.2.0
   */
  execute(id) {
    return RestApiService
      .put(`${ this.getApiPath() }/${ id }/execute`)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}

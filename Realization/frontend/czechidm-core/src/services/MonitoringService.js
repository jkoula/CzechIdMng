import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Configured monitoring evaluators.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export default class MonitoringService extends AbstractService {

  getApiPath() {
    return '/monitorings';
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
    return 'MONITORING';
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
      .setSort('seq')
      .setSort('evaluatorType')
      .setSort('id');
  }

  /**
   * Loads all registered evaluators (available for monitoring)
   *
   * @return {promise}
   */
  getSupportedEvaluators() {
    return RestApiService
      .get(`${ this.getApiPath() }/search/supported`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Executes given monitoring.
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

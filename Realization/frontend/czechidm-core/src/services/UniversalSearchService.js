import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Universal search service.
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
class UniversalSearchService extends AbstractService {

  getApiPath() {
    return '/universal-searches';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.niceLabel;
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  /**
   * Loads all registered delegation types.
   *
   * @return {promise}
   */
  getSupportedTypes() {
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
}

export default UniversalSearchService;

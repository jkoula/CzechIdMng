import { Services, Domain, Utils } from 'czechidm-core';

export default class SystemAttributeMappingService extends Services.AbstractService {

  constructor() {
    super();
  }

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  getApiPath() {
    return '/system-attribute-mappings';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  getScriptUsage(code) {
    return Services.RestApiService
    .get(`${ this.getApiPath() }/script-usage/${ encodeURIComponent(code) }`)
    .then(response => {
        return response.json();
    })
    .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
    });}

  /**
 * Returns value for attribute and account
 *
 * @param  {string} schema attr name
 * @param  {string} account id
 * @return {promise}
 */
  getAttributeValue(schemaAttrName, accountId) {
    return Services.RestApiService
      .get(`${this.getApiPath()}/${encodeURIComponent(schemaAttrName)}/value/${encodeURIComponent(accountId)}`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
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

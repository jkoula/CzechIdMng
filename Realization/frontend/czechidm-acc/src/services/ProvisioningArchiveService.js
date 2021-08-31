import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import { Utils } from 'czechidm-core';
import ProvisioningOperationService from './ProvisioningOperationService';

/**
 * Archived provisioning operations.
 *
 * @author Radek Tomiška
 */
export default class ProvisioningArchiveService extends Services.AbstractService {

  constructor() {
    super();
    this.operationService = new ProvisioningOperationService();
  }

  getNiceLabel(entity) {
    return this.operationService.getNiceLabel(entity);
  }

  getApiPath() {
    return '/provisioning-archives';
  }

  getGroupPermission() {
    return 'PROVISIONINGARCHIVE';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('modified', 'desc');
  }

  /**
   * Obtains provisioning values with operation type
   *
   * @param  {[type]} id Provisioning archive item id
   * @return {[type]}
   */
  getDecoratedDifferenceObject(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/difference-object`)
      .then(response => {
        return response.json();
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        if (Utils.Response.hasInfo(jsonResponse)) {
          throw Utils.Response.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }
}

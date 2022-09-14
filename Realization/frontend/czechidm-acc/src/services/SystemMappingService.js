import { Services, Domain } from 'czechidm-core';
import SystemOperationTypeEnum from '../domain/SystemOperationTypeEnum';
import { SystemEntityTypeService } from '../services/SystemEntityTypeService';

export default class SystemMappingService extends Services.AbstractService {

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
    return `${entity.name} (${entity.entityType} - ${SystemOperationTypeEnum.getNiceLabel(entity.operationType)})`;
  }

  getApiPath() {
    return '/system-mappings';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('entityType');
  }

  supportsBulkAction() {
    return true;
  }

  /**
   * Validates system mappping
   *
   * @param systemMappingId {String}
   */
   validate(systemMappingId) {
     return Services.RestApiService.get(this.getApiPath() + `/${systemMappingId}/validate`);
   }
}

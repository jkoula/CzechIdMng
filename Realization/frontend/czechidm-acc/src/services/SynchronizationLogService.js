import { Domain, Services } from 'czechidm-core';
import SynchronizationConfigService from './SynchronizationConfigService';

/**
 * Synchronizatin log service.
 *
 * @author Vít Švanda
 * @Author Radek Tomiška
 */
export default class SynchronizationLogService extends Services.AbstractService {

  constructor() {
    super();
    this.synchronizationConfigService = new SynchronizationConfigService();
  }

  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity
      && entity._embedded
      && entity._embedded.synchronizationConfig) {
      return this.synchronizationConfigService.getNiceLabel(entity._embedded.synchronizationConfig);
    }
    return entity.started;
  }

  getApiPath() {
    return '/system-synchronization-logs';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('started', 'DESC');
  }
}

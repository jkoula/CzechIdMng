import { Managers } from 'czechidm-core';
import { SynchronizationLogService } from '../services';

const service = new SynchronizationLogService();

/**
 * @author Vít Švanda
 */
export default class SynchronizationLogManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'SynchronizationLog';
  }

  getCollectionType() {
    return 'synchronizationLogs';
  }
}

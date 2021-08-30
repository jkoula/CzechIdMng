import {Managers} from 'czechidm-core';
import {SystemGroupSystemService} from '../services';

const service = new SystemGroupSystemService();

/**
 * System group-system relation manager
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroupManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemGroupSystem';
  }

  getCollectionType() {
    return 'systemGroupSystems';
  }
}

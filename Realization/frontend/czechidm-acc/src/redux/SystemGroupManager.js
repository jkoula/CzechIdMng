import {Managers} from 'czechidm-core';
import {SystemGroupService} from '../services';

const service = new SystemGroupService();

/**
 * System group manager
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
    return 'SystemGroup';
  }

  getCollectionType() {
    return 'systemGroups';
  }
}

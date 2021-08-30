import EntityManager from './EntityManager';
import RoleSystemService from '../../services/RoleSystemService';

/**
 * IdM role-system manager - (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */

const service = new RoleSystemService();

export default class RoleSystemManager extends EntityManager {

  getService() {
    return service;
  }

  getEntitySubType() {
    return 'RoleSystem';
  }

  getCollectionType() {
    return 'roleSystems';
  }
}

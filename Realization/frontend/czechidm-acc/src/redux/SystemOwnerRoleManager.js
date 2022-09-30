import _ from 'lodash';
import EntityManager from 'czechidm-core/src/redux/data/EntityManager';
import SystemOwnerRoleService from '../services/SystemOwnerRoleService';

export default class SystemOwnerRoleManager extends EntityManager{

    constructor() {
        super();
        this.service = new SystemOwnerRoleService();
      }
    
      getService() {
        return this.service;
      }
    
      getEntityType() {
        return 'OwnerRoles';
      }

      getCollectionType() {
        return 'systemOwnersRoles';
      }
}

import AbstractService from "czechidm-core/src/services/AbstractService";

class SystemOwnerRoleService extends AbstractService {

    getApiPath() {
        return '/system-owner-roles';
      } 
    
      getNiceLabel(entity) {
        if (!entity) {
            return '';
          }
          if (entity.name === entity.code) {
            return entity.name;
          }
          return `${ entity.name } (${ entity.code })`;
        }

      supportsPatch() {
          return false;
      }
      supportsBulkAction() {
        return false;
      }
}
export default SystemOwnerRoleService;
import AbstractService from "czechidm-core/src/services/AbstractService";

class SystemOwnerService extends AbstractService {

    getApiPath() {
        return '/scripts';
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
    
      // dto
      supportsPatch() {
        return false;
      }
    
      supportsBulkAction() {
        return true;
      }
}
export default SystemOwnerService;
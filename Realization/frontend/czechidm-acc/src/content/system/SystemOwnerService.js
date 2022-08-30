import AbstractService from "czechidm-core/src/services/AbstractService";

class SystemOwnerService extends AbstractService {

    getApiPath() {
        return '/system-owners';
        // 1
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
export default SystemOwnerService;
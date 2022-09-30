import AbstractService from "czechidm-core/src/services/AbstractService";
import IdentityService from "czechidm-core/src/services/IdentityService";
import SystemService from "./SystemService";

const systemService = new SystemService()
const identityService = new IdentityService()

class SystemOwnerService extends AbstractService {

    getApiPath() {
        return '/system-owners';
      }
    
    getNiceLabel(entity) {
      let label = `${systemService.getNiceLabel(entity._embedded.system)}`;
      if (entity.owner) {
        label += `${" - "+identityService.getNiceLabel(entity._embedded.owner)}`;
      }
      //
      return label;

      }

    supportsPatch() {
        return false;
      }
      
    supportsBulkAction() {
        return false;
      }

    getGroupPermission() {
        return 'SYSTEMOWNER';
      }
}
export default SystemOwnerService;
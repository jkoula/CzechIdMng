import AbstractService from "czechidm-core/src/services/AbstractService";
import SystemService from "../../services/SystemService";
import RoleService from "czechidm-core/src/services/RoleService";

const systemService = new SystemService()
const roleService = new RoleService()

class SystemOwnerRoleService extends AbstractService {

    getApiPath() {
        return '/system-owner-roles';
      } 
    
    getNiceLabel(entity) {
        let label = `${systemService.getNiceLabel(entity._embedded.system)}`;
        if (entity.ownerRole) {
          label += `${roleService.getNiceLabel(entity._embedded.ownerRole)}`;
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
        return 'SYSTEMOWNERROLE';
      }
}
export default SystemOwnerRoleService;
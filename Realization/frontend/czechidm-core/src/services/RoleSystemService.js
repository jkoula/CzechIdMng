import SearchParameters from '../domain/SearchParameters';
import AbstractService from './AbstractService';

/**
 * IdM role-system service - It is parent for SysRoleSystemService in Acc module (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class RoleSystemService extends AbstractService {

  getApiPath() {
    return '/core/role-systems';
  }

  getGroupPermission() {
    return 'ROLE';
  }

  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity._embedded.role.name} - ${entity._embedded.system.name} (${entity._embedded.systemMapping.entityType})`;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}

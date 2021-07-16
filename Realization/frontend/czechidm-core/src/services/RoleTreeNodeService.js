import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import TreeNodeService from './TreeNodeService';

/**
 * Automatic roles administration.
 *
 * @author Radek Tomiška
 */
export default class RoleTreeNodeService extends AbstractService {

  constructor() {
    super();
    this.roleService = new RoleService();
    this.treeNodeService = new TreeNodeService();
  }

  getApiPath() {
    return '/role-tree-nodes';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (!entity._embedded) {
      if (entity.name) {
        return entity.name;
      }
      return entity.id;
    }
    let label = '';
    if (entity._embedded.role) {
      label = `${ this.roleService.getNiceLabel(entity._embedded.role) }`;
    }
    if (entity._embedded.treeNode) {
      if (label !== '') {
        label += ', ';
      }
      label += `${ this.treeNodeService.getNiceLabel(entity._embedded.treeNode) }`;
    }
    if (label !== '') {
      label += ' - ';
    }
    label += `${ entity.name ? entity.name : entity.id }`;
    //
    return label;
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'ROLETREENODE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name', 'asc');
  }
}

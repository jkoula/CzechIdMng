

import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class TreeNodeService extends AbstractService {

  const
  getApiPath() {
    return '/tree/nodes';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Returns search parameters by parent ID, used in tree
   */
  getTreeSearchParameters() {
    return super.getDefaultSearchParameters().setName(TreeNodeService.TREE_SEARCH).clearSort().setSort('name');
  }

  /**
   * Returns search parameters for search roots
   */
  getRootSearchParameters() {
    return super.getDefaultSearchParameters().setName(TreeNodeService.ROOT_SEARCH).clearSort().setSort('name');
  }
}

/**
 * Search by parent ID for tree
 * @type {Number}
 */
TreeNodeService.TREE_SEARCH = 'children';

/**
 * Search roots
 */
TreeNodeService.ROOT_SEARCH = 'roots';

export default TreeNodeService;

import {Services, Domain} from 'czechidm-core';

/**
 * System group service
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroupService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.code}`;
  }

  getApiPath() {
    return '/system-groups';
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'SYSTEMGROUP';
  }

  supportsBulkAction() {
    return true;
  }

  supportsPatch() {
    return false;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('code');
  }
}

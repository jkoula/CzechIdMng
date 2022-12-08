import { Services, Domain, Utils } from 'czechidm-core';

class RequestAccountRoleService extends Services.FormableEntityService {

  getApiPath() {
    return '/acc/concept-role-requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.role) {
      return `${request._embedded.role.name}`;
    }
    return request.id;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('role.name', 'asc');
  }
}

export default RequestAccountRoleService;

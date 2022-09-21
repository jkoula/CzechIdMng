import { Managers } from 'czechidm-core';
import RequestAccountRoleService from "../services/RequestAccountRoleService";

export default class RequestAccountRoleManager extends Managers.RequestIdentityRoleManager {
  constructor() {
    super();
    this.service = new RequestAccountRoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RequestAccountRole';
  }

  getCollectionType() {
    return 'requestAccountRoles';
  }

}

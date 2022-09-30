import { Managers } from 'czechidm-core';
import RequestAccountRoleService from "../services/RequestAccountRoleService";

export default class RequestAccountRoleManager extends Managers.RequestIdentityRoleManager {
  constructor() {
    super();
    this.service = new RequestAccountRoleService();
  }

  createEntity(entity, uiKey = null, cb = null) {
    if (entity) {
      entity.assignmentType = "eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto"
    }
    return super.createEntity(entity, uiKey, cb)
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

import _ from "lodash";
import { SystemOwnerService } from "../../services";
import EntityManager from "czechidm-core/src/redux/data/EntityManager";

export default class SystemOwnerManager extends EntityManager {
  constructor() {
    super();
    this.service = new SystemOwnerService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return "Owners";
  }

  getCollectionType() {
    return "systemOwners";
  }
}

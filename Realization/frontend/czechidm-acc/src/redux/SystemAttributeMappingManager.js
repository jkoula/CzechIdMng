import { Managers } from 'czechidm-core';
import { SystemAttributeMappingService } from '../services';

const service = new SystemAttributeMappingService();

export default class SystemAttributeMappingManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SystemAttributeMapping';
  }

  getCollectionType() {
    return 'systemAttributeMappings';
  }

  getScriptUsage(code, uiKey = null, cb = null) {
    return (dispatch, getState) => {
      dispatch(this.dataManager.requestData(uiKey));
        this.getService().getScriptUsage(code)
        .then(json => {
          if (cb) {
            cb(json, null, uiKey)
          }
          dispatch(this.receiveEntities(null, json, uiKey));
        })
        .catch(error => {
          if (cb) {
            cb(null, error, uiKey)
          }
          dispatch(this.dataManager.receiveError(null, uiKey, error));
        });
    };
  }
}

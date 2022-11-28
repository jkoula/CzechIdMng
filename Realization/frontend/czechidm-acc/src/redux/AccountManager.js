import Immutable from 'immutable';

import { Managers } from 'czechidm-core';
import { AccountService } from '../services';
import RequestAccountRoleManager from "./RequestAccountRoleManager";

const service = new AccountService();

/**
 * Accounts.
 *
 * @author Radek Tomiška
 */
export default class AccountManager extends Managers.FormableEntityManager {

  constructor() {
    super();
    this.dataManager = new Managers.DataManager();
    this.incompatibleRoleManager = new Managers.IncompatibleRoleManager();
  }


  getService() {
    return service;
  }

  getEntityType() {
    return 'Account'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'accounts';
  }

  /**
   * Load account incompatible roles (by assigned roles) from BE
   *
   * @param  {string} id
   * @param  {string} uiKey
   * @return {array[object]}
   */
   fetchIncompatibleRoles(id, uiKey) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getIncompatibleRoles(id)
        .then(json => {
          dispatch(this.dataManager.receiveData(uiKey, json._embedded[this.incompatibleRoleManager.getCollectionType()]));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }


    /**
   * Loads all registered account wizards
   *
   * @return {action}
   */
     fetchSupportedTypes() {
      const uiKey = AccountManager.UI_KEY_SUPPORTED_TYPES;
      //
      return (dispatch, getState) => {
        const loaded = Managers.DataManager.getData(getState(), uiKey);
        if (loaded) {
          // we dont need to load them again - change depends on BE restart
        } else {
          dispatch(this.getDataManager().requestData(uiKey));
          this.getService().getSupportedTypes()
            .then(json => {
              let types = new Immutable.Map();
              if (json._embedded && json._embedded.accountWizards) {
                json._embedded.accountWizards.forEach(item => {
                  types = types.set(item.id, item);
                });
              }
              dispatch(this.getDataManager().receiveData(uiKey, types));
            })
            .catch(error => {
              // TODO: data uiKey
              dispatch(this.getDataManager().receiveError(null, uiKey, error));
            });
        }
      };
    }
}

AccountManager.ENTITY_TYPE = "eu.bcvsolutions.idm.acc.dto.AccAccountDto";
AccountManager.UI_KEY_SUPPORTED_TYPES = 'account-wizards';
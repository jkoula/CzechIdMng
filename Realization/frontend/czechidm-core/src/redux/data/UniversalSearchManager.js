import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import DataManager from './DataManager';
import UniversalSearchService from '../../services/UniversalSearchService';

/**
 * Universal search manager.
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
export default class UniversalSearchManager extends EntityManager {

  constructor() {
    super();
    this.service = new UniversalSearchService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'UniversalSearch';
  }

  getCollectionType() {
    return 'universalSearches';
  }

  /**
   * Loads all registered delegation types.
   *
   * @return {action}
   */
  fetchSupportedTypes() {
    const uiKey = UniversalSearchManager.UI_KEY_SUPPORTED_TYPES;
    //
    return (dispatch, getState) => {
      const loaded = DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getSupportedTypes()
          .then(json => {
            let types = new Immutable.Map();
            if (json._embedded && json._embedded.delegationTypes) {
              json._embedded.delegationTypes.forEach(item => {
                types = types.set(item.id, item);
              });
            }
            dispatch(this.dataManager.receiveData(uiKey, types));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.dataManager.receiveError(null, uiKey, error));
          });
      }
    };
  }
}

UniversalSearchManager.UI_KEY_SUPPORTED_TYPES = 'universal-search-supported-types';

import Immutable from 'immutable';
//
import { Managers, Utils } from 'czechidm-core';
import { SystemEntityTypeService } from '../services';

/**
 * System entity types
 *
 * @author Tomáš Doischer
 */
export default class SystemEntityTypeManager extends Managers.EntityManager {

    constructor() {
        super();
        this.dataManager = new Managers.DataManager();
        this.service = new SystemEntityTypeService();
    }

    getService() {
        return this.service;
    }

    getEntityType() {
        return SystemEntityTypeManager.UI_KEY_SUPPORTED_ENTITY_TYPES;
    }

    getUiKey() {
        return 'entity-type';
    }

    /**
    * Loads registered system entity types
    *
    * @return {action}
    */
    fetchEntities(searchParameters, uiKey, cb) {
        //
        return (dispatch, getState) => {
        const loaded = Managers.DataManager.getData(getState(), uiKey);
        const showLoading = Managers.DataManager.isShowLoading(getState(), uiKey);
        if (loaded || showLoading) {
            // we dont need to load them again - change depends on BE restart
        } else {
            dispatch(this.dataManager.requestData(uiKey));
            this.getService().getSupportedEntityTypes()
            .then(json => {``
                let types = new Immutable.Map();
                if (json._embedded && json._embedded.systemEntityTypes) {
                    json._embedded.systemEntityTypes.forEach(item => {
                        item.id = item.systemEntityCode
                        types = types.set(item.value, item.systemEntityCode);
                    });
                }
                cb(json, null, uiKey)
                dispatch(this.dataManager.receiveData(uiKey, types));
            })
            .catch(error => {
                cb(null, error, uiKey)
                dispatch(this.dataManager.receiveError(null, uiKey, error));
            });
        }
        };
    }

    getNiceLabelForEntityType(entityType) {
        if (entityType && entityType._embedded && entityType._embedded.systemEntityType) {
            return this.getService().getNiceLabel(entityType._embedded.systemEntityType);
        }
    }

    getCollectionType() {
        return 'systemEntityTypes';
    }
}

SystemEntityTypeManager.UI_KEY_TASKS = 'system-entity-types';
SystemEntityTypeManager.UI_KEY_SUPPORTED_ENTITY_TYPES = 'SupportedSystemEntityTypes';
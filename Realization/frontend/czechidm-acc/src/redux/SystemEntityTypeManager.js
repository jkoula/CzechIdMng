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

    /**
    * Loads registered system entity types
    *
    * @return {action}
    */
    fetchSupportedTasks() {
        const uiKey = SystemEntityTypeManager.UI_KEY_SUPPORTED_TASKS;
        //
        return (dispatch, getState) => {
        const loaded = Managers.DataManager.getData(getState(), uiKey);
        const showLoading = Managers.DataManager.isShowLoading(getState(), uiKey);
        if (loaded || showLoading) {
            // we dont need to load them again - change depends on BE restart
        } else {
            dispatch(this.dataManager.requestData(uiKey));
            this.getService().getSupportedEntityTypes()
            .then(json => {
                let tasks = new Immutable.Map();
                if (json._embedded && json._embedded.types) {
                json._embedded.types.forEach(item => {
                    types = types.set(item.type, item);
                });
                }
                dispatch(this.dataManager.receiveData(uiKey, tasks));
            })
            .catch(error => {
                dispatch(this.dataManager.receiveError(null, uiKey, error));
            });
        }
        };
    }

    // getNiceLabel(entity, showDescription = true, supportedTasks = null) {
    //     let _taskType;
    //     if (supportedTasks && supportedTasks.has(entity.taskType)) {
    //       _taskType = supportedTasks.get(entity.taskType);
    //     }
    //     if (_taskType && _taskType.formDefinition) {
    //       const simpleTaskType = Utils.Ui.getSimpleJavaType(entity.taskType);
    //       let _label = formAttributeManager.getLocalization(_taskType.formDefinition, null, 'label', simpleTaskType);
    //       if (_label !== simpleTaskType) {
    //         _label += ` (${ simpleTaskType })`;
    //       }
    //       return _label;
    //     }
    //     return this.getService().getNiceLabel(entity, showDescription);
    // }
}

SystemEntityTypeManager.UI_KEY_TASKS = 'system-entity-types';
SystemEntityTypeManager.UI_KEY_SUPPORTED_TASKS = 'supported-system-entity-types';
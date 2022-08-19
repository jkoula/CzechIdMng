import { Services, Utils } from 'czechidm-core';

/**
 * System entity types
 *
 * @author Tomáš Doischer
 */
export default class SystemEntityTypeService extends Services.AbstractService {

    getApiPath() {
        return '/system-entity-type';
    }
    
    supportsAuthorization() {
        return false;
    }

    /**
     * Loads all registered tasks (available for scheduling)
     *
     * @return {promise}
     */
    getSupportedTasks() {
        return Services.RestApiService
        .get(this.getApiPath() + '/search/supported')
        .then(response => {
            return response.json();
        })
        .then(json => {
            if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
            }
            return json;
        });
    }
}
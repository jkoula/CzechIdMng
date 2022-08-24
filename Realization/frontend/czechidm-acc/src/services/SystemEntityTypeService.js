import { Services, Utils } from 'czechidm-core';

/**
 * System entity types
 *
 * @author Tomáš Doischer
 */
export default class SystemEntityTypeService extends Services.AbstractService {

    getApiPath() {
        return '/system-entity-types';
    }
    
    supportsAuthorization() {
        return false;
    }

    /**
     * Loads all registered tasks (available for scheduling)
     *
     * @return {promise}
     */
     getSupportedEntityTypes() {
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

    getNiceLabel(entity) {
        return Services.LocalizationService.i18n(`${entity.module}:entity.SystemEntityType.${entity.systemEntityCode}.label`);
    }

    getNiceLabelForEntityType(entityType) {
        if (entityType && entityType._embedded && entityType._embedded.systemEntityType) {
            return this.getNiceLabel(entityType._embedded.systemEntityType);
        }
    }
}
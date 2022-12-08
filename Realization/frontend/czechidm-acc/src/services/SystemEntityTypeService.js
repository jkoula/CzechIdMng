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
     * Returns options by type by given id and mapping
     *
     * @param  {string|number} id of type
     * @param  {string|number} id of mapping
     * @return {Promise} promise with response
     */
    getByIdAndMapping(id, systemMappingId) {
        return Services.RestApiService
        .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/${ encodeURIComponent(systemMappingId) }`)
        .then(response => {
            return response.json();
        })
        .then(json => {
            if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
            }
            return json;
        }).catch(ex => {
            throw this._resolveException(ex);
        });
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

    /**
    * Returns resource by given id
    *
    * @param  {string|number} id resource identifier
    * @return {Promise} promise with response
    */
    getById(id) {
        return super.getById(id).then(json => {
            let result = { ...json } ;
            result.id = result.systemEntityCode;
            return result;
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
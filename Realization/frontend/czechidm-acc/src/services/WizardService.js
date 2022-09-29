import { Services, Utils} from 'czechidm-core';

/**
 * @author Roman Kučera
 */
class WizardService extends Services.FormableEntityService {
    /**
     * Loads all registered wizards.
     */
    getSupportedTypes() {
        return Services.RestApiService
            .get(`${this.getApiPath()}/search/supported`)
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
     * Execute wizard step.
     */
    executeConnectorType(connectorType) {
        return Services.RestApiService
            .post(`${this.getApiPath()}/wizards/execute`, connectorType)
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
     * Open existed wizard step.
     */
    loadConnectorType(connectorType) {
        return Services.RestApiService
            .put(`${this.getApiPath()}/wizards/load`, connectorType)
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

export default WizardService;
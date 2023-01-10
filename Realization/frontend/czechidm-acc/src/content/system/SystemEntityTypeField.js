import PropTypes from 'prop-types';
import { SystemEntityTypeManager } from '../../redux';
//
import { Basic } from 'czechidm-core';

const uiKey = 'system-entity-type-field';
const systemEntityTypeManager = new SystemEntityTypeManager();

/**
* Localized system entity type field.
*
* @author Tomáš Doischer
*/
export default class SystemEntityTypeField extends Basic.AbstractContent {

    constructor(props, context) {
        super(props, context);
        if (!this.state) {
            this.state = { };
        }
      }

    getManager() {
        return manager;
    }
    
    getUiKey() {
        return uiKey;
    }

    componentDidMount() {
        const {entityType, mapping} = this.props;

        if (!entityType && mapping) {
            this.context.store.dispatch(systemEntityTypeManager.fetchEntityByMapping(mapping.entityType, mapping.id, null, (type) => {
                if (type) {
                    const localizedSystemEntityType = systemEntityTypeManager.getNiceLabel(type);
                    this.setState({
                        localizedSystemEntityType
                    })
                }
            }));
        } else if (entityType) {
            const localizedSystemEntityType = systemEntityTypeManager.getNiceLabel(type);
            this.setState({
                localizedSystemEntityType
            })
        }
    }

    render() {
        const { localizedSystemEntityType } = this.state;

        if (localizedSystemEntityType) {
            return localizedSystemEntityType;
        }

        return null;
    }
}

SystemEntityTypeField.propTypes = {
    ...Basic.AbstractComponent.propTypes,
    entityType: PropTypes.object,
    mapping: PropTypes.object
  };
  
  SystemEntityTypeField.defaultProps = {
    ...Basic.AbstractComponent.defaultProps,
    entityType: null,
    mapping: null
  };
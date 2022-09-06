import React from 'react';
import {Advanced} from 'czechidm-core';
import SystemOptionDecorator from './SystemOptionDecorator';
import SystemValueDecorator from './SystemValueDecorator';

export default class SystemSelect extends Advanced.EntitySelectBox {

    render(){
        const { rendered, entityType, ...others } = this.props;
        if (!rendered) {
          return null;
        }
        if (entityType && entityType !== 'System') {
          LOGGER.warn(`IdentitySelect supports identity entity type only, given [${ entityType }] type will be ignored.`);
        }
        return (
            <Advanced.EntitySelectBox
              ref="selectComponent"
              entityType="System"
              { ...others }/>
        );
    }
}
  SystemSelect.propTypes = {
    ...Advanced.EntitySelectBox.propTypes
  };
  SystemSelect.defaultProps = {
    ...Advanced.EntitySelectBox.defaultProps,
    optionComponent: SystemOptionDecorator,
    valueComponent: SystemValueDecorator
  };
  
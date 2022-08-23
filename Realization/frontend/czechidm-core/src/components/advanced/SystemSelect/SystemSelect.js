import React from 'react';
//
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';

export default class SystemSelect extends EntitySelectBox {

    render(){
        const { rendered, entityType, ...others } = this.props;
        // 
        if (!rendered) {
          return null;
        }
        if (entityType && entityType !== 'System') {
          LOGGER.warn(`IdentitySelect supports identity entity type only, given [${ entityType }] type will be ignored.`);
        }
        //
        return (
            <EntitySelectBox
              ref="selectComponent"
              entityType="System"
              { ...others }/>
        );
    }
}
    SystemSelect.propTypes = {
    ...EntitySelectBox.propTypes
  };
    SystemSelect.defaultProps = {
    ...EntitySelectBox.defaultProps,
  };
  
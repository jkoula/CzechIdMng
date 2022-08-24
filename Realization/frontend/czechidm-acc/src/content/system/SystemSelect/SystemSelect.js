import React from 'react';
//
import EntitySelectBox from 'czechidm-core/src/components/advanced/EntitySelectBox/EntitySelectBox';
// import IdentityOptionDecorator from 'czechidm-core/src/components/advanced/IdentitySelect/IdentityOptionDecorator';
// import IdentityValueDecorator from 'czechidm-core/src/components/advanced/IdentitySelect/IdentityValueDecorator';

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
  // SystemSelect.propTypes = {
  //   ...EntitySelectBox.propTypes
  // };
  // SystemSelect.defaultProps = {
  //   ...EntitySelectBox.defaultProps,
  //   optionComponent: IdentityOptionDecorator,
  //   valueComponent: IdentityValueDecorator
  // };
  
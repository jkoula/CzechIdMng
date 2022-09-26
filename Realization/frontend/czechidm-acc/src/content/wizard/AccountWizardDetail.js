import { Basic, ComponentService } from 'czechidm-core';
import React from 'react';
import IdmContext from 'czechidm-core/src/context/idm-context';
import DefaultAccountWizard from './account/DefaultAccountWizard';

const componentService = new ComponentService();

/**
 * Detail of a account wizzard
 *
 * @author Roman Kucera
 */
export default class AccountWizardDetail extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.wizardContext = {};
  }

  render() {
    const {show, connectorType, closeWizard, reopened} = this.props;
    const wizardContext = this.wizardContext;
    let ConnectorTypeComponent = DefaultAccountWizard;
    if (connectorType) {
      const component = componentService.getConnectorTypeComponent(connectorType.id);
      if (component) {
        ConnectorTypeComponent = component.component;
      }
    }

    return (
      <Basic.Div rendered={!!show}>
        <IdmContext.Provider value={{...this.context, wizardContext}}>
          <ConnectorTypeComponent
            match={this.props.match}
            modal
            reopened={reopened}
            closeWizard={closeWizard}
            connectorType={connectorType}
            show={!!show}/>
        </IdmContext.Provider>
      </Basic.Div>
    );
  }

}

AccountWizardDetail.defaultProps = {
  reopened: false // Defines if the wizard use for create new system or for reopen existed.
};

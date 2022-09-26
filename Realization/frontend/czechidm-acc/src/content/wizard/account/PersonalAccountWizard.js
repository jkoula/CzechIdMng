import { Basic } from 'czechidm-core';
import React from 'react';
import IdmContext from 'czechidm-core/src/context/idm-context';
import DefaultAccountWizard from './DefaultAccountWizard';
import AccountTypeEnum from '../../../domain/AccountTypeEnum';

/**
 * Wizard for personal account
 *
 * @author Roman Kucera
 */
 export default class PersonalAccountWizard extends Basic.AbstractContextComponent {

    constructor(props, context) {
        super(props, context);
        this.wizardContext = {};
    }

    render() {
        const {show, connectorType, closeWizard, reopened} = this.props;
        const wizardContext = this.wizardContext;

        return (
            <Basic.Div rendered={!!show}>
              <IdmContext.Provider value={{...this.context, wizardContext}}>
                <DefaultAccountWizard
                  match={this.props.match}
                  modal
                  reopened={reopened}
                  closeWizard={closeWizard}
                  connectorType={connectorType}
                  show={!!show}
                  accountType={AccountTypeEnum.findKeyBySymbol(AccountTypeEnum.PERSONAL)}/>
              </IdmContext.Provider>
            </Basic.Div>
          );
    }
 }
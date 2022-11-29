import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from "../../components/basic";
import * as Advanced from "../../components/advanced";
import AbstractComponent from "../../components/basic/AbstractComponent/AbstractComponent";
import AbstractContextComponent from "../../components/basic/AbstractContextComponent/AbstractContextComponent";
import {
    IdentityManager,
    RoleTreeNodeManager,
    RoleManager,
    IdentityContractManager,
    CodeListManager,
    DataManager,
    ConfigurationManager, RequestIdentityRoleManager
} from '../../redux';

const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();

export class IdentityRoleTableFilter extends AbstractContextComponent {

    render() {
        const {useFilter, showEnvironment, hasRoleForceFilter, hasIdentityForceFilter, environmentItems,
            contractForceSearchparameters, cancelFilter} = this.props

        return <Advanced.Filter onSubmit={ useFilter }>
            <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                    <Basic.Col lg={ showEnvironment ? 3 : 5 }>
                        <Advanced.Filter.TextField
                            ref="roleText"
                            placeholder={ this.i18n('filter.role.placeholder') }
                            header={ this.i18n('filter.role.placeholder') }
                            hidden={ hasRoleForceFilter }
                            help={ Advanced.Filter.getTextHelp() }/>
                        <Advanced.Filter.SelectBox
                            ref="identityId"
                            manager={ identityManager }
                            label={ null }
                            placeholder={ this.i18n('filter.identity.placeholder') }
                            header={ this.i18n('filter.identity.placeholder') }
                            hidden={ hasIdentityForceFilter }/>
                    </Basic.Col>
                    <Basic.Col lg={ 3 } rendered={ showEnvironment }>
                        <Advanced.CodeListSelect
                            ref="roleEnvironment"
                            code="environment"
                            label={ null }
                            placeholder={ this.i18n('entity.Role.environment.label') }
                            items={ environmentItems || [] }
                            hidden={ hasRoleForceFilter }
                            multiSelect/>
                    </Basic.Col>
                    <Basic.Col lg={ showEnvironment ? 3 : 4 }>
                        <Basic.Div rendered={ contractForceSearchparameters !== null }>
                            <Advanced.Filter.SelectBox
                                ref="identityContractId"
                                placeholder={ this.i18n('entity.IdentityRole.identityContract.title') }
                                manager={ identityContractManager }
                                forceSearchParameters={ contractForceSearchparameters }
                                niceLabel={ (entity) => identityContractManager.getNiceLabel(entity, false) }/>
                        </Basic.Div>
                    </Basic.Col>
                    <Basic.Col lg={ 3 } className="text-right">
                        <Advanced.Filter.FilterButtons cancelFilter={ cancelFilter }/>
                    </Basic.Col>
                </Basic.Row>
            </Basic.AbstractForm>
        </Advanced.Filter>
    }

}
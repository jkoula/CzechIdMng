import React from "react";
import SearchParameters from "../../domain/SearchParameters";
import * as Basic from "../../components/basic";
import * as Utils from "../../utils";
import ComponentService from "../../services/ComponentService";
import AbstractContextComponent from "../../components/basic/AbstractContextComponent/AbstractContextComponent";
import {
    IdentityContractManager
} from '../../redux';

const componentService = new ComponentService();

export class AssignmentOwnerSelector extends AbstractContextComponent {

    constructor(props, context) {
        super(props, context);
        //
        const ownerType = props.isAccount ?
            "eu.bcvsolutions.idm.acc.dto.AccAccountDto" : props.entity && props.entity.ownerType ?
                props.entity.ownerType :IdentityContractManager.ENTITY_TYPE;
        this.state = {
            selectedOwnerType: ownerType,
            value: props.entity?.ownerUuid
        }
    }

    handleTabSwitch = (newValue) => {
            this.setState({selectedOwnerType: newValue, value: null})
    }

    handleChange = (value) => {
        const {onChange} = this.props;
        this.setState({value: value?.id}, onChange);
    }

    getSelectedOwnerType = () => {
        return this.state.selectedOwnerType;
    }

    getValue = () => {
        return this.state.value;
    }

    getTabsForOwnerSelection = () => {
        const {selectedOwnerType, value} = this.state;
        const {identityUsername, accountId, isAccount, readOnly, isNew, entity} = this.props;
        const components = componentService.getRoleAssignmentComponents().toList();
        return components.map(component => {
                const ManagerType = component.ownerManager;
                const managerInstance = new ManagerType();

                let forceParams = new SearchParameters()
                    .setFilter('id', accountId)
                    .setFilter('validNowOrInFuture', true)
                    .setFilter('_permission', ['CHANGEPERMISSION', 'CANBEREQUESTED'])
                    .setFilter('_permission_operator', 'or')

                if (!isAccount) {
                    forceParams = forceParams.setFilter('identity', identityUsername);
                }

                return <Basic.Tab value={component.ownerType}
                                  title={this.i18n(component.locale + '.label')}
                                  rendered={isAccount || !isNew ? component.ownerType === selectedOwnerType : true}>
                    {
                        component.ownerType === selectedOwnerType ?
                            (
                                <component.ownerSelectComponent
                                    ref={component.ownerType}
                                    manager={managerInstance}
                                    forceSearchParameters={forceParams}
                                    defaultSearchParameters={
                                        new SearchParameters().clearSort()
                                    }
                                    pageSize={100}
                                    returnProperty={false}
                                    readOnly={!isNew || readOnly || !Utils.Entity.isNew(entity)}
                                    onChange={this.handleChange}
                                    niceLabel={(owner) => managerInstance.getNiceLabel(owner, false)}
                                    required
                                    useFirst
                                    clearable={false}
                                    disabled={isAccount}
                                    value={component.ownerType === selectedOwnerType ? value : null}
                                />
                            ) : <span/>
                    }
                </Basic.Tab>
            }
        );
    }

    render() {
        const {selectedOwnerType} = this.state;
        const tabs = this.getTabsForOwnerSelection();
        //
        return (
            <Basic.Tabs onSelect={this.handleTabSwitch} activeKey={selectedOwnerType}>
                {tabs}
            </Basic.Tabs>
        );
    }
}
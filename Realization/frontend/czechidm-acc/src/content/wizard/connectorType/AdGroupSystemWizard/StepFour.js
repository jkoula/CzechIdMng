import {Basic, Advanced, Domain} from 'czechidm-core';
import React from 'react';
import AbstractWizardStep from '../../AbstractWizardStep';
import {SystemMappingManager} from '../../../../redux';

const systemMappingManager = new SystemMappingManager();

/**
 * Step four of MS AD group wizard (group container).
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
export default class StepFour extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const metadata = this.state.connectorType.metadata;
    if (metadata) {
      this.state.membershipSwitch = metadata.membershipSwitch === 'true';
      this.state.assignCatalogueSwitch = metadata.assignCatalogueSwitch === 'true';
      this.state.removeCatalogueRoleSwitch = metadata.removeCatalogueRoleSwitch === 'true';
      this.state.assignRoleSwitch = metadata.assignRoleSwitch === 'true';
      this.state.assignRoleRemoveSwitch = metadata.assignRoleRemoveSwitch === 'true';
      this.state.groupContainer = metadata.groupContainer;
      this.state.mainRoleCatalog = metadata.mainRoleCatalog;
      this.state.newRoleCatalog = metadata.newRoleCatalog;
      this.state.regenerateSchemaSwitch = metadata.regenerateSchemaSwitch === 'true';
      this.state.schemaId = metadata.schemaId;
      if (metadata.memberSystemMappingId !== undefined) {
        this.state.memberSystemMappingId = metadata.memberSystemMappingId;
      }
    }
    this.state.identityMappingSearchParameters = new Domain.SearchParameters()
      .setFilter('operationType', 'PROVISIONING')
      .setFilter('entityType', 'IDENTITY');
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.mapping = json._embedded.mapping;
    wizardContext.connectorType = json;
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
    metadata.groupContainer = formData.groupContainer;
    metadata.membershipSwitch = formData.membershipSwitch;
    metadata.assignCatalogueSwitch = formData.assignCatalogueSwitch;
    metadata.removeCatalogueRoleSwitch = formData.removeCatalogueRoleSwitch;
    metadata.assignRoleSwitch = formData.assignRoleSwitch;
    metadata.assignRoleRemoveSwitch = formData.assignRoleRemoveSwitch;
    metadata.mainRoleCatalog = formData.mainRoleCatalog;
    metadata.newRoleCatalog = formData.newRoleCatalog;
    metadata.regenerateSchemaSwitch = formData.regenerateSchemaSwitch;
    metadata.memberSystemMappingId = formData.memberSystemMappingId;
  }

  _toggleSwitch(key) {
    const state = {};
    state[key] = !this.state[key];

    this.setState(state);
  }

  _onChangeGroupContainer(event) {
    const value = event.currentTarget.value;
    this.setState({
      groupContainer: value
    });
  }

  _onChangeNewRoleCatalog(event) {
    const baseCode = event.currentTarget.value;
    this.setState({
      newRoleCatalog: baseCode,
      mainRoleCatalog: null
    });
  }

  _onChangeMainRoleCatalog(value) {
    this.setState({
      newRoleCatalog: null,
      mainRoleCatalog: value
    });
  }

  _onChangeMemberSystem(systemMapping) {
    const systemMappingId = systemMapping ? systemMapping.id : null;
    this.setState({
      memberSystemMappingId: systemMappingId
    });
  }

  render() {
    const {
      showLoading,
      membershipSwitch,
      assignRoleSwitch,
      assignCatalogueSwitch,
      removeCatalogueRoleSwitch,
      assignRoleRemoveSwitch,
      groupContainer,
      mainRoleCatalog,
      newRoleCatalog,
      regenerateSchemaSwitch,
      schemaId,
      identityMappingSearchParameters,
      memberSystemMappingId
    } = this.state;

    const formData = {};
    formData.groupContainer = groupContainer;
    formData.membershipSwitch = membershipSwitch;
    formData.assignCatalogueSwitch = assignCatalogueSwitch;
    formData.removeCatalogueRoleSwitch = removeCatalogueRoleSwitch;
    formData.assignRoleSwitch = assignRoleSwitch;
    formData.assignRoleRemoveSwitch = assignRoleRemoveSwitch;
    formData.newRoleCatalog = newRoleCatalog;
    formData.mainRoleCatalog = mainRoleCatalog;
    formData.regenerateSchemaSwitch = regenerateSchemaSwitch;
    formData.memberSystemMappingId = memberSystemMappingId;

    const locKey = this.getLocKey();
    const roleSyncLocKey = 'acc:content.system.systemSynchronizationConfigDetail.roleConfigDetail';

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          onSubmit={(event) => {
            this.wizardNext(event);
          }}
          data={formData}>
          <Basic.SelectBox
            ref="memberSystemMappingId"
            manager={systemMappingManager}
            niceLabel={(mapping) => mapping._embedded.objectClass._embedded.system.name}
            forceSearchParameters={identityMappingSearchParameters}
            label={this.i18n(`${locKey}.memberSystemMapping.label`)}
            onChange={this._onChangeMemberSystem.bind(this)}
            required
            helpBlock={this.i18n(`${locKey}.memberSystemMapping.help`)}/>
          <Basic.TextArea
            ref="groupContainer"
            label={this.i18n(`${locKey}.groupContainer.label`)}
            helpBlock={this.i18n(`${locKey}.groupContainer.help`)}
            onChange={this._onChangeGroupContainer.bind(this)}
            required
            max={255}/>
          <Basic.ToggleSwitch
            ref="membershipSwitch"
            onChange={this._toggleSwitch.bind(this, 'membershipSwitch')}
            label={this.i18n(`${roleSyncLocKey}.membershipSwitch.label`)}
            helpBlock={this.i18n(`${roleSyncLocKey}.membershipSwitch.helpBlock`)}/>
          <Basic.Div style={{display: 'flex', justifyContent: 'start'}}>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                ref="assignCatalogueSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignCatalogueSwitch')}
                label={this.i18n(`${roleSyncLocKey}.assignCatalogueSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignCatalogueSwitch.helpBlock`)}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                level="danger"
                style={{marginLeft: 15}}
                ref="removeCatalogueRoleSwitch"
                onChange={this._toggleSwitch.bind(this, 'removeCatalogueRoleSwitch')}
                readOnly={!assignCatalogueSwitch}
                label={this.i18n(`${roleSyncLocKey}.removeCatalogueRoleSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.removeCatalogueRoleSwitch.helpBlock`)}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Panel rendered={!!assignCatalogueSwitch} style={{backgroundColor: '#d9edf7', borderColor: '#bce8f1', color: '#31708f'}}>
            <Basic.PanelBody>
              <Basic.Div style={{display: 'flex', justifyContent: 'start'}}>
                <Basic.Div style={{flex: 1}}>
                  <Advanced.RoleCatalogueSelect
                    ref="mainRoleCatalog"
                    readOnly={!assignCatalogueSwitch}
                    required={!!removeCatalogueRoleSwitch && !newRoleCatalog}
                    onChange={this._onChangeMainRoleCatalog.bind(this)}
                    label={this.i18n(`${locKey}.mainRoleCatalog.label`)}
                    helpBlock={this.i18n(`${locKey}.mainRoleCatalog.help`)}/>
                </Basic.Div>
                <Basic.Div style={{flex: 1, marginLeft: 15}}>
                  <Basic.TextField
                    ref="newRoleCatalog"
                    readOnly={!assignCatalogueSwitch}
                    required={!!removeCatalogueRoleSwitch && !mainRoleCatalog}
                    label={this.i18n(`${locKey}.newRoleCatalog.label`)}
                    onChange={this._onChangeNewRoleCatalog.bind(this)}
                    helpBlock={this.i18n(`${locKey}.newRoleCatalog.help`)}/>
                </Basic.Div>
              </Basic.Div>
            </Basic.PanelBody>
          </Basic.Panel>
          <Basic.Div style={{display: 'flex', justifyContent: 'start'}}>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                ref="assignRoleSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignRoleSwitch')}
                label={this.i18n(`${roleSyncLocKey}.assignRoleSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignRoleSwitch.helpBlock`)}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                level="danger"
                style={{marginLeft: 15}}
                ref="assignRoleRemoveSwitch"
                onChange={this._toggleSwitch.bind(this, 'assignRoleRemoveSwitch')}
                readOnly={!assignRoleSwitch}
                label={this.i18n(`${roleSyncLocKey}.assignRoleRemoveSwitch.label`)}
                helpBlock={this.i18n(`${roleSyncLocKey}.assignRoleRemoveSwitch.helpBlock`)}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Alert
            title={this.i18n(`${roleSyncLocKey}.assignRoleAndDiffSyncWarning.title`)}
            text={this.i18n(`${roleSyncLocKey}.assignRoleAndDiffSyncWarning.text`)}
            showHtmlText
            rendered={!!assignRoleSwitch}
            level="warning"/>
          <Basic.Div style={{flex: 1}}>
            <Basic.ToggleSwitch
              ref="regenerateSchemaSwitch"
              rendered={!!schemaId}
              onChange={this._toggleSwitch.bind(this, 'regenerateSchemaSwitch')}
              label={this.i18n(`${locKey}.regenerateSchemaSwitch.label`)}
              helpBlock={this.i18n(`${locKey}.regenerateSchemaSwitch.help`)}/>
          </Basic.Div>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}

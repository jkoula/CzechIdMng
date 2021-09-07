// eslint-disable-next-line max-classes-per-file
import {Basic} from 'czechidm-core';
import React from 'react';
import Joi from 'joi';
import DefaultSystemWizard from './DefaultSystemWizard';
import AbstractWizardStep from '../AbstractWizardStep';
import MsSqlAuthenticationTypeEnum from '../../../domain/MsSqlAuthenticationTypeEnum';

/**
 * Wizard for create a system with MS-SQL connector.
 *
 * @author Vít Švanda
 * @since 11.2.0
 */
export default class MsSqlSystemWizard extends DefaultSystemWizard {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.wizardContext = {};
  }

  getWizardId() {
    return 'mssql-connector-type';
  }

  getModule() {
    return 'acc';
  }

  getWizardSteps(props, context) {
    const {match, connectorType} = this.props;
    const steps = super.getWizardSteps(props, context);
    const stepOneId = 'jdbcStepOne';
    // Replace first step.
    steps[0] = {
      id: stepOneId,
      getComponent: () => {
        return (
          <JdbcStepOne
            match={match}
            wizardStepId={stepOneId}
            connectorType={connectorType}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    // Remove schema step.
    steps.splice(1, 2);
    return [...steps];
  }
}

/**
 * First step of JDBC connector - Creates a system, generate schema.
 */
class JdbcStepOne extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      const metadata = this.state.connectorType.metadata;
      if (metadata) {
        this.state.ntlm = metadata.ntlm === 'true';
        this.state.trustServerCertificate = metadata.trustServerCertificate === 'true';
        this.state.authenticationType = metadata.authenticationType;
        this.state.domain = metadata.domain;
        this.state.instanceName = metadata.instanceName;
        this.state.name = metadata.name;
        this.state.port = metadata.port;
        this.state.host = metadata.host;
        this.state.user = metadata.user;
        this.state.database = metadata.database;
        this.state.table = metadata.table;
        this.state.keyColumn = metadata.keyColumn;
      }
      if (!wizardContext.connectorType.reopened) {
        // primary schema attribute will be cleared only for new system (in reopened case is UID attribute not deleted).
        wizardContext.connectorType.metadata.primarySchemaAttributeId = null;
      }
    }
  }

  /**
   * Prepare metadata for next action (send to the BE).
   */
  compileMetadata(_connectorType, formData, system) {
    const metadata = _connectorType.metadata;
    metadata.system = system ? system.id : null;
    metadata.name = formData.name;
    metadata.port = formData.port;
    metadata.host = formData.host;
    metadata.user = formData.user;
    metadata.password = formData.password;
    metadata.database = formData.database;
    metadata.table = formData.table;
    metadata.keyColumn = formData.keyColumn;
    metadata.authenticationType = formData.authenticationType;
    metadata.trustServerCertificate = formData.trustServerCertificate;
    metadata.domain = formData.domain;
    metadata.instanceName = formData.instanceName;
    metadata.ntlm = formData.ntlm;
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  _toggleSwitch(key) {
    const state = {};
    state[key] = !this.state[key];

    this.setState(state);
  }

  _onChangeTextField(key, event) {
    const state = {};
    if (event && event.target) {
      state[key] = event.target.value;
      this.setState(state);
    }
  }

  _onChangeEnumField(key, event) {
    const state = {};
    if (event) {
      state[key] = event.value;
      this.setState(state);
    }
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading,
      ntlm,
      authenticationType,
      trustServerCertificate,
      domain,
      instanceName,
      name,
      port,
      host,
      user,
      database,
      table,
      keyColumn} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.name = name;
      formData.ntlm = ntlm;
      formData.authenticationType = authenticationType;
      formData.trustServerCertificate = trustServerCertificate;
      formData.domain = domain;
      formData.instanceName = instanceName;
      formData.port = port;
      formData.host = host;
      formData.user = user;
      formData.database = database;
      formData.table = table;
      formData.keyColumn = keyColumn;
      if (_connectorType.reopened) {
        // We expecting the password was already filled for reopened system.
        formData.password = '********';
      }
    }
    const locKey = this.getLocKey();
    const jdbcLocKey = 'acc:wizard.jdbc-connector-type.steps.jdbcStepOne';
    const isWindowsAuthentication = authenticationType === 'WINDOWS_AUTHENTICATION';
    const isNtlm = !!ntlm;

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.AbstractForm
          ref="form"
          onSubmit={(event) => {
            this.wizardNext(event);
          }}
          data={formData}>
          <Basic.TextField
            ref="name"
            onChange={this._onChangeTextField.bind(this, 'name')}
            label={this.i18n(`${jdbcLocKey}.systemName`)}
            required
            max={255}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 3, marginRight: 15}}>
              <Basic.TextField
                ref="host"
                onChange={this._onChangeTextField.bind(this, 'host')}
                label={this.i18n(`${jdbcLocKey}.host.label`)}
                helpBlock={this.i18n(`${jdbcLocKey}.host.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="port"
                onChange={this._onChangeTextField.bind(this, 'port')}
                validation={Joi.number().integer().min(0).max(65535)}
                label={this.i18n(`${jdbcLocKey}.port.label`)}
                helpBlock={this.i18n(`${jdbcLocKey}.port.help`)}
                required/>
            </Basic.Div>
          </Basic.Div>
          <Basic.TextField
            ref="database"
            onChange={this._onChangeTextField.bind(this, 'database')}
            label={this.i18n(`${jdbcLocKey}.database.label`)}
            helpBlock={this.i18n(`${jdbcLocKey}.database.help`)}
            required
            max={128}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.TextField
                ref="table"
                onChange={this._onChangeTextField.bind(this, 'table')}
                label={this.i18n(`${jdbcLocKey}.table.label`)}
                helpBlock={this.i18n(`${jdbcLocKey}.table.help`)}
                required
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="keyColumn"
                onChange={this._onChangeTextField.bind(this, 'keyColumn')}
                label={this.i18n(`${jdbcLocKey}.keyColumn.label`)}
                helpBlock={this.i18n(`${jdbcLocKey}.keyColumn.help`)}
                required
                max={128}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.EnumSelectBox
            ref="authenticationType"
            onChange={this._onChangeEnumField.bind(this, 'authenticationType')}
            enum={MsSqlAuthenticationTypeEnum}
            label={this.i18n(`${locKey}.authenticationType.label`)}
            required
            clearable={false}/>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.ToggleSwitch
                ref="ntlm"
                onChange={this._toggleSwitch.bind(this, 'ntlm')}
                level="warning"
                hidden={!isWindowsAuthentication}
                label={this.i18n(`${locKey}.ntlm.label`)}
                helpBlock={this.i18n(`${locKey}.ntlm.help`)}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.ToggleSwitch
                ref="trustServerCertificate"
                level="warning"
                hidden={!isWindowsAuthentication}
                onChange={this._toggleSwitch.bind(this, 'trustServerCertificate')}
                label={this.i18n(`${locKey}.trustServerCertificate.label`)}
                helpBlock={this.i18n(`${locKey}.trustServerCertificate.help`)}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.TextField
                ref="domain"
                onChange={this._onChangeTextField.bind(this, 'domain')}
                label={this.i18n(`${locKey}.domain.label`)}
                max={100}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="instanceName"
                onChange={this._onChangeTextField.bind(this, 'instanceName')}
                label={this.i18n(`${locKey}.instanceName.label`)}
                max={100}/>
            </Basic.Div>
          </Basic.Div>
          <Basic.Div style={{display: 'flex'}}>
            <Basic.Div style={{flex: 1, marginRight: 15}}>
              <Basic.TextField
                ref="user"
                hidden={isWindowsAuthentication && !isNtlm}
                label={this.i18n(`${jdbcLocKey}.user.label`)}
                helpBlock={this.i18n(`${jdbcLocKey}.user.help`)}
                required={!(isWindowsAuthentication && !isNtlm)}
                max={128}/>
            </Basic.Div>
            <Basic.Div style={{flex: 1}}>
              <Basic.TextField
                ref="password"
                hidden={isWindowsAuthentication && !isNtlm}
                pwdAutocomplete={false}
                required={!(isWindowsAuthentication && !isNtlm) && (_connectorType ? !_connectorType.reopened : true)}
                type="password"
                label={this.i18n(`${jdbcLocKey}.password.label`)}
                max={255}/>
            </Basic.Div>
          </Basic.Div>
        </Basic.AbstractForm>
      </Basic.Div>
    );
  }
}

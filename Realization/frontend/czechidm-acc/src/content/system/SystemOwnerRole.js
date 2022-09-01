
import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import {Advanced, Basic, Managers} from 'czechidm-core';
import SystemOwnerRoleManager from './SystemOwnerRoleManager';
import _ from 'lodash';
import SystemManager from '../../redux/SystemManager';
import SystemSelect from '../../components/SystemSelect/SystemSelect';

const manager = new SystemOwnerRoleManager();
const systemManager= new SystemManager();

class SystemOwnerRole extends Advanced.AbstractTableContent {
  constructor(props) {
    super(props);
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return "acc:content.system.owner";
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getNavigationKey() {
    return this.getRequestNavigationKey(
      "system-owner",
      this.props.match.params
    );
  }

  showDetail(entity) {
    console.log("Show detail", entity);
    super.showDetail(entity, () => {
    });
  }
  save = (entity, event) => {
    const formEntity = this.refs.form.getData();
    super.save(formEntity, event);
  };

  render() {
    const { _showLoading, forceSearchParameters } = this.props;
    const { detail } = this.state;
    return (
      <Basic.Div>
      <Basic.Confirm ref="confirm-delete" level="danger" />
      <Advanced.Table
        ref="table"
        manager={manager}
        buttons={[
          <Basic.Button
            level="success"
            key="add_button"
            className="btn-xs"
            onClick={this.showDetail.bind(this,{})}
          >
            <Basic.Icon type="fa" icon="plus" /> {this.i18n("button.add")}
          </Basic.Button>,
        ]}>
        <Advanced.Column
          header=""
          className="detail-button"
          cell={({ rowIndex, data }) => {
            return (
              <Advanced.DetailButton
                title={this.i18n("button.detail")}
                onClick={this.showDetail.bind(this, data[rowIndex])}
              />
            );
          }}
        ></Advanced.Column>
        <Advanced.Column
          property="ownerRole"
          sortProperty="ownerRole.name"
          face="text"
          header={this.i18n("acc:entity.SystemOwnerRole.ownerRole.label")}
          cell={({ rowIndex, data }) => {
            const entity = data[rowIndex];
            console.log(entity, "OBSAH ENTIT");
            return (
              <Advanced.EntityInfo
                entityType="identity"
                entityIdentifier={entity.owner}
                entity={entity._embedded.owner}
                face="popover"
                showIcon
              />
            );
          }}
        />
        <Advanced.Column
          property="type"
          width={125}
          face="text"
          header={this.i18n("acc:entity.SystemOwnerRole.type.label")}
          sort={false}
          cell={({ rowIndex, data, property }) => {
            return (
              <Advanced.CodeListValue
                code="owner-type"
                value={data[rowIndex][property]}
              />
            );
          }}
        />
        <Advanced.Column></Advanced.Column>
      </Advanced.Table>
      <Basic.Modal
        bsSize="large"
        show={this.state.detail.show}
        onHide={this.closeDetail.bind(this)}
        backdrop="static"
        keyboard={!_showLoading}
      >
        <form onSubmit={this.save.bind({}, this)}>
          <Basic.Modal.Header
            closeButton={!_showLoading}
            text={this.i18n("create.header")}
          />
          <Basic.Modal.Header
            closeButton={!_showLoading}
          />
          <Basic.Modal.Body>
            <Basic.AbstractForm
              ref="form"
              showLoading={_showLoading}
            >
              <SystemSelect
                ref="owner"
                manager={systemManager}
                label={this.i18n("acc:entity.SystemOwnerRole.owner.label")}
                readOnly
                required
              />

              <Advanced.RoleSelect
                ref="ownerRole"
                // manager={ Managers.RoleManager }
                // label={this.i18n('acc:entity.RoleSystem.role')}
                // // readOnly={ !isNew || !this._isSystemMenu() }
              />

    
            </Basic.AbstractForm>
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeDetail.bind(this)}
              showLoading={_showLoading}
            >
              {this.i18n("button.close")}
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={_showLoading}
              showLoadingIcon
              showLoadingText={this.i18n("button.saving")}
            >
              {this.i18n("button.save")}
            </Basic.Button>
          </Basic.Modal.Footer>
        </form>
      </Basic.Modal>
    </Basic.Div>
    );
  }
}
function select(state, props) {
  return {
  };
}
export default connect(select)(SystemOwnerRole);

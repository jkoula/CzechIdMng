
import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import {Advanced, Basic, Managers, Utils} from 'czechidm-core';
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
    super.showDetail(entity, () => {
    });
  }
  save = (entity, event) => {
    const formEntity = this.refs.form.getData();
    super.save(formEntity, event);
  };

  render() {
    const { uiKey,_showLoading, forceSearchParameters } = this.props;
    const { detail } = this.state;
    return (
      <Basic.Div>
      <Basic.Confirm ref="confirm-delete" level="danger" />
      <Advanced.Table
        ref="table"
        manager={manager}
        uiKey = {uiKey}
        forceSearchParameters = {forceSearchParameters}
        rowClass={({rowIndex, data}) => {
          const embedded = data[rowIndex]._embedded;
          if (embedded) {
            return Utils.Ui.getRowClass(embedded.systemOwnersRole);
          }
          return '';
        }}
        buttons={[
          <Basic.Button
            level="success"
            key="add_button"
            className="btn-xs"
            onClick={this.showDetail.bind(this,{system: this.props.match.params.entityId})}
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
            if (entity) {
              console.log("entity", entity)
            } else {
              console.log("null entity")
            }
            console.log("entity", entity)
            return (
              <Advanced.EntityInfo
              header={this.i18n("acc:entity.SystemOwnerRole.type.label")}
                entityType="role"
                entityIdentifier={ entity.ownerRole }
                entity={ entity._embedded.ownerRole }
                face="popover"
                showIcon
              />
            );
          }}
        />
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
                ref="system"
                manager={systemManager}
                label={this.i18n("acc:entity.SystemOwnerRole.system.label")}
                readOnly
                required
              />

              <Advanced.RoleSelect
                ref="ownerRole"
                label={this.i18n('acc:entity.SystemOwnerRole.ownerRole.label')}
                helpBlock={this.i18n("acc:entity.SystemOwnerRole.ownerRole.help")}
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

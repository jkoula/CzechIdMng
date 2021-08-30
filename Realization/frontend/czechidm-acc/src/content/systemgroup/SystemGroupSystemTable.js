import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import _ from 'lodash';
//
import { Advanced, Basic, Domain, Managers, Utils } from 'czechidm-core';
import {SystemGroupManager, SystemGroupSystemManager, SystemManager, SystemAttributeMappingManager} from '../../redux';

const manager = new SystemGroupSystemManager();
const systemGroupManager = new SystemGroupManager();
const systemManager = new SystemManager();
const systemAttributeMappingManager = new SystemAttributeMappingManager();

/**
 * Table of system groups-system relations.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
class SystemGroupSystemTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false
      }
    };
  }

  getContentKey() {
    return 'acc:content.systemGroupSystem';
  }

  getManager() {
    return manager;
  }

  getDefaultSearchParameters() {
    return this.getManager().getDefaultSearchParameters();
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    const {entityId} = this.props.match.params;
    // Load a group.
    this.context.store.dispatch(systemGroupManager.fetchEntity(entityId));

    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    } else {
      entity.systemGroup = entityId;
    }
    //
    super.showDetail(entity, () => {
      if (this.refs.system) {
        this.refs.system.focus();
      }
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({message: this.i18n('save.success', {count: 1, record: this.getManager().getNiceLabel(entity)})});
    }
    //
    super.afterSave(entity, error);
  }

  _onChangeSystem(value) {
    this.setState({_systemId: value ? value.id : null}, () => {
      if (this.refs.mergeAttribute) {
        this.refs.mergeAttribute.setValue(null);
      }
    });
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      className,
      _permissions,
      _systemGroup
    } = this.props;

    const {
      showLoading,
      detail,
      _showLoading,
      _systemId
    } = this.state;

    let systemId = detail.entity && detail.entity.system ? detail.entity.system : Domain.SearchParameters.BLANK_UUID;
    if (_systemId) {
      systemId = _systemId;
    }

    const type = _systemGroup ? _systemGroup.type : '';
    const forceSearchParametersMergeAttribute = new Domain.SearchParameters()
      .setFilter('systemId', systemId)
      .setFilter('operationType', 'PROVISIONING')
      .setFilter('strategyType', 'MERGE')
      .setFilter('entityType', 'IDENTITY');

    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Col lg={12}>
            <Basic.Confirm ref="confirm-delete" level="danger"/>

            <Advanced.Table
              ref="table"
              uiKey={uiKey}
              manager={this.getManager()}
              rowClass={({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex])}
              filterOpened
              forceSearchParameters={forceSearchParameters}
              showRowSelection
              showLoading={showLoading}
              buttons={[
                <span>
                  <Basic.Button
                    level="success"
                    key="add_button"
                    className="btn-xs"
                    onClick={this.showDetail.bind(this, {})}
                    rendered={Managers.SecurityManager.hasAuthority('SYSTEM_GROUP_CREATE')}
                    icon="fa:plus">
                    {this.i18n('button.add')}
                  </Basic.Button>
                </span>
              ]}
              _searchParameters={this.getSearchParameters()}
              className={className}>

              <Advanced.Column
                header=""
                className="detail-button"
                cell={
                  ({rowIndex, data}) => {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                sort={false}/>

              <Advanced.Column
                property="system"
                sortProperty="system.name"
                face="text"
                header={this.i18n('acc:entity.SystemGroupSystem.system')}
                sort
                cell={
                  ({rowIndex, data}) => {
                    const entity = data[rowIndex];
                    return (
                      <Advanced.EntityInfo
                        entityType="system"
                        entityIdentifier={entity.system}
                        entity={entity._embedded.system}
                        face="popover"
                        showIcon/>
                    );
                  }
                }
                rendered={_.includes(columns, 'system')}/>
            </Advanced.Table>
          </Basic.Col>
        </Basic.Row>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>
          <Basic.Modal.Header
            icon="fa:list-alt"
            closeButton={!_showLoading}
            text={this.i18n('create.header')}
            rendered={Utils.Entity.isNew(detail.entity)}/>
          <Basic.Modal.Header
            icon="fa:list-alt"
            closeButton={!_showLoading}
            text={this.i18n('edit.header', {name: this.getManager().getNiceLabel(detail.entity)})}
            rendered={!Utils.Entity.isNew(detail.entity)}/>
          <Basic.Modal.Body>
            <Basic.AbstractForm
              ref="form"
              onSubmit={(event) => {
                this.save({}, event);
              }}
              data={detail.entity}
              showLoading={_showLoading}
              readOnly={!this.getManager().canSave(detail.entity, _permissions)}>
              <Basic.SelectBox
                ref="systemGroup"
                manager={systemGroupManager}
                clearable={false}
                label={this.i18n('acc:entity.SystemGroupSystem.systemGroup')}
                required
                readOnly/>
              <Basic.SelectBox
                ref="system"
                onChange={this._onChangeSystem.bind(this)}
                manager={systemManager}
                label={this.i18n('acc:entity.SystemGroupSystem.system')}
                required/>
              <Basic.SelectBox
                ref="mergeAttribute"
                forceSearchParameters={forceSearchParametersMergeAttribute}
                hidden={type !== 'CROSS_DOMAIN'}
                manager={systemAttributeMappingManager}
                clearable={false}
                label={this.i18n('acc:entity.SystemGroupSystem.mergeAttribute')}
                required/>
            </Basic.AbstractForm>
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeDetail.bind(this)}
              showLoading={_showLoading}>
              {this.i18n('button.close')}
            </Basic.Button>
            <Basic.Button
              level="success"
              onClick={this.save.bind(this, {})}
              rendered={manager.canSave(detail.entity, _permissions)}
              showLoading={_showLoading}
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

SystemGroupSystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Css
   */
  className: PropTypes.string
};

SystemGroupSystemTable.defaultProps = {
  columns: ['system'],
  forceSearchParameters: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    _systemGroup: systemGroupManager.getEntity(state, entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, {forwardRef: true})(SystemGroupSystemTable);

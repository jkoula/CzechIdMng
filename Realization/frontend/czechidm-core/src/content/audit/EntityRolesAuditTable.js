import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AuditManager } from '../../redux';
import AuditModificationEnum from '../../enums/AuditModificationEnum';

const auditManager = new AuditManager();

/**
* Table of assigned roles for entity (identity / role).
*
* @author Radek Tomiška
* @since 12.0.0
*/
export class EntityRolesAuditTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.audit.identityRoles';
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
    if (this.refs.table !== undefined) {
      this.refs.table.cancelFilter(this.refs.filterForm);
    }
  }

  /**
  * Method for show detail of revision, redirect to detail.
  *
  * @param entityId id of revision
  */
  showDetail(entityId) {
    this.context.history.push(`/audit/entities/${ entityId }/diff/`);
  }

  render() {
    const { uiKey, forceSearchParameters } = this.props;
    //
    // TODO: add relatedOwnerType ~ support other entities than role / identity
    const showRole = !forceSearchParameters.getFilters().has('relatedOwnerId');
    const showIdentity = !forceSearchParameters.getFilters().has('ownerId');
    //
    return (
      <div>
        <Advanced.Table
          ref="table"
          filterOpened
          uiKey={ uiKey }
          manager={ auditManager }
          forceSearchParameters={ forceSearchParameters }
          rowClass={ ({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); } }
          showId
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.FilterDate ref="fromTill"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="modification"
                      placeholder={ this.i18n('entity.Audit.modification') }
                      enum={ AuditModificationEnum }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="modifier"
                      placeholder={ this.i18n('entity.Audit.modifier') }
                      help={ this.i18n('content.audit.filter.modifier.help') }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 } rendered={ showIdentity }>
                    <Advanced.Filter.TextField
                      ref="ownerCode"
                      placeholder={ this.i18n('entity.Identity._type') }
                      help={ this.i18n('content.audit.filter.identity.help') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } rendered={ showRole }>
                    <Advanced.Filter.RoleSelect
                      ref="subOwnerId"
                      label={ null }
                      placeholder={ this.i18n('content.audit.identityRoles.role') }
                      returnProperty="id"/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex].id) }/>
                );
              }
            }
            sort={ false }/>
          <Advanced.Column
            header={ this.i18n('modification') }
            property="modification"
            width={ 100 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Basic.Label
                    level={ AuditModificationEnum.getLevel(data[rowIndex][property]) }
                    text={ this.i18n(`content.audit.identityRoles.modification.${ data[rowIndex][property] }`) }/>
                );
              }
            }/>
          <Advanced.Column
            property="modifier"
            width={ 50 }
            face="text"/>
          <Advanced.Column
            property="timestamp"
            header={this.i18n('entity.Audit.revisionDate')}
            sort
            width={ 150 }
            face="datetime"/>
          <Advanced.Column
            property="entityId"
            header={ this.i18n('entity.Audit.entity') }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const value = data[rowIndex][property];
                //
                if (data[rowIndex]._embedded && data[rowIndex]._embedded[property]) {
                  return (
                    <Advanced.EntityInfo
                      entityType={ Utils.Ui.getSimpleJavaType(data[rowIndex].type) }
                      entityIdentifier={ value }
                      entity={ data[rowIndex]._embedded[property] }
                      face="popover"
                      showEntityType={ false }
                      showLink={ !data[rowIndex].deleted }
                      deleted={ data[rowIndex].deleted }
                      showIcon/>
                  );
                }
                //
                return (
                  <Advanced.UuidInfo value={ value } />
                );
              }
            }/>
          <Advanced.Column
            property="changedAttributes"
            width={ 100 }
            cell={
              ({ rowIndex, data, property }) => {
                if (data[rowIndex].modification === 'MOD') {
                  return _.replace(data[rowIndex][property], ',', ', ');
                }
                return null;
              }
            }/>
          <Advanced.Column
            header={this.i18n('entity.Identity._type')}
            property="identity"
            rendered={ showIdentity }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                const embeddedEntity = data[rowIndex]._embedded;
                let identity = {
                  id: entity.ownerId,
                  username: entity.ownerCode
                };
                if (embeddedEntity) {
                  identity = data[rowIndex]._embedded.ownerId;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ entity.ownerId }
                    entity={identity}
                    face="popover"
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            header={ this.i18n('entity.IdentityRole.role') }
            sortProperty="role.name"
            rendered={ showRole }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                //
                const role = data[rowIndex]._embedded.subOwnerId;
                //
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.role }
                    face="popover"
                    entity={ role }
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            header={ this.i18n('entity.IdentityRole.identityContract.title') }
            property="identityContract"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                // For deleted operation is not filled embedded
                const embeddedEntity = data[rowIndex]._embedded.entityId;
                let identityContract = null;
                if (embeddedEntity && embeddedEntity._embedded.identityContract) {
                  identityContract = embeddedEntity._embedded.identityContract;
                } else if (embeddedEntity && embeddedEntity._embedded.owner && embeddedEntity._embedded.owner._embedded) {
                  identityContract = embeddedEntity._embedded.owner._embedded.identityContract;
                }
                if (!embeddedEntity && !identityContract) {
                  identityContract = data[rowIndex]._embedded.identityContract;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="identityContract"
                    entityIdentifier={ entity.identityContract }
                    entity={ identityContract }
                    face="popover"
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            header={ this.i18n('entity.IdentityRole.contractPosition.label') }
            property="contractPosition"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="contractPosition"
                    entityIdentifier={ entity.contractPosition }
                    showIdentity={ false }
                    face="popover" />
                );
              }
            }/>
          <Advanced.Column
            property="entity"
            header={this.i18n('label.validFrom')}
            face="date"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                return entity.validFrom;
              }
            }/>
          <Advanced.Column
            property="entity"
            header={this.i18n('label.validTill')}
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                return entity.validTill;
              }
            }
            face="date"/>
          <Advanced.Column
            property="directRole"
            header={this.i18n('entity.IdentityRole.directRole.label')}
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="identityRole"
                    entityIdentifier={ entity.directRole }
                    showIdentity={ false }
                    face="popover" />
                );
              }
            }
            width={ 150 }/>

          <Advanced.Column
            property="entity"
            header={ <Basic.Icon value="component:automatic-role"/> }
            title={ this.i18n('entity.IdentityRole.automaticRole.help') }
            face="bool"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex].entity;
                if (!entity) {
                  return null;
                }
                return (
                  <Basic.BooleanCell propertyValue={ entity.automaticRole !== null } className="column-face-bool"/>
                );
              }
            }
            width={ 15 }/>
        </Advanced.Table>
      </div>
    );
  }
}

EntityRolesAuditTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Selected entity force search parameters.
   */
  forceSearchParameters: PropTypes.object.isRequired
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(EntityRolesAuditTable);

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
* Audit for selected entity / owner. Used on entity detail.
*
* @author Radek Tomiška
* @since 12.0.0
*/
export class EntityAuditTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.audit';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(auditManager.fetchAuditedEntitiesNames());
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
  * Method for show detail of revision, redirect to detail
  *
  * @param entityId id of revision
  */
  showDetail(entityId) {
    this.context.history.push(`/audit/entities/${ entityId }/diff/`);
  }

  _getNiceLabelForOwner(ownerType, ownerCode) {
    if (ownerCode && ownerCode !== null && ownerCode !== 'null') {
      return ownerCode;
    }
    return '';
  }

  render() {
    const { uiKey, auditedEntities, forceSearchParameters } = this.props;
    //
    return (
      <div>
        <Advanced.Table
          ref="table"
          filterOpened
          uiKey={ uiKey }
          manager={ auditManager }
          forceSearchParameters={ forceSearchParameters }
          rowClass={({ rowIndex, data }) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
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
                      ref="type"
                      searchable
                      placeholder={ this.i18n('entity.Audit.type') }
                      options={ auditedEntities }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      className="pull-right"
                      ref="modifier"
                      placeholder={ this.i18n('content.audit.identities.modifier') }
                      help={ this.i18n('content.audit.filter.modifier.help') }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 12 } >
                    <Advanced.Filter.CreatableSelectBox
                      ref="changedAttributesList"
                      placeholder={ this.i18n('entity.Audit.changedAttributes.placeholder') }
                      tooltip={ this.i18n('entity.Audit.changedAttributes.tooltip') }/>
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
            property="type"
            width={ 200 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <span title={ data[rowIndex][property] }>
                    { Utils.Ui.getSimpleJavaType(data[rowIndex][property]) }
                  </span>
                );
              }
            }/>
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
                      face="popover"
                      entity={ data[rowIndex]._embedded[property] }
                      showEntityType={ false }
                      showIcon/>
                  );
                }
                if (data[rowIndex].revisionValues) {
                  return (
                    <Advanced.EntityInfo
                      entityType={ Utils.Ui.getSimpleJavaType(data[rowIndex].type) }
                      entityIdentifier={ value }
                      entity={ data[rowIndex].revisionValues }
                      face="popover"
                      showLink={ false }
                      showEntityType={ false }
                      showIcon
                      deleted/>
                  );
                }
                //
                return (
                  <Advanced.UuidInfo value={ value } />
                );
              }
            }/>
          <Advanced.Column
            property="subOwnerCode"
            face="text"
            cell={
              ({ rowIndex, data }) => {
                return this._getNiceLabelForOwner(data[rowIndex].subOwnerType, data[rowIndex].subOwnerCode);
              }
            }
          />
          <Advanced.Column
            property="modification"
            width={ 100 }
            sort
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Basic.Label
                    level={ AuditModificationEnum.getLevel(data[rowIndex][property]) }
                    text={ AuditModificationEnum.getNiceLabel(data[rowIndex][property]) }/>
                );
              }
            }/>
          <Advanced.Column property="modifier" sort face="text"/>
          <Advanced.Column property="timestamp" header={this.i18n('entity.Audit.revisionDate')} sort face="datetime"/>
          <Advanced.Column
            hidden
            property="changedAttributes"
            cell={
              ({ rowIndex, data, property }) => {
                return _.replace(data[rowIndex][property], ',', ', ');
              }
            }
          />
        </Advanced.Table>
      </div>
    );
  }
}

EntityAuditTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Selected entity force search parameters.
   */
  forceSearchParameters: PropTypes.object.isRequired
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    auditedEntities: auditManager.prepareOptionsFromAuditedEntitiesNames(auditManager.getAuditedEntitiesNames(state))
  };
}

export default connect(select)(EntityAuditTable);

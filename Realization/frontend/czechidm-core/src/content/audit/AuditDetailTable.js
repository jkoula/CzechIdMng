import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { AuditManager } from '../../redux';

const auditManager = new AuditManager();

const MOD_ADD = 'ADD';

/**
* Table for audit detail revision values.
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
export class AuditDetailTable extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  /**
   * Method for show detail of revision, redirect to detail
   *
   * @param entityId id of revision
   */
  showDetail(entityId) {
    this.context.history.push(`/audit/entities/${ entityId }`);
  }

  _getForceSearchParameters() {
    const { entityId, entityClass } = this.props;

    if (entityId !== undefined || entityClass !== undefined) {
      return auditManager.getDefaultSearchParameters().setFilter('type', entityClass).setFilter('entityId', entityId);
    }
    return null;
  }

  _prepareData(revision) {
    const revisionValues = revision.revisionValues;
    const transformData = [];
    //
    let index = 0;
    for (const key in revisionValues) {
      if (revisionValues.hasOwnProperty(key)) {
        if (revisionValues[key] instanceof Object
          && !(key === 'setting' && revision.type === 'eu.bcvsolutions.idm.core.model.entity.IdmProfile')) { // FIXME: how to work with big hash maps?
          for (const keySec in revisionValues[key]) {
            if (revisionValues[key].hasOwnProperty(keySec)) {
              const row = {
                key: keySec,
                value: revisionValues[key][keySec]
              };
              transformData[index] = row;

              index += 1;
            }
          }
        } else {
          const row = {
            key,
            value: revisionValues[key]
          };
          transformData[index] = row;

          index += 1;
        }
      }
    }
    return transformData;
  }

  render() {
    const { detail, diffValues, diffRowClass, showLoading } = this.props;
    if (detail === null || detail.revisionValues === null) {
      return null;
    }
    //
    // transform revision values for table, key=>value
    const transformData = this._prepareData(detail);
    //
    return (
      <Basic.Table
        showLoading={ showLoading }
        className={ !transformData || transformData.length === 0 ? 'no-margin' : '' }
        data={ transformData }
        noData={ detail.modification === MOD_ADD ? this.i18n('revision.created') : this.i18n('revision.deleted') }
        rowClass={({ rowIndex, data }) => {
          if (diffValues && diffValues[data[rowIndex].key] !== undefined) {
            return diffRowClass;
          }
          return null;
        }}>
        <Basic.Column
          property="key"
          header={ this.i18n('entity.Audit.key') }/>
        <Basic.Column
          property="value"
          header={ this.i18n('entity.Audit.value') }
          cell={
            ({ data, rowIndex }) => {
              const rowData = data[rowIndex];
              const propertyName = rowData.key;
              const propertyValue = rowData.value;
              //
              if (propertyValue === null) {
                return Utils.Ui.toStringValue(null);
              }
              //
              // confidential value decorator - confidential value is randomm uuid - show asterix instead
              if (((propertyName === 'value' && detail.type === 'eu.bcvsolutions.idm.core.model.entity.IdmConfiguration') // => configuration property
                || ((propertyName === 'stringValue' || propertyName === 'shortTextValue') && detail.revisionValues.persistentType)) // => eav
                && detail.revisionValues.confidential === true) { // => is confidential value
                return (
                  <span>********</span>
                );
              }
              //
              // reserved audit constants
              if ((propertyName === 'modifier'
                    || propertyName === 'creator'
                    || propertyName === 'originalModifier'
                    || propertyName === 'originalCreator')
                  && propertyValue !== '[SYSTEM]'
                  && propertyValue !== '[GUEST]') {
                return (
                  <Advanced.EntityInfo entityType="identity" entityIdentifier={ propertyValue } face="popover" />
                );
              }
              if (Advanced.EntityInfo.getComponent(propertyName)) {
                return (
                  <Advanced.EntityInfo entityType={ propertyName } entityIdentifier={ propertyValue } face="popover" />
                );
              }
              return (
                <span>{ Utils.Ui.toStringValue(propertyValue) }</span>
              );
            }
          }/>
      </Basic.Table>
    );
  }
}

AuditDetailTable.propTypes = {
  // columns for display, check default props.
  columns: PropTypes.arrayOf(PropTypes.string),
  // detail for audit
  detail: PropTypes.object,
  // diff value for check
  diffValues: PropTypes.object,
  // Class for row when diffValues contains key
  diffRowClass: PropTypes.string,
  showLoading: PropTypes.bool
};

AuditDetailTable.defaultProps = {
  columns: ['id', 'type', 'modification', 'modifier', 'revisionDate', 'changedAttributes'],
  diffRowClass: 'warning'
};

function select() {
  return {
  };
}

export default connect(select)(AuditDetailTable);

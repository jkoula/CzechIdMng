import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import moment from 'moment';
import _ from 'lodash';
//
import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
import { ProvisioningOperationManager, ProvisioningArchiveManager, SystemManager } from '../../redux';
import ProvisioningOperationTableComponent, { ProvisioningOperationTable } from './ProvisioningOperationTable';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
//
const manager = new ProvisioningOperationManager();
const archiveManager = new ProvisioningArchiveManager();
const systemManager = new SystemManager();

/**
 * Active and archived provisioning operations
 *
 * @author Radek Tomiška
 * @author Ondrej Husnik
 */
class ProvisioningOperations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: null,
        activeKey: 1
      }
    };
  }

  getManager() {
    return manager;
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  /**
   * Shows modal detail with given entity
   */
  showDetail(entity, isArchive) {
    if (entity && entity.id) {
      this.__obtainProvisioningDifferences(entity.id, isArchive);
    }

    this.setState({
      detail: {
        show: true,
        entity,
        isArchive
      },
      showChangesOnly: true
    });
  }

  /**
   * Close modal detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: null
      }
    });
  }

  _onChangeSelectTabs(activeKey) {
    this.setState({
      activeKey
    });
  }

  /**
   * Transforma account or connector object value into FE property values
   *
   * @param  {object} objectValue
   * @return {string}
   */
  _toPropertyValue(objectValue) {
    return Utils.Ui.toStringValue(objectValue);
  }

  /**
   * Switching ALL values vs CHANGES ONLY of the displayed attributes in the detail table.
   * @return {[type]} [description]
   */
  _toggleShowChangesOnly() {
    this.setState({
      showChangesOnly: !this.refs.switchShowChangesOnly.getValue()
    });
  }

  /**
   * Rearranges data to suitable form used in following functions.
   * It also solves siuations when data to display is in connectorObject only
   * and is missing in other in accountObject e.g. __PASSWORD__ provisioning.
   *
   * @return {object} Composition of data to display.
   */
  _reorganizeTableData(detail) {
    const result = [];
    const accountObject = detail.entity.provisioningContext.accountObject; // values in IdM
    const diffData = this.state.provisioningDifferences;

    // prepare attribute name and strategy
    if (accountObject) {
      for (const schemaAttributeId in accountObject) {
        if (!{}.hasOwnProperty.call(accountObject, schemaAttributeId)) {
          continue;
        }
        const strategyStr = this._extractStrategy(schemaAttributeId);
        const attrName = this._extractAttrName(schemaAttributeId, strategyStr);
        result.push({
          property: attrName,
          strategy: strategyStr
        });
      }
    }

    // create difference view based on dedicated diff object
    if (diffData && diffData.length > 0) {
      for (const item of diffData) {
        const name = item.name;
        const multivalue = item.multivalue;
        const changed = item.changed;
        const index = result.findIndex(attr => { return attr.property === name; });
        if (index < 0) {
          continue;
        }
        result[index].multivalue = multivalue;
        result[index].changed = changed;

        if (multivalue) {
          result[index].accountVal = [];
          result[index].systemVal = [];
          result[index].valueState = [];
          for (const value of item.values) {
            result[index].accountVal.push(value.value == null ? '' : this._toPropertyValue(value.value));
            result[index].systemVal.push(value.oldValue == null ? '' : this._toPropertyValue(value.oldValue));
            result[index].valueState.push(value.change == null ? '' : this._toPropertyValue(value.change));
          }
        } else {
          result[index].accountVal = item.value.value == null ? '' : this._toPropertyValue(item.value.value);
          result[index].systemVal = item.value.oldValue == null ? '' : this._toPropertyValue(item.value.oldValue);
          result[index].valueState = item.value.change == null ? '' : this._toPropertyValue(item.value.change);
        }
      }
      return result;
    }
    // fallback solution in case of multiple records in the batch of the provisioning queue
    // then there is missing dedicated diff object
    if (accountObject) {
      for (const schemaAttributeId in accountObject) {
        if (!{}.hasOwnProperty.call(accountObject, schemaAttributeId)) {
          continue;
        }
        const strategyStr = this._extractStrategy(schemaAttributeId);
        const attrName = this._extractAttrName(schemaAttributeId, strategyStr);
        const multivalue = Array.isArray(accountObject[schemaAttributeId]);

        let value;
        if (multivalue) {
          value = [];
          for (const variable of accountObject[schemaAttributeId]) {
            value.push(variable == null ? '' : this._toPropertyValue(variable));
          }
        } else {
          value = accountObject[schemaAttributeId] == null ? '' : this._toPropertyValue(accountObject[schemaAttributeId]);
        }

        const valueState = multivalue ? Array(value.length).fill(null) : null;
        const index = result.findIndex(item => { return item.property === attrName; });
        if (index < 0) {
          result.push({
            property: attrName,
            strategy: strategyStr,
            accountVal: value,
            multivalue,
            valueState
          });
        } else {
          result[index].accountVal = value;
          result[index].systemVal = '';
          result[index].valueState = valueState;
          result[index].multivalue = multivalue;
        }
      }
    }
    return result;
  }

  __obtainProvisioningDifferences(id, isArchive) {
    let selectedManager;
    if (isArchive) {
      selectedManager = archiveManager;
    } else {
      selectedManager = manager;
    }
    selectedManager.getService().getDecoratedDifferenceObject(id)
      .then(json => {
        this.setState({provisioningDifferences: json});
      });
  }

  /**
   * Aggregate and sort detail table data
   * @param {object} detail
   * @returns
   */
  _prepareProvisioningDetail(detail) {
    if (!detail.entity) {
      return null;
    }
    let resultContent = this._reorganizeTableData(detail);

    // filter out unchanged values if set so
    if (this.state.showChangesOnly) {
      resultContent = _.filter(resultContent, item => {
        return item.changed;
      });
    }

    // filter out unchanged individual values of a multivalue attribute
    if (this.state.showChangesOnly) {
      for (const attr of resultContent) {
        if (attr.multivalue && Array.isArray(attr.valueState)) {
          const toRemove = [];
          for (let idx = 0; idx < attr.valueState.length; ++idx) {
            if (!attr.valueState[idx]) {
              toRemove.push(idx);
            }
          }
          for (const idxRem of toRemove) {
            attr.accountVal.splice(idxRem, 1);
            attr.systemVal.splice(idxRem, 1);
            attr.valueState.splice(idxRem, 1);
          }
        }
      }
    }

    // sort by name
    resultContent.sort((lItem, rItem) => {
      const l = lItem.property;
      const r = rItem.property;
      if (l >= r) {
        if (l > r) {
          return 1;
        }
        return 0;
      }
      return -1;
    });
    // order changed attributes at the beginning
    resultContent.sort((lItem, rItem) => {
      const l = lItem.changed;
      const r = rItem.changed;
      if (l && !r) {
        return -1;
      }
      if (r && !l) {
        return 1;
      }
      return 0;
    });
    return resultContent;
  }

  /**
   * Extracts the strategy part from the composed name
   * @returns
   */
  _extractStrategy(text) {
    const strategyArr = text.match(/\s*\([A-Z_]{3,}\)[^()]*$/);
    if (strategyArr == null || strategyArr.length === 0) {
      return '';
    }
    return strategyArr[strategyArr.length - 1].trim();
  }

  /**
   * Extracts the part with attribute name from composed name
   * @returns
   */
  _extractAttrName(text, strategy) {
    let strategyStr = strategy;
    if (strategy == null) {
      strategyStr = this._extractStrategy(text);
    }
    return text.replace(strategyStr, '').trim();
  }

  /**
   * Generator of the column label with help
   *
   * @param {*} headerDesc
   * @param {*} helpText
   * @returns
   */
  _renderColumnHelp(headerDesc, helpText) {
    return (
      <div>
        <span> {headerDesc} </span>
        <Basic.Popover
          ref="popover"
          trigger={['click', 'hover']}
          value={ helpText }
          className="abstract-entity-info-popover">
          <span>
            <Basic.Icon level="success" icon="question-sign" />
          </span>
        </Basic.Popover>
      </div>
    );
  }

  /**
  * Turns attribute state into decoration style
  *
  * @param  {[type]} key value change state
  * @return {[type]}     Style string representation
  */
  _getValueStateDecoration(key) {
    if (!key) {
      return null;
    }
    switch (key) {
      case 'ADDED': {
        return 'success';
      }
      case 'UPDATED': {
        return 'warning';
      }
      case 'REMOVED': {
        return 'danger';
      }
      default: {
        return 'default';
      }
    }
  }

  /**
   * Highlights the content of cells of attributes to change
   * @param {}
   * @returns
   */
  _highlightCellContent(isArchive, {rowIndex, data, property}) {
    const row = data[rowIndex];
    const decorate = property === 'accountVal';
    const titleKey = isArchive ? 'ProvisioningArchive' : 'ProvisioningOperation';

    if (row.multivalue) {
      const values = row[property];
      const valueStates = row.valueState;
      const result = [];
      if (!values || !valueStates) {
        return null;
      }
      for (let idx = 0; idx < values.length; ++idx) {
        if (valueStates[idx] && decorate) {
          result.push(<Basic.Label
            title={this.i18n(`acc:entity.${titleKey}.attributeDiffType.${valueStates[idx]}`)}
            level={this._getValueStateDecoration(valueStates[idx])}
            style={valueStates[idx] === 'REMOVED' ? {textDecoration: 'line-through'} : null}
            text={values[idx] || ' '}/>);
        } else {
          result.push((`${values[idx]}`));
        }
        if (idx < values.length - 1) {
          result.push(' ');
        }
      }
      return result;
    }
    const value = row[property];
    const valueState = row.valueState;
    const level = this._getValueStateDecoration(valueState);
    if (row.changed && decorate) {
      return (
        <Basic.Label
          title={this.i18n(`acc:entity.${titleKey}.attributeDiffType.${valueState}`)}
          level={level}
          style={valueState === 'REMOVED' ? {textDecoration: 'line-through'} : null}
          text={value || ' '}/>);
    }
    return (`${value}`);
  }

  /**
   * Formats attribute name and its strategy
   *
   * @param  {}
   * @return {}
   */
  _formatAttributeName({rowIndex, data}) {
    return (<span>{data[rowIndex].property} <small>{data[rowIndex].strategy}</small></span>);
  }

  render() {
    const { forceSearchParameters, columns, uiKey, showDeleteAllButton } = this.props;
    const { detail, activeKey, showChangesOnly} = this.state;
    const isArchive = detail && detail.isArchive ? detail.isArchive : false;

    const tableContent = this._prepareProvisioningDetail(detail);
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />

        <Basic.Tabs activeKey={ activeKey } onSelect={ this._onChangeSelectTabs.bind(this) }>
          <Basic.Tab
            eventKey={ 1 }
            title={ this.i18n('tabs.active.label') }
            rendered={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGOPERATION_READ']) }>
            <ProvisioningOperationTableComponent
              ref="table"
              key="table"
              uiKey={ uiKey }
              manager={ manager }
              isArchive={ false }
              showDetail={ this.showDetail.bind(this) }
              showRowSelection={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGOPERATION_UPDATE']) }
              forceSearchParameters={ forceSearchParameters }
              columns={ columns }
              showDeleteAllButton={ showDeleteAllButton }/>
          </Basic.Tab>

          <Basic.Tab
            eventKey={ 2 }
            title={ this.i18n('tabs.archive.label') }
            rendered={ Managers.SecurityManager.hasAnyAuthority(['PROVISIONINGARCHIVE_READ']) }>
            <ProvisioningOperationTableComponent
              ref="archiveTable"
              key="archiveTable"
              uiKey={ `archive-${ uiKey }` }
              manager={ archiveManager }
              isArchive
              showDetail={ this.showDetail.bind(this) }
              forceSearchParameters={ forceSearchParameters }
              columns={ columns }/>
          </Basic.Tab>
        </Basic.Tabs>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static">
          <Basic.Modal.Header closeButton text={this.i18n('detail.header')}/>
          <Basic.Modal.Body>
            {
              !detail.entity
              ||
              <div>
                <Basic.AbstractForm data={ detail.entity } readOnly>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.LabelWrapper
                        label={ this.i18n('acc:entity.ProvisioningOperation.created.label') }
                        helpBlock={ this.i18n('acc:entity.ProvisioningOperation.created.help') }>
                        <div style={{ margin: '7px 0' }}>
                          <Advanced.DateValue value={ detail.entity.created } showTime/>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper
                        label={
                          detail.isArchive === true
                          ?
                          this.i18n('acc:entity.ProvisioningArchive.modified.label')
                          :
                          this.i18n('acc:entity.ProvisioningOperation.modified.label')
                        }
                        helpBlock={
                          detail.isArchive === true
                          ?
                          this.i18n('acc:entity.ProvisioningArchive.modified.help')
                          :
                          this.i18n('acc:entity.ProvisioningOperation.modified.help')
                        }>
                        <div style={{ margin: '7px 0' }}>
                          <Advanced.DateValue value={ detail.entity.modified } showTime/>
                        </div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.EnumLabel
                        ref="operationType"
                        label={ this.i18n('acc:entity.ProvisioningOperation.operationType') }
                        enum={ ProvisioningOperationTypeEnum }/>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.EnumLabel ref="entityType" label={ this.i18n('acc:entity.SystemEntity.entityType') } enum={ SystemEntityTypeEnum }/>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.ProvisioningOperation.entity') }>
                        {
                          !detail.entity.entityIdentifier
                          ?
                          <span>N/A</span>
                          :
                          <Advanced.EntityInfo
                            entityType={ detail.entity.entityType }
                            entityIdentifier={ detail.entity.entityIdentifier }
                            style={{ margin: 0 }}
                            face="popover"
                            showIcon/>
                        }
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                  <Basic.Row>
                    <Basic.Col lg={ 4 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.System.name') }>
                        <Advanced.EntityInfo
                          entityType="system"
                          entityIdentifier={ detail.entity.system }
                          entity={ detail.entity._embedded.system }
                          style={{ margin: 0 }}
                          face="popover"/>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                    <Basic.Col lg={ 8 }>
                      <Basic.LabelWrapper label={ this.i18n('acc:entity.SystemEntity.uid') }>
                        <Basic.Div style={{ margin: '7px 0' }} rendered={ detail.isArchive }>
                          { detail.entity.systemEntityUid }
                        </Basic.Div>
                        <Basic.Div style={{ margin: '7px 0' }} rendered={ !detail.isArchive && detail.entity._embedded.systemEntity }>
                          { detail.entity._embedded.systemEntity ? detail.entity._embedded.systemEntity.uid : null }
                        </Basic.Div>
                        <Basic.Div style={{ margin: '7px 0' }} rendered={ !detail.isArchive && !detail.entity._embedded.systemEntity }>
                          <Basic.Alert
                            level="error"
                            title={ this.i18n('acc:error.SYSTEM_ENTITY_NOT_FOUND.title') }
                            text={
                              this.i18n('acc:error.SYSTEM_ENTITY_NOT_FOUND.message', {
                                system: systemManager.getNiceLabel(detail.entity._embedded.system)
                              })
                            }/>
                        </Basic.Div>
                      </Basic.LabelWrapper>
                    </Basic.Col>
                  </Basic.Row>

                </Basic.AbstractForm>

                <Advanced.OperationResult value={ detail.entity.result } face="full"/>

                { // look out - archive doesn't have batch
                  (!detail.entity._embedded || !detail.entity._embedded.batch || !detail.entity._embedded.batch.nextAttempt)
                  ||
                  <div style={{ marginBottom: 15 }}>
                    <Basic.ContentHeader text={ this.i18n('detail.nextAttempt.header') }/>
                    {
                      this.i18n('detail.nextAttempt.label', {
                        escape: false,
                        currentAttempt: detail.entity.currentAttempt,
                        maxAttempts: detail.entity.maxAttempts,
                        nextAttempt: moment(detail.entity._embedded.batch.nextAttempt).format(this.i18n('format.datetime'))
                      })
                    }
                  </div>
                }
                <Basic.ToggleSwitch
                  ref="switchShowChangesOnly"
                  label={this.i18n('detail.switchShowChangesOnly.label')}
                  onChange={this._toggleShowChangesOnly.bind(this)}
                  value={showChangesOnly}
                />
                <Basic.Row>
                  <Basic.Col lg={ 12 }>
                    <Basic.Table
                      data={ tableContent }
                      noData={ this.i18n('component.basic.Table.noData') }
                      className="table-bordered"
                      rowClass={({rowIndex, data}) => {
                        return data[rowIndex].changed ? 'warning' : '';
                      }}>
                      <Basic.Column
                        property="property"
                        header={ this.i18n('detail.attributeNameCol') }
                        cell={this._formatAttributeName}
                      />
                      <Basic.Column
                        property="systemVal"
                        header={ this._renderColumnHelp(this.i18n('detail.systemObject.label'), this.i18n('detail.systemObject.help')) }
                        cell={this._highlightCellContent.bind(this, isArchive)}
                      />
                      <Basic.Column
                        property="accountVal"
                        header={ this._renderColumnHelp(this.i18n('detail.accountObject.label'), this.i18n('detail.accountObject.help')) }
                        cell={this._highlightCellContent.bind(this, isArchive)}
                      />
                    </Basic.Table>
                  </Basic.Col>
                </Basic.Row>
              </div>
            }
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.closeDetail.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

ProvisioningOperations.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Force searchparameters - system id
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Show delete all button
   */
  showDeleteAllButton: PropTypes.bool
};
ProvisioningOperations.defaultProps = {
  forceSearchParameters: null,
  columns: ProvisioningOperationTable.defaultProps.columns,
  showDeleteAllButton: true
};

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(ProvisioningOperations);

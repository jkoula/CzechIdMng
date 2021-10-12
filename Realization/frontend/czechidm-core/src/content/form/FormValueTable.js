import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import Joi from 'joi';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
import { FormValueManager } from '../../redux';
import PersistentTypeEnum from '../../enums/PersistentTypeEnum';

const manager = new FormValueManager();

/**
 * Table of abstract form values.
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
export class FormValueTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true,
      persistentType: null
    };
  }

  getContentKey() {
    return 'content.form-values';
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    const filterData = Domain.SearchParameters.getFilterData(this.refs.filterForm);
    //
    // resolve additional filter options
    const intValue = this.refs.intValue.getValue();
    if (intValue) {
      filterData.longValue = intValue;
    }
    delete filterData.intValue;
    //
    this.refs.table.useFilterData(filterData);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      persistentType: null
    }, () => {
      this.refs.table.cancelFilter(this.refs.filterForm);
    });
  }

  /**
   * Loads filter from redux state or default.
   */
  loadFilter() {
    if (!this.refs.filterForm) {
      return;
    }
    //  filters from redux
    const _searchParameters = this.getSearchParameters();
    if (_searchParameters) {
      const filterData = {};
      _searchParameters.getFilters().forEach((v, k) => {
        filterData[k] = v;
      });
      // init persistent type by persisted filter in redux
      if (filterData.persistentType) {
        this.setState({
          persistentType: filterData.persistentType
        }, () => {
          if (filterData.persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.INT)) {
            filterData.intValue = filterData.longValue;
          }
          this.refs.filterForm.setData(filterData);
        });
      } else {
        this.refs.filterForm.setData(filterData);
      }
    }
  }

  _onChangePersistentType(item) {
    this.setState({
      persistentType: item ? item.value : null,
    }, () => {
      this.refs.stringValueLike.setValue(null);
      this.refs.shortTextValueLike.setValue(null);
      this.refs.fromTill.setValue(null);
      this.refs.intValue.setValue(null);
      this.refs.longValue.setValue(null);
      this.refs.doubleValue.setValue(null);
      this.refs.booleanValue.setValue(null);
      this.refs.uuidValue.setValue(null);
    });
  }

  render() {
    const { uiKey, forceSearchParameters, showFilter, columns, className } = this.props;
    const { filterOpened, persistentType } = this.state;
    //
    let _forceSearchParameters = forceSearchParameters || new Domain.SearchParameters();
    _forceSearchParameters = _forceSearchParameters.setFilter('addOwnerDto', true);
    //
    return (
      <Advanced.Table
        ref="table"
        uiKey={ uiKey }
        manager={ manager }
        forceSearchParameters={ _forceSearchParameters }
        showFilter={ showFilter }
        className={ className }
        showRowSelection
        filter={
          <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
            <Basic.AbstractForm ref="filterForm">
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Advanced.Filter.TextField
                    ref="text"
                    placeholder={this.i18n('filter.text.placeholder')} />
                </Basic.Col>
                <Basic.Col lg={ 6 } className="text-right">
                  <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)} />
                </Basic.Col>
              </Basic.Row>
              <Basic.Row>
                <Basic.Col lg={ 4 }>
                  <Basic.EnumSelectBox
                    ref="persistentType"
                    placeholder={ this.i18n('filter.type.placeholder') }
                    multiSelect={ false }
                    enum={ PersistentTypeEnum }
                    onChange={ this._onChangePersistentType.bind(this) }/>
                </Basic.Col>
                <Basic.Col lg={ 8 }>
                  <Basic.TextField
                    ref="stringValueLike"
                    hidden={
                      persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.TEXT)
                        && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CHAR)
                    }
                    placeholder={ this.i18n('filter.value.placeholder') }
                    help={ Advanced.Filter.getTextHelp() }
                  />
                  <Basic.TextField
                    ref="shortTextValueLike"
                    hidden={
                      persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.SHORTTEXT)
                       && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST)
                       && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ENUMERATION)
                    }
                    placeholder={ this.i18n('filter.value.placeholder') }
                    min={ 0 }
                    max={ 2000 }
                    help={ Advanced.Filter.getTextHelp() }
                  />
                  <Advanced.Filter.FilterDate
                    ref="fromTill"
                    hidden={
                      persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.DATE)
                        && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.DATETIME)
                    }
                    fromProperty="dateValueFrom"
                    tillProperty="dateValueTill"/>
                  <Basic.TextField
                    ref="intValue"
                    placeholder={ this.i18n('filter.value.placeholder') }
                    hidden={ persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.INT) }
                    validation={ Joi.number().integer().min(-2147483648).max(2147483647) }/>
                  <Basic.TextField
                    ref="longValue"
                    placeholder={ this.i18n('filter.value.placeholder') }
                    hidden={ persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.LONG) }
                    validation={ Joi.number().integer().min(-9223372036854775808).max(9223372036854775807) }/>
                  <Basic.TextField
                    ref="doubleValue"
                    placeholder={ this.i18n('filter.value.placeholder') }
                    hidden={ persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.DOUBLE) }
                    validation={ Joi.number().min(-(10 ** 33)).max(10 ** 33) } />
                  <Basic.BooleanSelectBox
                    ref="booleanValue"
                    placeholder={ this.i18n('filter.value.placeholder') }
                    hidden={ persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.BOOLEAN) }
                    options={ [
                      { value: 'true', niceLabel: 'true' },
                      { value: 'false', niceLabel: 'false' }
                    ]}/>
                  <Basic.TextField
                    ref="uuidValue"
                    placeholder={ this.i18n('filter.value.placeholder') }
                    hidden={
                      persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.UUID)
                        && persistentType !== PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.ATTACHMENT)
                    }/>
                </Basic.Col>
              </Basic.Row>
            </Basic.AbstractForm>
          </Advanced.Filter>
        }
        filterOpened={ filterOpened }
        _searchParameters={ this.getSearchParameters() }>

        <Advanced.Column
          property="ownerId"
          cell={
            ({ rowIndex, data }) => {
              return (
                <Advanced.EntityInfo
                  entityType={ Utils.Ui.getSimpleJavaType((data[rowIndex].ownerType)) }
                  entity={ data[rowIndex]._embedded ? data[rowIndex]._embedded.owner : null }
                  entityIdentifier={ data[rowIndex].ownerId }
                  showIcon
                  face="popover"
                  showEntityType />
              );
            }
          }
          rendered={ _.includes(columns, 'owner') }/>
        <Advanced.Column
          property="_embedded.formAttribute.code"
          header={ this.i18n('entity.FormAttribute.code.label')}
          sort
          sortProperty="formAttribute.code"
          rendered={ _.includes(columns, 'code') }/>
        <Advanced.Column
          property="_embedded.formAttribute.name"
          header={ this.i18n('entity.FormAttribute.name.label') }
          sort
          sortProperty="formAttribute.name"
          rendered={ _.includes(columns, 'name') }/>
        <Advanced.Column
          property="value"
          rendered={ _.includes(columns, 'value') }
          cell={
            ({ rowIndex, data, property }) => (
              Utils.Ui.toStringValue(data[rowIndex][property])
            )
          }/>
        <Advanced.Column
          property="persistentType"
          face="enum"
          enumClass={ PersistentTypeEnum }
          sort
          rendered={_.includes(columns, 'persistentType')}/>
        <Advanced.Column
          property="_embedded.formAttribute.defaultValue"
          header={ this.i18n('entity.FormAttribute.defaultValue')}
          sort
          sortProperty="formAttribute.defaultValue"
          rendered={ _.includes(columns, 'defaultValue') }/>
        <Advanced.Column
          property="_embedded.formAttribute.faceType"
          header={ this.i18n('entity.FormAttribute.faceType.label') }
          sort
          sortProperty="formAttribute.faceType"
          rendered={ _.includes(columns, 'faceType') }/>
      </Advanced.Table>
    );
  }
}

FormValueTable.propTypes = {
  filterOpened: PropTypes.bool,
  showFilter: PropTypes.bool,
  uiKey: PropTypes.string.isRequired,
  forceSearchParameters: PropTypes.object,
  columns: PropTypes.arrayOf(PropTypes.string),
};

FormValueTable.defaultProps = {
  filterOpened: true,
  showFilter: true,
  forceSearchParameters: null,
  columns: ['owner', 'code', 'name', 'value', 'persistentType', 'defaultValue', 'faceType'],
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(FormValueTable);

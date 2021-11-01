import React from 'react';
import _ from 'lodash';
//
import {Basic, Advanced, Utils} from 'czechidm-core';

/**
 * Table for account attributes.
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
export class AttributeTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      systemEntity: null,
      attributeNameFilter: null
    };
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getContentKey() {
    return 'acc:content.system.accounts';
  }

  _useAttributesFilter() {
    const attributeNameFilterComponent = this.refs.attributeNameFilter;
    const attributeValueFilterComponent = this.refs.attributeValueFilter;
    let attributeNameFilter;
    let attributeValueFilter;
    if (attributeNameFilterComponent) {
      attributeNameFilter = attributeNameFilterComponent.getValue();
    }
    if (attributeValueFilterComponent) {
      attributeValueFilter = attributeValueFilterComponent.getValue();
    }
    this.setState({attributeNameFilter, attributeValueFilter});
  }

  _cancelAttributesFilter() {
    const attributeNameFilterComponent = this.refs.attributeNameFilter;
    const attributeValueFilterComponent = this.refs.attributeValueFilter;
    if (attributeNameFilterComponent) {
      attributeNameFilterComponent.setValue(null);
    }
    if (attributeValueFilterComponent) {
      attributeValueFilterComponent.setValue(null);
    }
    this.setState({attributeNameFilter: null, attributeValueFilter: null});
  }

  /**
   * Filter of attributes by name (only on FE).
   * @param connectorObject
   */
  _applyAttributeFilter(connectorObject) {
    const {attributeNameFilter, attributeValueFilter} = this.state;

    if (!connectorObject || !connectorObject.attributes || (!attributeNameFilter && !attributeValueFilter)) {
      return connectorObject;
    }
    let attributes = _.merge([], connectorObject.attributes);
    if (attributeNameFilter) {
      attributes = attributes.filter(attribute => attribute.name.toLowerCase().includes(attributeNameFilter.toLowerCase()));
    }
    if (attributeValueFilter) {
      attributes = attributes.filter(attribute => {
        const values = attribute.values;
        if (_.isArray(values)) {
          const filteredValues = values.filter(value => {
            const valueString = value ? value.toString() : null;
            if (valueString && valueString.toLowerCase().includes(attributeValueFilter.toLowerCase())) {
              return true;
            }
          });
          attribute.values = filteredValues;
          return !!filteredValues && filteredValues.length > 0;
        }
        if (_.isObject(values)) {
          const valueString = values ? values.toString() : null;
          if (valueString && valueString.toLowerCase().includes(attributeValueFilter.toLowerCase())) {
            return true;
          }
        }
        return false;
      });
    }
    return {attributes};
  }

  render() {
    const {
      connectorObject,
      rendered
    } = this.props;
    const _connectorObject = this._applyAttributeFilter(connectorObject);

    return (
      <>
        <form onSubmit={ this._useAttributesFilter.bind(this) }>
          <Basic.AbstractForm ref="filterForm">
            <Basic.Row>
              <Basic.Col lg={4}>
                <Advanced.Filter.TextField
                  ref="attributeNameFilter"
                  placeholder={this.i18n('acc:content.system.accounts.filter.attributeName.placeholder')}/>
              </Basic.Col>
              <Basic.Col lg={4}>
                <Advanced.Filter.TextField
                  ref="attributeValueFilter"
                  placeholder={this.i18n('acc:content.system.accounts.filter.attributeValue.placeholder')}/>
              </Basic.Col>
              <Basic.Col lg={4} className="text-right">
                <Advanced.Filter.FilterButtons
                  useFilter={this._useAttributesFilter.bind(this)}
                  cancelFilter={this._cancelAttributesFilter.bind(this)}/>
              </Basic.Col>
            </Basic.Row>
          </Basic.AbstractForm>
        </form>
        <Basic.Table
          showLoading={!connectorObject && !this.state.hasOwnProperty('connectorObject')}
          data={_connectorObject ? _connectorObject.attributes : null}
          noData={this.i18n('component.basic.Table.noData')}
          className="table-bordered"
          rendered={rendered}>
          <Basic.Column property="name" header={this.i18n('label.property')}/>
          <Basic.Column
            property="values"
            header={this.i18n('label.value')}
            cell={
              ({rowIndex, data}) => {
                return (
                  Utils.Ui.toStringValue(data[rowIndex].values)
                );
              }
            }/>
        </Basic.Table>
      </>
    );
  }
}

export default AttributeTable;

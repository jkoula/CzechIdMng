import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { SecurityManager, DataManager, TreeTypeManager } from '../../../redux';

/**
 * Table of structure types.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export class TypeTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true
    };
  }

  getContentKey() {
    return 'content.tree.types';
  }

  componentDidMount() {
    const { treeTypeManager, uiKey } = this.props;
    const searchParameters = treeTypeManager.getService().getDefaultSearchParameters();
    this.context.store.dispatch(treeTypeManager.fetchEntities(searchParameters, uiKey));
    this.context.store.dispatch(treeTypeManager.fetchDefaultTreeType());
  }

  componentWillUnmount() {
    this.cancelFilter();
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    const data = {
      ...this.refs.filterForm.getData(),
    };
    this.refs.table.useFilterData(data);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.history.push(`/tree/types/${ uuidId }?new=1`);
    } else {
      this.context.history.push(`/tree/types/${ entity.id }`);
    }
  }

  render() {
    const { uiKey, treeTypeManager, defaultTreeType } = this.props;
    const { filterOpened } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-12">
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ treeTypeManager }
            showRowSelection
            rowClass={ ({ rowIndex, data }) => { return data[rowIndex].disabled ? 'disabled' : ''; } }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <Basic.Col lg={ 6 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={`${ this.i18n('entity.TreeType.code') } / ${ this.i18n('entity.TreeType.name') }`}/>
                    </Basic.Col>
                    <Basic.Col lg={ 6 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={!filterOpened}
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  buttonSize="xs"
                  onClick={ this.showDetail.bind(this, {}) }
                  rendered={ SecurityManager.hasAuthority('TREETYPE_CREATE') }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={ this.i18n('button.detail') }
                      onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                  );
                }
              }
              sort={false}/>
            <Advanced.Column property="code" sort width={125}/>
            <Advanced.Column property="name" sort/>
            <Advanced.Column
              header={ this.i18n('entity.TreeType.defaultTreeType.label') }
              className="column-face-bool"
              cell={
                ({ rowIndex, data }) => {
                  //
                  return (
                    <input type="checkbox" checked={ !!(defaultTreeType && defaultTreeType.id === data[rowIndex].id) } disabled />
                  );
                }
              }/>
          </Advanced.Table>
        </div>
      </Basic.Row>
    );
  }
}

TypeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  treeTypeManager: PropTypes.object.isRequired,
  /**
   * Default tree type is used for info in table
   */
  defaultTreeType: PropTypes.object
};

TypeTable.defaultProps = {
  _showLoading: false,
  defaultTreeType: null
};

function select(state) {
  return {
    defaultTreeType: DataManager.getData(state, TreeTypeManager.UI_KEY_DEFAULT_TREE_TYPE)
  };
}

export default connect(select)(TypeTable);

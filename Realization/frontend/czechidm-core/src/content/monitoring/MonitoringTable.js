import PropTypes from 'prop-types';
import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
//
import {
  MonitoringManager,
  MonitoringResultManager,
  DataManager
} from '../../redux';
//
// default manager
const manager = new MonitoringManager();
const monitoringResultManager = new MonitoringResultManager();

/**
* Configured monitoring evaluators.
*
* @author Radek Tomiška
* @since 11.1.0
*/
export class MonitoringTable extends Advanced.AbstractTableContent {

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(this.getManager().fetchSupportedEvaluators(() => {
      this.refs.filterForm.focus();
    }));
  }

  getContentKey() {
    return 'content.monitorings';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return this.props.manager;
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
    if (Utils.Entity.isNew(entity)) {
      this.context.history.push(`/monitoring/monitorings/${ uuid.v1() }/detail?new=1`);
    } else {
      this.context.history.push(`/monitoring/monitorings/${ encodeURIComponent(entity.id) }/detail`);
    }
  }

  render() {
    const {
      uiKey,
      columns,
      supportedEvaluators,
      className,
      showAddButton,
      showRowSelection
    } = this.props;
    const { filterOpened } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          showRowSelection={ showRowSelection }
          className={ className }
          filterOpened={ filterOpened }
          rowClass={
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              //
              // installed vs. available monitoring evaluatos - evaluator from disabled module
              if (supportedEvaluators && !supportedEvaluators.has(entity.evaluatorType)) {
                return 'disabled';
              }
              return Utils.Ui.getDisabledRowClass(entity);
            }
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, {}) }
                rendered={ supportedEvaluators && supportedEvaluators.size > 0 && manager.canSave() && showAddButton }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filter={
            <Filter
              ref="filterForm"
              onSubmit={ this.useFilter.bind(this) }
              onCancel={ this.cancelFilter.bind(this) }/>
          }
          afterBulkAction={
            () => {
              this.context.store.dispatch(monitoringResultManager.fetchLastMonitoringResults());
            }
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
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }
            sort={ false }/>
          <Advanced.Column
            property="evaluatorType"
            header={ this.i18n('entity.Monitoring.evaluatorType.label') }
            sort
            rendered={ _.includes(columns, 'evaluatorType') }
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskName entity={ data[rowIndex] } supportedTasks={ supportedEvaluators } showIcon={ false }/>
              )
            }/>
          <Advanced.Column
            face="text"
            header={ this.i18n('entity.Monitoring.evaluatorProperties.label') }
            rendered={_.includes(columns, 'evaluatorProperties')}
            width="25%"
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskProperties
                  key={ `evaluator-eav-${ Utils.Ui.getComponentKey(data[rowIndex]) }` }
                  entity={ data[rowIndex] }
                  supportedTasks={ supportedEvaluators }
                  condensed/>
              )
            }/>
          <Advanced.Column
            property="description"
            header={ this.i18n('entity.Monitoring.description.label') }
            face="text"
            sort
            rendered={ _.includes(columns, 'description') }/>
          <Advanced.Column
            property="disabled"
            header={ this.i18n('entity.Monitoring.disabled.label') }
            face="bool"
            sort
            rendered={ _.includes(columns, 'disabled') }/>
          <Advanced.Column
            property="checkPeriod"
            header={ this.i18n('entity.Monitoring.checkPeriod.label') }
            face="text"
            sort
            rendered={ _.includes(columns, 'checkPeriod') }/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

MonitoringTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  //
  _showLoading: PropTypes.bool
};

MonitoringTable.defaultProps = {
  columns: ['evaluatorType', 'evaluatorProperties', 'description', 'disabled', 'checkPeriod'],
  manager,
  forceSearchParameters: null,
  _showLoading: false,
  showRowSelection: true,
  showAddButton: true
};

function select(state, component) {
  return {
    supportedEvaluators: DataManager.getData(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _showLoading: Utils.Ui.isShowLoading(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(MonitoringTable);

/**
 * Table filter component.
 *
 * @author Radek Tomiška
 */
class Filter extends Advanced.Filter {

  focus() {
    this.refs.text.focus();
  }

  render() {
    const { onSubmit, onCancel } = this.props;
    //
    return (
      <Advanced.Filter onSubmit={ onSubmit }>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row className="last">
            <Basic.Col lg={ 8 }>
              <Advanced.Filter.TextField
                ref="text"
                placeholder={ this.i18n('content.monitorings.filter.text.placeholder') }
                help={ Advanced.Filter.getTextHelp() }/>
            </Basic.Col>
            <Basic.Col lg={ 4 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={ onCancel }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }
}

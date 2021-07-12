import PropTypes from 'prop-types';
import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
//
import {
  MonitoringManager,
  MonitoringResultManager,
  DataManager,
  FormAttributeManager,
  ConfigurationManager,
  SecurityManager
} from '../../redux';
import MonitoringResultTable from './MonitoringResultTable';
//
const formAttributeManager = new FormAttributeManager();
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

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      ...this.state,
      filterOpened: props.filterOpened,
      evaluatorType: null,
      urlId: null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(this.getManager().fetchSupportedEvaluators(() => {
      const { entityId } = this.props.match.params;
      if (entityId) {
        this.loadDetail({
          id: entityId
        });
      } else {
        this.refs.filterForm.focus();
      }
    }));
  }

  componentDidUpdate() {
    const { entityId } = this.props.match.params;
    //
    if (entityId && entityId !== this.state.urlId) {
      this.loadDetail({
        id: entityId
      });
    }
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

  /**
   * Open modal with report's select
   */
  loadDetail(entity) {
    // reload Entity
    this.setState({
      detail: {
        show: true,
        entity
      },
      urlId: entity.id
    }, () => {
      this.context.store.dispatch(manager.fetchEntity(entity.id, `${ this.getUiKey() }-detail`, (loadedEntity, error) => {
        if (error) {
          this.addError(error);
        } else {
          this.showDetail(loadedEntity);
        }
      }));
    });
  }

  showDetail(entity) {
    const { supportedEvaluators } = this.props;
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    this.setState({
      detail: {
        show: true,
        entity
      },
      evaluatorType: supportedEvaluators.has(entity.evaluatorType) ? this._toEvaluatorOption(supportedEvaluators.get(entity.evaluatorType)) : null
    }, () => {
      // @todo-upgrade-10 - Remove set timeout after update react-bootstap!
      setTimeout(() => {
        this.refs.evaluatorType.focus();
      }, 10);
    });
  }

  closeDetail() {
    const { detail } = this.state;
    //
    this.setState({
      detail: {
        ...detail,
        show: false
      },
      evaluatorType: null
    }, () => {
      this.context.history.replace('/monitoring/monitorings');
    });
  }

  save(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (this.refs.formInstance) {
      if (!this.refs.formInstance.isValid()) {
        return;
      }
    }
    const formEntity = this.refs.form.getData();
    //
    // transform properties
    if (this.refs.formInstance) {
      formEntity.evaluatorProperties = this.refs.formInstance.getProperties();
    }
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({
        message: this.i18n('save.success', { count: 1, name: this.getManager().getNiceLabel(entity, this.props.supportedEvaluators) })
      });
      // TODO: trimmed vs. not trimmed view ...
      this.refs.table.reload();
      this.context.store.dispatch(monitoringResultManager.fetchLastMonitoringResults());
    }
    //
    super.afterSave(entity, error);
  }

  onChangeEvaluatorType(evaluatorType) {
    this.setState({
      evaluatorType
    });
  }

  _getSupportedEvaluators() {
    const { supportedEvaluators } = this.props;
    //
    const _supportedEvaluators = [];
    if (!supportedEvaluators) {
      return _supportedEvaluators;
    }
    //
    supportedEvaluators.forEach(evaluator => {
      _supportedEvaluators.push(this._toEvaluatorOption(evaluator));
    });
    //
    return _supportedEvaluators;
  }

  _toEvaluatorOption(evaluator) {
    return {
      niceLabel: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'label', Utils.Ui.getSimpleJavaType(evaluator.evaluatorType)),
      value: evaluator.evaluatorType,
      description: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'help', evaluator.description),
      formDefinition: evaluator.formDefinition
    };
  }

  render() {
    const {
      uiKey,
      columns,
      _showLoading,
      supportedEvaluators,
      _permissions,
      className,
      showAddButton,
      showRowSelection,
      instanceId
    } = this.props;
    const { detail, evaluatorType, filterOpened } = this.state;
    //
    let formInstance = new Domain.FormInstance({});
    if (evaluatorType && evaluatorType.formDefinition && detail.entity) {
      formInstance = new Domain.FormInstance(evaluatorType.formDefinition).setProperties(detail.entity.evaluatorProperties);
    }
    //
    const _supportedEvaluators = this._getSupportedEvaluators();
    //
    let resultForceSearchParameters = new Domain.SearchParameters();
    if (detail.entity) {
      resultForceSearchParameters = resultForceSearchParameters.setFilter('monitoring', detail.entity.id);
    }
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
                onClick={ this.showDetail.bind(this, { instanceId, checkPeriod: 0 }) }
                rendered={ _supportedEvaluators.length > 0 && manager.canSave() && showAddButton }
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

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard={ !_showLoading }>

          <form onSubmit={ this.save.bind(this, {}) }>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('create.header') }
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body style={{ paddingTop: 0 }}>
              <Basic.AbstractForm
                ref="form"
                data={ detail.entity }
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }
                style={{ paddingTop: 0 }}>
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Basic.ContentHeader text={ this.i18n('tabs.basic') } />
                    <Basic.EnumSelectBox
                      ref="evaluatorType"
                      options={ _supportedEvaluators }
                      onChange={ this.onChangeEvaluatorType.bind(this) }
                      label={ this.i18n('entity.Monitoring.evaluatorType.label') }
                      helpBlock={ evaluatorType ? evaluatorType.description : null }
                      required
                      searchable/>
                    <Basic.TextField
                      ref="checkPeriod"
                      validation={
                        Joi
                          .number()
                          .integer()
                          .min(0)
                          .max(9223372036854775807)
                      }
                      required
                      label={ this.i18n('entity.Monitoring.checkPeriod.label') }
                      helpBlock={ this.i18n('entity.Monitoring.checkPeriod.help', { escape: false }) }/>
                    <Basic.DateTimePicker
                      ref="executeDate"
                      label={ this.i18n('entity.Monitoring.executeDate.label') }
                      helpBlock={ this.i18n('entity.Monitoring.executeDate.help') }/>
                    <Basic.TextField
                      ref="instanceId"
                      label={ this.i18n('entity.Monitoring.instanceId.label') }
                      helpBlock={ this.i18n('entity.Monitoring.instanceId.help') }
                      required/>
                    <Basic.TextField
                      ref="seq"
                      validation={
                        Joi
                          .number()
                          .integer()
                          .min(0)
                          .max(32767)
                          .allow(null)
                      }
                      label={ this.i18n('entity.Monitoring.seq.label') }
                      helpBlock={ this.i18n('entity.Monitoring.seq.help') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 }>
                    <Basic.Div>
                      <Basic.ContentHeader text={ this.i18n('entity.Monitoring.evaluatorProperties.title') }/>
                      <Advanced.EavForm
                        ref="formInstance"
                        formInstance={ formInstance }
                        useDefaultValue={ Utils.Entity.isNew(detail.entity) }/>
                    </Basic.Div>
                  </Basic.Col>
                </Basic.Row>

                <Basic.TextArea
                  ref="description"
                  label={this.i18n('entity.Monitoring.description.label')}
                  max={ 2000 }/>
                <Basic.Checkbox
                  ref="disabled"
                  label={ this.i18n('entity.Monitoring.disabled.label') }
                  helpBlock={ this.i18n('entity.Monitoring.disabled.help') }/>

              </Basic.AbstractForm>

              {
                !SecurityManager.hasAuthority('MONITORINGRESULT_READ')
                ||
                <Basic.Div>
                  <Basic.ContentHeader
                    text={ this.i18n('content.monitoring-results.header') }
                    style={{ marginBottom: 0 }}
                    rendered={ !Utils.Entity.isNew(detail.entity) } />

                  <MonitoringResultTable
                    ref="monitoringResultTable"
                    showFilter={ false }
                    showToolbar
                    showRowSelection={ false }
                    history={ this.context.history }
                    location={ this.props.location }
                    columns={ ['result', 'created', 'owner', 'value', 'instanceId'] }
                    match={ this.props.match }
                    uiKey={ `monitoring-monitoring-result-table-${ detail.entity.id }` }
                    rendered={ !Utils.Entity.isNew(detail.entity) }
                    forceSearchParameters={ resultForceSearchParameters }
                    className="no-margin"/>
                </Basic.Div>
              }
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(detail.entity, _permissions) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
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
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

MonitoringTable.defaultProps = {
  columns: ['evaluatorType', 'evaluatorProperties', 'description', 'disabled', 'checkPeriod'],
  manager,
  forceSearchParameters: null,
  _showLoading: false,
  _permissions: null,
  showRowSelection: true,
  showAddButton: true
};

function select(state, component) {
  return {
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    supportedEvaluators: DataManager.getData(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _showLoading: Utils.Ui.isShowLoading(state, `${ component.uiKey }-detail`)
      || Utils.Ui.isShowLoading(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _permissions: Utils.Permission.getPermissions(state, `${ component.uiKey }-detail`)
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

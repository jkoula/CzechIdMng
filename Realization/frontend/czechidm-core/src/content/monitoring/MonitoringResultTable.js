import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Domain from '../../domain';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { MonitoringResultManager, MonitoringManager, FormAttributeManager, DataManager } from '../../redux';
import NotificationLevelEnum from '../../enums/NotificationLevelEnum';
//
const manager = new MonitoringResultManager();
const monitoringManager = new MonitoringManager();
const formAttributeManager = new FormAttributeManager();

/**
 * Monitoring results.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
export class MonitoringResultTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      detail: {
        show: false,
        entity: {},
        message: null
      },
      urlId: null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(monitoringManager.fetchSupportedEvaluators(() => {
      const { entityId } = this.props.match.params;
      if (entityId) {
        this.loadDetail({
          id: entityId
        });
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
    return 'content.monitoring-results';
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
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  useFilterData(data = {}) {
    const _data = { ...this.refs.filterForm.getData() || {}, ...data };
    //
    this.refs.filterForm.setData(_data);
    this.refs.table.useFilterData(_data);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  _toEvaluatorOption(evaluator) {
    return {
      niceLabel: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'label', Utils.Ui.getSimpleJavaType(evaluator.evaluatorType)),
      value: evaluator.evaluatorType,
      description: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'help', evaluator.description),
      formDefinition: evaluator.formDefinition
    };
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
          if (loadedEntity.result && loadedEntity.result.model && loadedEntity.level) {
            loadedEntity.result.model.level = loadedEntity.level;
          }
          this.showDetail(loadedEntity);
        }
      }));
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    }, () => {
      const { forceSearchParameters } = this.props;
      //
      if (!forceSearchParameters || !forceSearchParameters.getFilters().has('monitoring')) {
        this.context.history.replace('/monitoring/monitoring-results');
      }
    });
  }

  render() {
    const {
      columns,
      rendered,
      className,
      supportedEvaluators,
      forceSearchParameters,
      showFilter,
      showToolbar,
      showRowSelection,
      _showLoading
    } = this.props;
    const { filterOpened, detail } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    let formInstance = new Domain.FormInstance({});
    if (detail.entity.evaluatorType && supportedEvaluators) {
      const evaluatorType = supportedEvaluators.has(detail.entity.evaluatorType) ? supportedEvaluators.get(detail.entity.evaluatorType) : null;
      if (evaluatorType && evaluatorType.formDefinition) {
        formInstance = new Domain.FormInstance(evaluatorType.formDefinition).setProperties(detail.entity.evaluatorProperties);
      }
    }
    //
    return (
      <Basic.Div>

        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.FilterDate
                      ref="fromTill"
                      fromProperty="createdFrom"
                      tillProperty="createdTill"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 3 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="level"
                      placeholder={ this.i18n('entity.MonitoringResult.level.label') }
                      enum={ NotificationLevelEnum }
                      multiSelect/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('content.monitoring-results.filter.text.placeholder') }
                      help={ Advanced.Filter.getTextHelp() }/>
                  </Basic.Col>
                  <Basic.Col lg={ 3 }/>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
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
          showRowSelection={ showRowSelection }
          showFilter={ showFilter }
          showToolbar={ showToolbar }
          className={ className }
          afterBulkAction={
            () => {
              this.context.store.dispatch(this.getManager().fetchLastMonitoringResults());
            }
          }
          forceSearchParameters={ forceSearchParameters }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ () => this.showDetail(data[rowIndex]) }/>
                );
              }
            }/>
          <Advanced.Column
            property="result"
            width={ 75 }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                // manual level has higher priority
                if (entity.result && entity.result.model && entity.level) {
                  entity.result.model.level = entity.level;
                }
                return (
                  <Advanced.OperationResult value={ entity.result } detailLink={ () => this.showDetail(data[rowIndex]) }/>
                );
              }
            }
            rendered={ _.includes(columns, 'result') }/>
          <Advanced.Column
            property="created"
            sort
            face="datetime"
            rendered={ _.includes(columns, 'created') }/>
          <Advanced.Column
            property="evaluatorType"
            header={ this.i18n('entity.MonitoringResult.evaluatorType.label') }
            sort
            rendered={ _.includes(columns, 'evaluatorType') }
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskName entity={ data[rowIndex] } supportedTasks={ supportedEvaluators } showIcon={ false }/>
              )
            }/>
          <Advanced.Column
            face="text"
            header={ this.i18n('entity.MonitoringResult.evaluatorProperties.label') }
            rendered={_.includes(columns, 'evaluatorProperties')}
            width="25%"
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskProperties
                  key={ `evaluator-eav-${ data[rowIndex].id }` }
                  entity={ data[rowIndex] }
                  supportedTasks={ supportedEvaluators }
                  condensed/>
              )
            }/>
          <Advanced.Column
            property="ownerId"
            header={ this.i18n('entity.MonitoringResult.owner.label') }
            rendered={ _.includes(columns, 'owner') }
            cell={
              ({ rowIndex, data, property }) => {
                const entity = data[rowIndex];
                //
                if (!entity._embedded || !entity._embedded[property]) {
                  if (entity._embedded.revision) {
                    return (
                      <Advanced.EntityInfo
                        entityType={ Utils.Ui.getSimpleJavaType(entity.ownerType) }
                        entityIdentifier={ entity[property] }
                        entity={ entity.content }
                        face="popover"
                        showLink={ false }
                        showEntityType={ false }
                        showIcon
                        deleted/>
                    );
                  }
                  return (
                    <Advanced.UuidInfo value={ entity[property] } />
                  );
                }
                //
                return (
                  <Advanced.EntityInfo
                    entityType={ Utils.Ui.getSimpleJavaType(entity.ownerType) }
                    entityIdentifier={ entity[property] }
                    entity={ entity._embedded[property] }
                    face="popover"
                    showEntityType={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="value"
            rendered={ _.includes(columns, 'value') }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          showLoading={ _showLoading }
          backdrop="static">
          <Basic.Modal.Header closeButton text={ this.i18n('edit.header') }/>
          <Basic.Modal.Body style={{ paddingTop: 0 }}>
            <Basic.FlashMessage message={ detail.message } className="no-margin" />

            <Basic.AbstractForm
              ref="form"
              data={ detail.entity }
              readOnly
              style={{ paddingTop: 0 }}>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.ContentHeader text={ this.i18n('tabs.basic') } />

                  <Basic.LabelWrapper label={ this.i18n('entity.created') }>
                    <Advanced.DateValue value={ detail.entity.created } showTime/>
                  </Basic.LabelWrapper>

                  {
                    !detail.entity.monitoringStarted
                    ||
                    <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.monitoringStarted.label') }>
                      <Advanced.DateValue value={ detail.entity.monitoringStarted } showTime />
                    </Basic.LabelWrapper>
                  }

                  <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.evaluatorType.label') }>
                    <span title={ detail.entity.evaluatorType }>
                      <Advanced.LongRunningTaskName entity={ detail.entity } supportedTasks={ supportedEvaluators } showIcon={ false }/>
                    </span>
                  </Basic.LabelWrapper>

                  <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.instanceId.label') }>
                    { detail.entity.instanceId }
                    <span className="help-block">{ this.i18n('entity.MonitoringResult.instanceId.help') }</span>
                  </Basic.LabelWrapper>

                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.ContentHeader text={ this.i18n('entity.Monitoring.evaluatorProperties.title') }/>
                  <Advanced.EavForm
                    ref="formInstance"
                    formInstance={ formInstance }
                    readOnly
                    useDefaultValue={ false }/>
                </Basic.Col>
              </Basic.Row>

              <Basic.Row rendered={ detail.entity.ownerType && detail.entity.ownerId } >
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.ownerType.label') }>
                    <span title={ detail.entity.ownerType }>
                      { Utils.Ui.getSimpleJavaType(detail.entity.ownerType) }
                    </span>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.owner.label') }>
                    {
                      !detail.entity || !detail.entity.ownerType
                      ||
                      <Advanced.EntityInfo
                        entityType={ Utils.Ui.getSimpleJavaType(detail.entity.ownerType) }
                        entityIdentifier={ detail.entity.ownerId }
                        style={{ margin: 0 }}
                        face="popover"
                        showEntityType={ false }
                        showIcon/>
                    }
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.value.label') }>
                    { detail.entity.value }
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  {
                    !detail.entity.monitoringStarted
                    ||
                    <Basic.LabelWrapper label={ this.i18n('entity.MonitoringResult.duration.label') }>
                      <Basic.TimeDuration
                        start={ detail.entity.monitoringStarted }
                        end={ detail.entity.monitoringEnded || detail.entity.modified }
                        humanized/>
                    </Basic.LabelWrapper>
                  }
                </Basic.Col>
              </Basic.Row>

              <Advanced.OperationResult value={ detail.entity.result } face="full" rendered={ !detail.message }/>
            </Basic.AbstractForm>

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

MonitoringResultTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * Rendered
   */
  rendered: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object
};

MonitoringResultTable.defaultProps = {
  columns: ['result', 'created', 'evaluatorType', 'evaluatorProperties', 'owner', 'value', 'instanceId'],
  filterOpened: false,
  rendered: true,
  showRowSelection: true
};

function select(state, component) {
  return {
    supportedEvaluators: DataManager.getData(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _showLoading: Utils.Ui.isShowLoading(state, `${ component.uiKey }-detail`)
      || Utils.Ui.isShowLoading(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(MonitoringResultTable);

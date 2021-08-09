import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Domain from '../../domain';
import {
  MonitoringManager,
  DataManager,
  MonitoringResultManager,
  FormAttributeManager,
  ConfigurationManager
} from '../../redux';
//
const manager = new MonitoringManager();
const formAttributeManager = new FormAttributeManager();
const monitoringResultManager = new MonitoringResultManager();

/**
 * Monitoring evaluator - basic information.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
class MonitoringDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      ...this.state,
      evaluatorType: null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, instanceId } = this.props;
    this.context.store.dispatch(manager.fetchSupportedEvaluators((supportedEvaluators) => {
      if (isNew) {
        this.context.store.dispatch(manager.receiveEntity(entityId, { instanceId, checkPeriod: 0 }, null, () => {
          this.refs.evaluatorType.focus();
        }));
      } else {
        this.getLogger().debug(`[FormDetail] loading entity detail [id:${ entityId }]`);
        this.context.store.dispatch(manager.fetchEntity(entityId, null, (entity) => {
          this.setState({
            evaluatorType:
              supportedEvaluators.has(entity.evaluatorType)
              ?
              this._toEvaluatorOption(supportedEvaluators.get(entity.evaluatorType))
              :
              null
          }, () => {
            this.refs.evaluatorType.focus();
          });
        }));
      }
    }));
  }

  getContentKey() {
    return 'content.monitorings';
  }

  getNavigationKey() {
    return 'monitoring-detail';
  }

  save(event) {
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
    const entity = this.refs.form.getData();
    //
    // transform properties
    if (this.refs.formInstance) {
      entity.evaluatorProperties = this.refs.formInstance.getProperties();
    }
    this.refs.form.processStarted();
    //
    if (Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(manager.createEntity(entity, null, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.patchEntity(entity, null, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.context.store.dispatch(monitoringResultManager.fetchLastMonitoringResults());
    this.addMessage({
      message: this.i18n('save.success', { count: 1, name: manager.getNiceLabel(entity, this.props.supportedEvaluators) })
    });
    if (isNew) {
      this.context.history.replace(`/monitoring/monitorings`);
    }
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
    const { entity, showLoading, _permissions } = this.props;
    const { evaluatorType } = this.state;
    //
    let formInstance = new Domain.FormInstance({});
    if (evaluatorType && evaluatorType.formDefinition && entity) {
      formInstance = new Domain.FormInstance(evaluatorType.formDefinition).setProperties(entity.evaluatorProperties);
    }
    //
    const _supportedEvaluators = this._getSupportedEvaluators();
    //
    return (
      <Basic.Div>
        <form onSubmit={ this.save.bind(this) }>
          <Basic.Panel className={ Utils.Entity.isNew(entity) ? '' : 'no-border last' }>
            {
              !Utils.Entity.isNew(entity)
              ||
              <Advanced.Panel.Header text={ this.i18n('create.header') }/>
            }
            <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
              <Basic.AbstractForm
                ref="form"
                data={ entity }
                readOnly={ !manager.canSave(entity, _permissions) }
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
                      readOnly={ !Utils.Entity.isNew(entity) }
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
                    <Basic.Div rendered={formInstance.getAttributes().size > 0 }>
                      <Basic.ContentHeader text={ this.i18n('entity.Monitoring.evaluatorProperties.title') }/>
                      <Advanced.EavForm
                        ref="formInstance"
                        formInstance={ formInstance }
                        useDefaultValue={ Utils.Entity.isNew(entity) }/>
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
            </Basic.PanelBody>
            <Basic.PanelFooter showLoading={ showLoading } >
              <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
                { this.i18n('button.back') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, _permissions) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </Basic.Div>
    );
  }
}

MonitoringDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
MonitoringDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId) || Utils.Ui.isShowLoading(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    supportedEvaluators: DataManager.getData(state, MonitoringManager.UI_KEY_SUPPORTED_EVALUATORS),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(MonitoringDetail);

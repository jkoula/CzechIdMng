import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
//
import {Basic, Utils} from 'czechidm-core';
import {SystemGroupManager} from '../../redux';
import SystemGroupTypeEnum from '../../domain/SystemGroupTypeEnum';

/**
 * System group detail
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroupDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      _showLoading: false
    };
    this.manager = new SystemGroupManager();
  }

  getContentKey() {
    return 'acc:content.systemGroup';
  }

  componentDidMount() {
    super.componentDidMount();
    if (this.refs.code) {
      this.refs.code.focus();
    }
  }

  save(afterAction, event) {
    const {manager, uiKey} = this.props;
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const entity = this.refs.form.getData();
    this.refs.form.processStarted();

    if (Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        this.context.history.replace(`${this.addRequestPrefix('system-groups', this.props.match.params)}/${createdEntity.id}/detail`);
      }));
    } else {
      this.context.store.dispatch(manager.updateEntity(entity, `${uiKey}-detail`, (patchedEntity, error) => {
        this._afterSave(patchedEntity, error);
      }));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    //
    this.refs.form.processEnded();
    this.addMessage({message: this.i18n('save.success', {code: entity.code})});
  }

  render() {
    const {
      entity,
      showLoading,
      permissions,
      manager
    } = this.props;

    const {_showLoading} = this.state;

    if (!entity) {
      return null;
    }

    return (
      <Basic.Div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic')}/>
        <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
          <Basic.PanelHeader
            text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic')}/>
          <Basic.PanelBody style={Utils.Entity.isNew(entity) ? {paddingTop: 0, paddingBottom: 0} : {padding: 0}}>
            <Basic.AbstractForm
              ref="form"
              onSubmit={(event) => {
                this.save('CONTINUE', event);
              }}
              data={entity}
              showLoading={_showLoading || showLoading}
              readOnly={!manager.canSave(entity, permissions)}>
              <Basic.TextField
                ref="code"
                label={this.i18n('acc:entity.SystemGroup.code')}
                max={255}
                required/>
              <Basic.EnumSelectBox
                ref="type"
                useSymbol={false}
                enum={SystemGroupTypeEnum}
                required
                label={this.i18n('acc:entity.SystemGroup.type.label')}
                helpBlock={this.i18n('acc:entity.SystemGroup.type.helpBlock')}/>
              <Basic.TextArea
                ref="description"
                label={this.i18n('acc:entity.SystemGroup.description')}
                max={2000}/>
              <Basic.Checkbox
                ref="disabled"
                label={this.i18n('acc:entity.SystemGroup.disabled.label')}
                helpBlock={this.i18n('acc:entity.SystemGroup.disabled.helpBlock')}/>
            </Basic.AbstractForm>
          </Basic.PanelBody>

          <Basic.PanelFooter>
            <Basic.Button
              type="button"
              level="link"
              onClick={this.context.history.goBack}
              showLoading={_showLoading}>
              {this.i18n('button.back')}
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              onClick={this.save.bind(this, 'CONTINUE')}
              showLoadingText={this.i18n('button.saving')}
              rendered={manager.canSave(entity, permissions)}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

SystemGroupDetail.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool,
  permissions: PropTypes.arrayOf(PropTypes.string)
};
SystemGroupDetail.defaultProps = {
  permissions: null
};

import React, { PropTypes } from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import authorityHelp from './AuthoritiesPanel_cs.md';
import AuthoritiesPanel from './AuthoritiesPanel';
import * as Basic from '../../components/basic';
import { RoleManager, WorkflowProcessDefinitionManager, SecurityManager } from '../../redux';

const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
const roleManager = new RoleManager();

class RoleDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      _showLoading: true
    };
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    const { entity } = this.props;

    if (Utils.Entity.isNew(entity)) {
      this._setSelectedEntity(entity);
    } else {
      this._setSelectedEntity(this._transformSubroles(entity));
    }
  }

  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (nextProps.entity && nextProps.entity !== entity) {
      this._setSelectedEntity(this._transformSubroles(nextProps.entity));
    }
  }

  _transformSubroles(entity) {
    const copyOfEntity = _.merge({}, entity);
    // transform subroles to array of identifiers
    delete copyOfEntity.subRoles;
    delete copyOfEntity.superiorRoles;

    copyOfEntity.subRoles = entity.subRoles.map(subRole => {
      if (subRole._embedded === undefined) {
        return subRole;
      }
      return subRole._embedded.sub.id;
    });
    // transform superiorRoles
    copyOfEntity.superiorRoles = entity.superiorRoles.map(superiorRole => {
      if (superiorRole._embedded === undefined) {
        return superiorRole;
      }
      return superiorRole._embedded.superior.id;
    });
    return copyOfEntity;
  }

  _setSelectedEntity(entity) {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    this.setState({
      _showLoading: true
    });
    const entity = this.refs.form.getData();
    // append selected authorities
    entity.authorities = this.refs.authorities.getWrappedInstance().getSelectedAuthorities();
    // append subroles
    if (entity.subRoles) {
      entity.subRoles = entity.subRoles.map(subRoleId => {
        return {
          sub: roleManager.getSelfLink(subRoleId)
        };
      });
    }
    // delete superior roles - we dont want to save them (they are ignored on BE anyway)
    delete entity.superiorRoles;
    //
    // console.log(entity, saveEntity);
    // this.getLogger().debug('[RoleTable] save entity', entity);
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(roleManager.createEntity(entity, null, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(roleManager.patchEntity(entity, null, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (isNew) {
      this.context.router.push('/roles');
    }
    this._setSelectedEntity(this._transformSubroles(entity));
  }

  render() {
    const { entity, showLoading } = this.props;
    const { _showLoading } = this.state;
    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.PanelHeader text={this.i18n('tabs.basic')} />
          <Basic.AbstractForm ref="form" showLoading={ _showLoading || showLoading } readOnly={!SecurityManager.hasAuthority('ROLE_WRITE')}>
            <Basic.Row>
              <div className="col-lg-8">
                <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('setting.basic.header')}</h3>
                <div className="form-horizontal">
                  <Basic.TextField
                    ref="name"
                    label={this.i18n('entity.Role.name')}
                    required/>
                  <Basic.EnumSelectBox
                    ref="roleType"
                    label={this.i18n('entity.Role.roleType')}
                    enum={RoleTypeEnum}
                    required
                    readOnly={!Utils.Entity.isNew(entity)}/>
                  <Basic.SelectBox
                    ref="superiorRoles"
                    label={this.i18n('entity.Role.superiorRoles')}
                    manager={roleManager}
                    multiSelect
                    readOnly
                    placeholder=""/>
                  <Basic.SelectBox
                    ref="subRoles"
                    label={this.i18n('entity.Role.subRoles')}
                    manager={roleManager}
                    multiSelect/>
                  <Basic.TextArea
                    ref="description"
                    label={this.i18n('entity.Role.description')}/>
                  <Basic.Checkbox
                    ref="disabled"
                    label={this.i18n('entity.Role.disabled')}/>
                </div>

                <h3 style={{ margin: '20px 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                  { this.i18n('setting.approval.header') }
                </h3>
                <Basic.SelectBox
                  labelSpan=""
                  componentSpan=""
                  ref="approveAddWorkflow"
                  label={this.i18n('entity.Role.approveAddWorkflow')}
                  forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.add') }
                  multiSelect={false}
                  manager={workflowProcessDefinitionManager}/>
                <Basic.SelectBox
                  labelSpan=""
                  componentSpan=""
                  ref="approveRemoveWorkflow"
                  label={this.i18n('entity.Role.approveRemoveWorkflow')}
                  forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.remove') }
                  multiSelect={false}
                  manager={workflowProcessDefinitionManager}/>
              </div>

              <div className="col-lg-4">
                <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                  <span dangerouslySetInnerHTML={{ __html: this.i18n('setting.authority.header') }} className="pull-left"/>
                  <Basic.HelpIcon content={authorityHelp} className="pull-right"/>
                  <div className="clearfix"/>
                </h3>
                <AuthoritiesPanel
                  ref="authorities"
                  roleManager={roleManager}
                  authorities={entity.authorities}
                  disabled={!SecurityManager.hasAuthority('ROLE_WRITE')}/>
              </div>
            </Basic.Row>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={_showLoading}>{this.i18n('button.back')}</Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('ROLE_WRITE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.AbstractForm>
      </form>
    );
  }
}


RoleDetail.propTypes = {
  entity: PropTypes.object,
  isNew: PropTypes.bool,
  showLoading: PropTypes.bool
};
RoleDetail.defaultProps = {
  isNew: null
};

export default connect()(RoleDetail);

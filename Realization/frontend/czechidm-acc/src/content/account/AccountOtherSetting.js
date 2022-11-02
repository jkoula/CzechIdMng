import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import { Basic, Utils, Managers, Domain } from 'czechidm-core';
import { AccountManager, SystemEntityManager, SystemManager, SystemMappingManager } from '../../redux';
import AccountTypeEnum from '../../domain/AccountTypeEnum';
import SystemEntityTypeManager from '../../redux/SystemEntityTypeManager';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';
//
const manager = new AccountManager();
const systemEntityManager = new SystemEntityManager();
const systemManager = new SystemManager();
const systemMappingManager = new SystemMappingManager();
const systemEntityTypeManager = new SystemEntityTypeManager();

/**
* Account other setting
*
* @author Roman Kucera
*/
class AccountOtherSetting extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      systemEntity: null,
      _showLoading: false,
      entity: null
    };
  }

  getContentKey() {
    return 'acc:content.accounts.detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    //
    this.setState({
      _showLoading: true
    }, () => {
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (entity) => {
        this.setState({
          entity: entity,
          _showLoading: false,
          accountType: AccountTypeEnum.getNiceLabel(entity?._embedded?.systemMapping?.accountType)
        });
      }));
    });

    this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
    this.selectNavigationItems(['sys-systems-main-menu', 'accounts', 'account-other-setting']);
  }

  getNavigationKey() {
    return 'account-other-setting';
  }

  save(event) {
    const { entity, uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }

    const formEntity = this.refs.form.getData();

    if (!formEntity.entityType) {
      formEntity.entityType = entity.entityType;
    }

    this.setState({
      _showLoading: true
    }, () => {
      this.context.store.dispatch(manager.updateEntity(formEntity, `${uiKey}-other`, this._afterSave.bind(this)));
    });
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        _showLoading: false
      }, () => {
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false,
      entity: entity,
      accountType: AccountTypeEnum.getNiceLabel(entity._embedded.systemMapping.accountType)
    }, () => {
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
      this.forceUpdate();
    });
  }

  onChangeSystemEntity(systemEntity) {
    this.setState({
      systemEntity
    });
  }

  render() {
    const { uiKey, _permissions } = this.props;
    const { _showLoading, entity, accountType } = this.state;

    let forceSearchEntity;
    let forceSearchMappings;
    if (entity) {
      forceSearchEntity = new Domain.SearchParameters()
        .setFilter('systemId', entity.system);

      forceSearchMappings = new Domain.SearchParameters()
        .setFilter('operationType', SystemOperationTypeEnum.findKeyBySymbol(SystemOperationTypeEnum.PROVISIONING))
        .setFilter('systemId', entity.system || Domain.SearchParameters.BLANK_UUID);
    }

    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }
        >
          <Basic.PanelHeader text={this.i18n('tabs.other')} />
          {!entity
            ? <Basic.Loading isStatic show={_showLoading} />
            :
            <Basic.AbstractForm
              ref="form"
              data={entity}>
              <Basic.PanelBody style={{ paddingTop: 30 }} showLoading={_showLoading}>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.Account.system')}
                  readOnly
                  forceSearchParameters={new Domain.SearchParameters(Domain.SearchParameters.NAME_AUTOCOMPLETE)} />
                <Basic.TextField
                  key={accountType}
                  label={this.i18n('acc:entity.Account.accountType')}
                  readOnly
                  value={accountType} />
                <Basic.SelectBox
                  ref="entityType"
                  label={this.i18n('acc:entity.SystemEntity.entityType')}
                  multiSelect={false}
                  manager={systemEntityTypeManager}
                  readOnly />
                <Basic.TextField
                  ref="uid"
                  label={this.i18n('acc:entity.Account.uid')}
                  required
                  max={1000} />
                {
                  !Managers.SecurityManager.hasAuthority('SYSTEMENTITY_READ')
                  ||
                  <Basic.SelectBox
                    ref="systemEntity"
                    manager={systemEntityManager}
                    label={this.i18n('acc:entity.Account.systemEntity')}
                    forceSearchParameters={forceSearchEntity}
                    onChange={this.onChangeSystemEntity.bind(this)} />
                }
                <Basic.SelectBox
                  ref="systemMapping"
                  manager={systemMappingManager}
                  forceSearchParameters={forceSearchMappings}
                  label={this.i18n('acc:entity.RoleSystem.systemMapping')}
                  required />
                <Basic.Checkbox
                  ref="inProtection"
                  label={this.i18n('acc:entity.Account.inProtection')}
                  readOnly />
                <Basic.DateTimePicker
                  mode="datetime"
                  ref="endOfProtection"
                  label={this.i18n('acc:entity.Account.endOfProtection')}
                  readOnly={!entity?.inProtection} />
              </Basic.PanelBody>
            </Basic.AbstractForm>
          }

          <Basic.PanelFooter>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={_showLoading}
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

AccountOtherSetting.propTypes = {
  uiKey: PropTypes.string,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
AccountOtherSetting.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(AccountOtherSetting);

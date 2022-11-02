import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import { Basic, Utils } from 'czechidm-core';
import AccountManager from '../../redux/AccountManager';
//
const manager = new AccountManager();

/**
* Account roles
*
* @author Roman Kucera
*/
class AccountRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.accounts.detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, entity } = this.props;
    //
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, entity || {}, null, () => {
        // this.refs.host.focus();
      }));
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
    }
    this.selectNavigationItems(['sys-systems-main-menu', 'accounts', 'account-roles']);
  }

  getNavigationKey() {
    return 'account-roles';
  }

  save(event) {
    const { uiKey, entity } = this.props;
    const { values, originalValues } = this.state;

    if (event) {
      event.preventDefault();
    }
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.setState({
        _showLoading: false
      }, () => {
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false
    }, () => {
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
    });
  }

  render() {
    const { uiKey, entity, showLoading, _permissions } = this.props;

    //
    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }>
          <Basic.PanelHeader text={this.i18n('tabs.roles')} />
          <Basic.PanelBody style={{ paddingTop: 30}}>
          </Basic.PanelBody>
        </Basic.Panel>
      </form>
    );
  }
}

AccountRoles.propTypes = {
  uiKey: PropTypes.string,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
AccountRoles.defaultProps = {
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

export default connect(select)(AccountRoles);

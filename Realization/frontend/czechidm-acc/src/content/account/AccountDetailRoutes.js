import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { Basic, Advanced, Utils } from 'czechidm-core';
import AccountManager from '../../redux/AccountManager';
import AccountDetail from './AccountDetail';

const manager = new AccountManager();

/**
 * Account detail
 *
 * @author Roman Kucera
 */
class AccountDetailRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.accounts.detail';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    return !!Utils.Ui.getUrlParameter(this.props.location, 'new');
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        {
          <Helmet title={ this.i18n('header') } />
        }
        {
          (this._getIsNew() || !entity)
          ||
          <Advanced.DetailHeader
            entity={ entity }
            showLoading={ showLoading }
            icon="fa:external-link"
            back="/accounts/accounts-all">
            { this.i18n('header', { name: manager.getNiceLabel(entity), escape: false }) }
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <AccountDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="account" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }

      </Basic.Div>
    );
  }
}

AccountDetailRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
AccountDetailRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(AccountDetailRoutes);

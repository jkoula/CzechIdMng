import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { PasswordHistoryManager } from '../../../redux';

const passwordHistoryManager = new PasswordHistoryManager();

/**
* Table of Audit for password change.
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
export class AuditIdentityPasswordChangeTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.audit.identityPasswordChange';
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
    if (this.refs.table !== undefined) {
      this.refs.table.cancelFilter(this.refs.filterForm);
    }
  }

  _getForceSearchParameters() {
    const { id } = this.props;
    let forceSearchParameters = passwordHistoryManager.getDefaultSearchParameters()
      .setFilter('changedAttributesList', ['validFrom']);
    if (id) {
      forceSearchParameters = forceSearchParameters.setFilter('identityId', id);
    }
    return forceSearchParameters;
  }

  render() {
    const { uiKey, singleUserMod } = this.props;
    //
    return (
      <div>
        <Advanced.Table
          header={
            <Basic.Alert level="warning" showHtmlText text={ this.i18n('idmOnlyInfo') } style={{ fontSize: 14, margin: 0 }}/>
          }
          ref="table"
          filterOpened
          uiKey={ uiKey }
          manager={ passwordHistoryManager }
          forceSearchParameters={ this._getForceSearchParameters() }
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.FilterDate ref="fromTill"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 } rendered={ !singleUserMod }>
                    <Advanced.Filter.TextField
                      ref="identityUsername"
                      placeholder={ this.i18n('entity.Identity._type') }
                      help={ this.i18n('content.audit.filter.identity.help') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="creator"
                      placeholder={ this.i18n('content.audit.identityPasswordChange.creator') }
                      help={ this.i18n('content.audit.filter.modifier.help') }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header={ this.i18n('entity.Identity._type') }
            property="identity"
            rendered={ !singleUserMod }
            width={ 250 }
            cell={
              ({ rowIndex, data }) => {
                const identity = data[rowIndex]._embedded.identity;
                if (!identity) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ identity.id }
                    entity={ identity }
                    face="popover"
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="created"
            header={ this.i18n('content.audit.identityPasswordChange.created') }
            width={ 200 }
            sort
            face="datetime"/>
          <Advanced.Column
            property="creator"
            header={ this.i18n('content.audit.identityPasswordChange.creator') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity) {
                  return null;
                }
                const identity = {
                  id: entity.creatorId,
                  username: entity.creator
                };
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ identity.id }
                    entity={ identity }
                    face="popover"
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

AuditIdentityPasswordChangeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  username: PropTypes.string,
  singleUserMod: PropTypes.boolean,
  id: PropTypes.string
};

AuditIdentityPasswordChangeTable.defaultProps = {
  singleUserMod: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AuditIdentityPasswordChangeTable);

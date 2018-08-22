import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import classnames from 'classnames';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
//
import { SecurityManager, RoleCatalogueManager } from '../../redux';

// Table uiKey
const rootsKey = 'role-catalogue-tree-roots';

/**
* Table of roles
*
* @author Radek Tomiška
*/
class RoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      showLoading: true,
      rootNodes: null
    };
    this.roleCatalogueManager = new RoleCatalogueManager();
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._loadCatalogue();
    this.refs.text.focus();
  }

  getContentKey() {
    return 'content.roles';
  }

  _loadCatalogue() {
    if (!SecurityManager.hasAuthority('ROLECATALOGUE_AUTOCOMPLETE')) {
      this.setState({
        showLoading: false
      });
      return;
    }
    //
    const searchParametersRoots = this.roleCatalogueManager.getService().getRootSearchParameters();
    this.context.store.dispatch(this.roleCatalogueManager.fetchEntities(searchParametersRoots, rootsKey, (loadedRoots) => {
      const rootNodes = loadedRoots._embedded[this.roleCatalogueManager.getCollectionType()];
      this.setState({
        rootNodes,
        showLoading: false
      });
    }));
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/role/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/role/${entity.id}/detail`);
    }
  }

  onDelete(bulkActionValue, selectedRows) {
    const { roleManager, uiKey } = this.props;
    const selectedEntities = roleManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: roleManager.getNiceLabel(selectedEntities[0]), records: roleManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: roleManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(roleManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          // redirect to role detail with identities table
          if (error.statusEnum === 'ROLE_DELETE_FAILED_IDENTITY_ASSIGNED') {
            this.context.router.push(`/role/${entity.id}/identities`);
            this.addMessage({
              position: 'tc',
              level: 'info',
              title: this.i18n('delete.identityAssigned.title'),
              message: this.i18n('delete.identityAssigned.message', { role: roleManager.getNiceLabel(entity) })
            });
          } else {
            this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: roleManager.getNiceLabel(entity) }) }, error);
          }
        }
        if (!error && successEntities) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  _useFilterByTree(nodeId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    if (!nodeId) {
      return;
    }
    const data = {
      ... this.refs.filterForm.getData(),
      roleCatalogue: nodeId
    };
    this.refs.roleCatalogue.setValue(nodeId);
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  /**
   * Decorator for Role cataloue tree. Custom icons
   */
  _roleTreeHeaderDecorator(props) {
    const style = props.style;
    const icon = props.node.isLeaf ? 'file-text' : 'folder';
    return (
      <div style={style.base}>
        <div style={style.title}>
          <Basic.Icon type="fa" icon={icon} style={{ marginRight: '5px' }}/>
          <Basic.Button level="link" onClick={this._useFilterByTree.bind(this, props.node.id)} style={{padding: '0px 0px 0px 0px'}}>
            { props.node.name }
            {
              !props.node.childrenCount
              ||
              <small style={{ color: '#aaa' }}>{' '}({props.node.childrenCount})</small>
            }
          </Basic.Button>
        </div>
      </div>
    );
  }

  render() {
    const { uiKey, roleManager, columns, showCatalogue, forceSearchParameters, className } = this.props;
    const { filterOpened, showLoading, rootNodes } = this.state;
    const showTree = showCatalogue && !showLoading && rootNodes && rootNodes.length !== 0;
    return (
      <Basic.Row>
        {
          !showTree
          ||
          <Basic.Col lg={ 3 } style={{ paddingRight: 0, paddingLeft: 0, marginLeft: 15, marginRight: -15 }}>
            <div className="basic-toolbar">
              <div className="pull-left">
                <h3 style={{ margin: 0 }}>{this.i18n('content.roles.roleCataloguePick')}</h3>
              </div>
              <div className="clearfix"></div>
            </div>
            <div style={{ paddingLeft: 15, paddingRight: 15, paddingTop: 15 }}>
              <Basic.Button level="link" className="btn-xs" onClick={ this.cancelFilter.bind(this) }>
                { this.i18n('button.allRoles') }
              </Basic.Button>
              <Advanced.Tree
                ref="roleCatalogueTree"
                rootNodes={ rootNodes }
                headerDecorator={this._roleTreeHeaderDecorator.bind(this)}
                uiKey="roleCatalogueTree"
                manager={this.roleCatalogueManager}
                />
            </div>
          </Basic.Col>
        }

        <Basic.Col lg={!showTree ? 12 : 9 }>
          <Basic.Confirm ref="confirm-delete" level="danger"/>

          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ roleManager }
            rowClass={ ({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); } }
            filterOpened={ filterOpened }
            forceSearchParameters={ forceSearchParameters }
            showRowSelection={ SecurityManager.hasAuthority('ROLE_DELETE') }
            style={ !showTree ? {} : { borderLeft: '1px solid #ddd' } }
            showLoading={ showLoading }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className={ showTree ? '' : 'last'}>
                    <Basic.Col lg={ 8 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('content.roles.filter.text.placeholder')}
                        help={ Advanced.Filter.getTextHelp() }/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } rendered={ false }>
                      <Advanced.Filter.EnumSelectBox
                        ref="roleType"
                        placeholder={this.i18n('entity.Role.roleType')}
                        enum={RoleTypeEnum}/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className={ classnames('last', { 'hidden': !showTree })}>
                    <div className="col-lg-4">
                      <Advanced.Filter.SelectBox
                        ref="roleCatalogue"
                        placeholder={this.i18n('entity.Role.roleCatalogue.name')}
                        manager={ this.roleCatalogueManager }/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { })}
                  rendered={SecurityManager.hasAuthority('ROLE_CREATE')}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            _searchParameters={ this.getSearchParameters() }
            className={ className }>

            <Advanced.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }
              sort={false}/>
            <Advanced.ColumnLink to="role/:id/detail" property="name" width="15%" sort face="text" rendered={_.includes(columns, 'name')}/>
            <Advanced.Column property="roleType" width="75px" sort face="enum" enumClass={RoleTypeEnum} rendered={false && _.includes(columns, 'roleType')}/>
            <Advanced.Column property="roleCatalogue.name" width="75px" face="text" rendered={_.includes(columns, 'roleCatalogue')}/>
            <Advanced.Column property="description" sort face="text" rendered={_.includes(columns, 'description')}/>
            <Advanced.Column property="disabled" sort face="bool" width="75px" rendered={_.includes(columns, 'disabled')}/>
          </Advanced.Table>
        </Basic.Col>
      </Basic.Row>
    );
  }
}

RoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * If role catalogue is shown
   */
  showCatalogue: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Css
   */
  className: PropTypes.string
};

RoleTable.defaultProps = {
  columns: ['name', 'roleType', 'disabled', 'approvable', 'description'],
  filterOpened: true,
  showCatalogue: true,
  forceSearchParameters: null
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { withRef: true })(RoleTable);

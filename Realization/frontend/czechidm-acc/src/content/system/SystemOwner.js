import PropTypes from 'prop-types';
import React from 'react';

import { connect } from 'react-redux';
import { Advanced, Basic } from 'czechidm-core';
import SystemOwnerManager from './SystemOwnerManager';
import _ from 'lodash';
import { IdentityManager, SystemManager} from '../../redux';

    const manager = new SystemOwnerManager();
    const identityManager = new IdentityManager();
    const systemManager = new SystemManager();


    class SystemOwner extends Advanced.AbstractTableContent {
        constructor(props){
            super(props);
            // state = {

            // }
            this.form = React.createRef();
            this.role = React.createRef();
            this.guarantee = React.createRef();
            this.confirmDelete = React.createRef();
        }

    
     getManager() {
        return manager;
          }
        
    componentDidMount() {
        super.componentDidMount()
    }

    getContentKey() {
        return 'content.system.owner';
      }
      getUiKey() {
        return this.props.uiKey;
      }
      getNavigationKey() {
        return this.getRequestNavigationKey('system-owner', this.props.match.params);
      }
      //
      showDetail(entity){
          super.showDetail(entity, () => {
            this.guarantee.current.focus();
          });
      }

    render(){
        const { _showLoading } = this.props;
        return(
            <Basic.Div>
                <Basic.Confirm 
                ref={this.confirmDelete}
                level="danger"/>
                <Advanced.Table
                          ref="table"
                          manager={ manager }
                buttons={
                    [
                      <Basic.Button
                        level="success"
                        key="add_button"
                        className="btn-xs"
                        onClick={this.showDetail.bind(this, this.props.system)}>
                        <Basic.Icon type="fa" icon="plus"/>
                        {' '}
                        {this.i18n('button.add') }
                      </Basic.Button>
                    ]
                  }>
                    <Advanced.Column
                    header=""
                      className="detail-button"
                      cell={
                    ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this,)}
                    sort={false}/>
                );
              }
            }>
                    </Advanced.Column>
                    <Advanced.Column
            property="owner"
            sortProperty="owner.name"
            face="text"
            header={ this.i18n('entity.RoleGuarantee.guarantee.label') }
            sort
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ entity }
                    entity={ entity }
                    face="popover"
                    showIcon/>
                );
              }
            }/>
            <Advanced.Column
            property="type"
            width={ 125 }
            face="text"
            header={ this.i18n('entity.RoleGuarantee.type.label') }
            sort
            // rendered={ guaranteeTypes.length > 0 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Advanced.CodeListValue code="guarantee-type" value={ data[rowIndex][property] }/>
                );
              }
            }
          />
            <Advanced.Column>dfsdfss</Advanced.Column>
                </Advanced.Table>
                
        <Basic.Modal
          bsSize="large"
           show={this.state.detail.show}
        //   onHide={this.closeDetail.bind(this)}
          backdrop="static"
        //   keyboard={!_showLoading}
          >

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header
             closeButton={ !_showLoading } 
             text={ this.i18n('create.header')} 
            //  rendered={ Utils.Entity.isNew(detail.entity) }
            />
            <Basic.Modal.Header
            //   closeButton={ !_showLoading }
            //   text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) }
            //   rendered={ !Utils.Entity.isNew(detail.entity) }
              />
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref={this.form}
                // showLoading={ _showLoading }
                // readOnly={ !manager.canSave(detail.entity, _permissions) }
                >
                    {/* SystemSelect */}
                <Advanced.SystemSelect
                  ref={this.role}
                  manager={ systemManager }
                //   label={ this.i18n('entity.RoleGuaranteeRole.role.label') }
                  readOnly
                //   required
                  />

                  <Advanced.IdentitySelect
                  ref={this.guarantee}
                  manager={ identityManager }
                //   label={ this.i18n('entity.RoleGuarantee.guarantee.label') }
                //   helpBlock={ this.i18n('entity.RoleGuarantee.guarantee.help') }
                //   required
                  />

              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                // onClick={ this.closeDetail.bind(this) }
                // showLoading={ _showLoading }>
                >
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                // // rendered={ manager.canSave(detail.entity, _permissions) }
                // // showLoading={ _showLoading}
                // showLoadingIcon
                // showLoadingText={ this.i18n('button.saving') }
                >
                // {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
            </Basic.Div>

        )
    }
}
function select(state, props) {
    
    return {
        owners: state.data.trimmed.Owners
    };
  }
export default connect(select)(SystemOwner);
// export default SystemOwner;

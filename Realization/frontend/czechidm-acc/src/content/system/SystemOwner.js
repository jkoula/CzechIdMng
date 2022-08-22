import PropTypes from 'prop-types';
import React from 'react';

import { connect } from 'react-redux';
import { Advanced, Basic } from 'czechidm-core';
import SystemOwnerManager from './SystemOwnerManager';
import _ from 'lodash';

const manager = new SystemOwnerManager();
class SystemOwner extends Basic.AbstractContent {

    componentDidMount() {
        super.componentDidMount()
    }

    getContentKey() {
        return 'content.system.owner';
      }
    
      getNavigationKey() {
        return this.getRequestNavigationKey('system-owner', this.props.match.params);
      }
      showDetail(){
        console.log('clicked')
      }
    render(){
        return(
            <Basic.Div>1111
                <Basic.Confirm ref="confirm-delete" level="danger">22222</Basic.Confirm>
                <Advanced.Table
                manager={ manager }
                buttons={
                    [
                      <Basic.Button
                        level="success"
                        key="add_button"
                        className="btn-xs"
                        onClick={ this.showDetail.bind(this,) }>
                        <Basic.Icon type="fa" icon="plus"/>
                        {' '}
                        { this.i18n('button.add') }
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
                    <Advanced.Column>dfsdfss</Advanced.Column>
                </Advanced.Table>
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

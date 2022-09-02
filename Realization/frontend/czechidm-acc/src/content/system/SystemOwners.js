import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic } from 'czechidm-core';
// import * as Basic from '../../components/basic';
import SystemOwnerRole from './SystemOwnerRole';
import SystemOwner from './SystemOwner';
import { SecurityManager} from '../../redux';
import { Domain } from 'czechidm-core';


export default class systemOwners extends Basic.AbstractContent {

    componentDidMount() {
        super.componentDidMount();
      }
    
      getContentKey() {
        return "acc:content.system.owner";
      }
    
      getNavigationKey() {
        return this.getRequestNavigationKey('system-owner-role', this.props.match.params);
      }
    
      render() {
        const forceSearchParameters = new Domain.SearchParameters().setFilter('system', this.props.match.params.entityId);
        //
        return (
          <Basic.Div>
            <Helmet title={ this.i18n('title') } />
            {
            //   !SecurityManager.hasAuthority('ROLEGUARANTEEROLE_READ')
            //   ||
              <Basic.Div>
                <Basic.ContentHeader 
                icon="component:roles" 
                text={ this.i18n('system.header') } 
                style={{ marginBottom: 0 }}/>
                <SystemOwnerRole
                  uiKey="system-owner-role"
                  forceSearchParameters={ forceSearchParameters }
                  className="no-margin"
                  match={ this.props.match }/>
              </Basic.Div>
            }
            {
            //   !SecurityManager.hasAuthority('ROLEGUARANTEE_READ')
            //   ||
              <Basic.Div>
                <Basic.ContentHeader icon="fa:group" text={ this.i18n('identity.header') } style={{ marginBottom: 0 }}/>
                <SystemOwner
                  uiKey="system-owner"
                //   forceSearchParameters={ forceSearchParameters }
                  className="no-margin"
                  match={ this.props.match }/>
              </Basic.Div>
            }
          </Basic.Div>
        );
      }
    }
    
import Div from 'czechidm-core/src/components/basic/Div/Div';
import React from 'react';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import SystemSelect from '../../components/SystemSelect/SystemSelect';
import SystemOwner from './SystemOwner';

export default class SystemOwners extends Basic.AbstractContent {

    componentDidMount() {
        super.componentDidMount();
      }
    
      getContentKey() {
        return "acc:content.system.owners";
      }

      getNavigationKey() {
        return this.getRequestNavigationKey(
          "system-owner-roles",
          this.props.match.params
        );
      }
    render() {
        return(
            <Basic.Div>
                 <Helmet title={ this.i18n('title') } />
                 <Basic.ContentHeader/>
                    <SystemSelect/>
                  <Basic.ContentHeader/>
                     <SystemSelect/>
             </Basic.Div>
        )
    }
}
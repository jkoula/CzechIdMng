import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';

/**
 * 404 error content.
 *
 * @author Radek Tomiška
 */
export default class Error404 extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      id: null // 404 with record id
    };
  }

  getContentKey() {
    return 'content.error.404';
  }

  UNSAFE_componentWillMount() {
    this.selectNavigationItem('home');
    const { query } = this.props.location;
    // 404 with record id
    if (query) {
      this.setState({
        id: query.id
      });
    }
  }

  render() {
    const { id } = this.state;
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <Basic.Alert
          level="warning"
          text={
            !id
            ?
            this.i18n('description')
            :
            this.i18n('record', { id, escape: false })
          }/>
      </div>
    );
  }
}

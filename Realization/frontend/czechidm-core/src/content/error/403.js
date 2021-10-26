import React from 'react';
import Helmet from 'react-helmet';
import { AbstractContent, Alert } from '../../components/basic';

/**
 * 403 error content.
 *
 * @author Radek Tomiška
 */
export default class Error403 extends AbstractContent {

  componentDidMount() {
    this.selectNavigationItem('home');
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('content.error.403.title') } />
        <Alert
          level="danger"
          text={ this.i18n('content.error.403.description') }/>
      </div>
    );
  }
}

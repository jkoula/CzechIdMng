import React from 'react';
import Helmet from 'react-helmet';
import { AbstractContent, Alert } from '../../components/basic';

/**
 * 503 error content.
 *
 * @author Radek Tomiška
 */
export default class Error503 extends AbstractContent {

  componentDidMount() {
    this.selectNavigationItem('home');
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('content.error.503.title') } />
        <Alert
          level="danger"
          text={ this.i18n('content.error.503.description') }/>
      </div>
    );
  }
}

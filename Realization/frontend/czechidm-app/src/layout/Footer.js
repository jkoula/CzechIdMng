import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Managers } from 'czechidm-core';
import packageInfo from '../../package.json';

/**
 * IdM footer with links.
 *
 * @author Radek Tomiška
 */
class Footer extends Basic.AbstractContent {

  showAbout(event) {
    if (event) {
      event.preventDefault();
    }
    this.context.history.replace('/about');
  }

  /**
   * Jump to page top
   */
  jumpTop() {
    $('html, body').animate({
      scrollTop: 0
    }, 'fast');
  }

  render() {
    const { backendVersion, rendered, helpLink, serviceDeskLink } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <footer>
        {
          /*
          TODO: https://v4.mui.com/components/app-bar/#back-to-top

          <div className="pull-right">
            <Basic.Button
              icon="chevron-up"
              aria-label="Left Align"
              onClick={ this.jumpTop.bind(this) }/>
          </div>
          <div className="clearfix" />
          */
        }
        <div>
          {/* RT: version is visible on about page only */}
          <span title={ `${ this.i18n('app.version.backend') }: ${ backendVersion }` } className="hidden">
            { this.i18n('app.version.frontend') } { packageInfo.version }
          </span>
          <span style={{ margin: '0 10px' }} className="hidden">|</span>
          <a
            href={this.i18n('app.author.homePage')}
            target="_blank"
            rel="noopener noreferrer">
            { this.i18n('app.author.name') }
          </a>
          <span style={{ margin: '0 10px' }}>|</span>
          {
            !helpLink
            ||
            <>
              <a
                href={ helpLink }
                target="_blank"
                rel="noopener noreferrer">
                { this.i18n('app.helpDesk') }
              </a>
              <span style={{ margin: '0 10px' }}>|</span>
            </>
          }
          {
            !serviceDeskLink
            ||
            <>
              <a
                href={ serviceDeskLink }
                target="_blank"
                rel="noopener noreferrer">
                { this.i18n('app.serviceDesk') }
              </a>
              <span style={{ margin: '0 10px' }}>|</span>
            </>
          }
          <a href="#" onClick={ this.showAbout.bind(this) } title={ this.i18n('content.about.link') }>
            { this.i18n('content.about.link') }
          </a>
        </div>
      </footer>
    );
  }
}

Footer.propTypes = {
  backendVersion: PropTypes.string,
  rendered: PropTypes.bool
};

Footer.defaultProps = {
  backendVersion: null,
  rendered: true
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    backendVersion: 'x.x.x', // settingManager.getValue(state, 'environment.version')
    helpLink: Managers.ConfigurationManager.getValue(
      state,
      'idm.pub.app.show.footer.help.link',
      'https://wiki.czechidm.com/start'
    ),
    serviceDeskLink: Managers.ConfigurationManager.getValue(
      state,
      'idm.pub.app.show.footer.serviceDesk.link',
      'https://redmine.czechidm.com/projects/czechidmng'
    ),
  };
}

// Wrap the component to inject dispatch and state into it
export default connect(select)(Footer);

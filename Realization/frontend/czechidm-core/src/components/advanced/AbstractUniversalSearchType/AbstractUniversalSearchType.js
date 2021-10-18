import React from 'react';
import PropTypes from 'prop-types';
import AbstractContextComponent from '../../basic/AbstractContextComponent/AbstractContextComponent';
import Icon from '../../basic/Icon/Icon';

/**
 * Abstract universal search type
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
export default class AbstractUniversalSearchType extends AbstractContextComponent {

  getIcon() {
    return 'fa:circle-o';
  }

  getLevel() {
    return null;
  }

  getLabel() {
    return this.i18n('component.advanced.AbstractUniversalSearchType.label');
  }

  getLink(searchValue) {
    return null;
  }

  _stopPropagationMouseDown(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }

  _onClick() {
    this.context.history.push(this.getLink(this.props.searchValue));
  }

  render() {
    const {
      universalSearchType,
      header,
      isLast
    } = this.props;

    if (header) {
      // Header:
      return (
        // eslint-disable-next-line jsx-a11y/no-static-element-interactions
        <div onMouseDown={this._stopPropagationMouseDown.bind(this)}>
          <div style={{display: 'flex', marginBottom: -8}}>
            <span style={{
              width: '100%',
              textAlign: 'center',
              flex: 1
            }}>
              <Icon
                level={this.getLevel()}
                value={this.getIcon()}/>
              {`\u00A0${this.getLabel()}:`}
            </span>
          </div>
          <hr style={{marginTop: 8, marginBottom: 0}}/>
        </div>
      );
    }
    // Footer
    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div onMouseDown={this._stopPropagationMouseDown.bind(this)}>
        <div style={{display: 'flex'}}>
          <span style={{
            width: '100%',
            textAlign: 'center',
            flex: 1
          }}>
            {this.i18n('component.advanced.AbstractUniversalSearchType.showAllLabel')}
            {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events */}
            (
            <a onClick={this._onClick.bind(this)}>
              {universalSearchType.count}
            </a>
            )
          </span>
        </div>
      </div>
    );

  }
}

AbstractUniversalSearchType.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * Universal search type.
   *
   * @type {UniversalSearchDto}
   */
  universalSearchType: PropTypes.object.isRequired,
  /**
   * Searching value.
   */
  searchValue: PropTypes.string
};
AbstractUniversalSearchType.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  header: true,
  isLast: false
};

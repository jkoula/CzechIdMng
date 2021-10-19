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

  getLink() {
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
      header
    } = this.props;

    if (header) {
      // Header:
      return (
        // eslint-disable-next-line jsx-a11y/no-static-element-interactions
        <div onMouseDown={this._stopPropagationMouseDown.bind(this)}>
          <div style={{
            paddingTop: 5}}>
            <span
              className="Uni-search-header"
              style={{
                paddingLeft: 3,
                paddingRight: 3
              }}>
              <Icon
                level={this.getLevel()}
                rendered={false}
                value={this.getIcon()}/>
              {`${this.getLabel()}`}
            </span>
          </div>
          <hr style={{marginTop: -10, marginBottom: 5, marginLeft: -8, marginRight: -8}}/>
        </div>
      );
    }
    // Footer
    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <div onMouseDown={this._stopPropagationMouseDown.bind(this)}>
        <div>
          <span style={{
            width: '100%',
            textAlign: 'center',
            flex: 1
          }}>
            {`${this.i18n('component.advanced.AbstractUniversalSearchType.showAllLabel')}\u00A0`}
            (
            {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events,jsx-a11y/no-static-element-interactions */}
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

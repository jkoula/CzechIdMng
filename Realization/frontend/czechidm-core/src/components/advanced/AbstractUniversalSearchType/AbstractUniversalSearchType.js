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
      universalSearchType
    } = this.props;

    return (
      // eslint-disable-next-line jsx-a11y/no-static-element-interactions
      <span onMouseDown={this._stopPropagationMouseDown.bind(this)}>
        <Icon level={ this.getLevel() } value={ this.getIcon() }/>
        {`  ${this.getLabel()}  `}
        (
        {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events */}
        <a onClick={this._onClick.bind(this)} title={this.i18n('content.about.link')}>
          {universalSearchType.count}
        </a>
          ):
        <hr style={{marginTop: 0, marginBottom: -8}}/>
      </span>
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
  ...AbstractContextComponent.defaultProps
};

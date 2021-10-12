import PropTypes from 'prop-types';
import { Advanced, Basic } from 'czechidm-core';

/**
 * System universal search type
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
export default class SystemUniversalSearchType extends Advanced.AbstractUniversalSearchType {

  getIcon() {
    return 'fa:link';
  }

  getLabel() {
    return this.i18n('acc:component.advanced.SystemUniversalSearchType.label');
  }

  getLink(searchValue) {
    return `/systems?text=${searchValue}`;
  }
}

SystemUniversalSearchType.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
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
SystemUniversalSearchType.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};

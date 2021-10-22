import PropTypes from 'prop-types';
import AbstractContextComponent from '../../components/basic/AbstractContextComponent/AbstractContextComponent';
import AbstractUniversalSearchType from '../../components/advanced/AbstractUniversalSearchType/AbstractUniversalSearchType';

/**
 * Identity universal search type
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
export default class IdentityUniversalSearchType extends AbstractUniversalSearchType {

  getIcon() {
    return 'fa:group';
  }

  getLabel() {
    return this.i18n('component.advanced.IdentityUniversalSearchType.label');
  }

  getLink(searchValue) {
    return `/identities?text=${searchValue}`;
  }
}

IdentityUniversalSearchType.propTypes = {
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
IdentityUniversalSearchType.defaultProps = {
  ...AbstractContextComponent.defaultProps
};

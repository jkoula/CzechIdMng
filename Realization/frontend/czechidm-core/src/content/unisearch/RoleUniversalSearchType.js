import PropTypes from 'prop-types';
import AbstractContextComponent from '../../components/basic/AbstractContextComponent/AbstractContextComponent';
import AbstractUniversalSearchType from '../../components/advanced/AbstractUniversalSearchType/AbstractUniversalSearchType';

/**
 * Identity universal search type
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
export default class RoleUniversalSearchType extends AbstractUniversalSearchType {

  getIcon() {
    return 'fa:key';
  }

  getLevel() {
    return 'warning';
  }

  getLabel() {
    return this.i18n('component.advanced.RoleUniversalSearchType.label');
  }

  getLink(searchValue) {
    return `/roles?text=${searchValue}`;
  }
}

RoleUniversalSearchType.propTypes = {
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
RoleUniversalSearchType.defaultProps = {
  ...AbstractContextComponent.defaultProps
};

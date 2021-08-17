import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as Utils from '../../../utils';
//
import {FormDefinitionManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new FormDefinitionManager();

/**
 * Form attribute basic information (info card).
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
export class FormDefinitionInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    return `/form-definitions/${ encodeURIComponent(this.getEntityId()) }/detail`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.name.label'),
        value: entity.name
      },
      {
        label: this.i18n('entity.type'),
        value: Utils.Ui.getSimpleJavaType(entity.type)
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered).
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:form-definition';
  }
}

FormDefinitionInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
FormDefinitionInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  showLink: true,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const { entityIdentifier, entity } = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  //
  return {
    _entity: manager.getEntity(state, entityId),
    _showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId),
  };
}
export default connect(select)(FormDefinitionInfo);

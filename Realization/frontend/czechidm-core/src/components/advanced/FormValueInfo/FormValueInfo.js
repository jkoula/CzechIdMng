import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { FormAttributeManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';
//
const formAttributeManager = new FormAttributeManager();

/**
 * Component for rendering information about form value.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export class FormValueInfo extends AbstractEntityInfo {

  getManager() {
    return null;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:form-value';
  }

  getNiceLabel(entity) {
    const _entity = this.getEntity(entity);
    if (_entity._embedded && _entity._embedded.formAttribute) {
      return `${ this.i18n('entity.FormValue.value.label') } - ${ formAttributeManager.getNiceLabel(entity._embedded.formAttribute) }`;
    }
    return this.i18n('entity.FormValue.value.label');
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.FormValue._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label" />,
      <Basic.Column property="value" />
    ];
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.FormValue.value.label'),
        value: entity.value
      },
      {
        label: this.i18n('entity.FormAttribute._type'),
        value: (
          <EntityInfo
            entityType="formAttribute"
            entity={ entity._embedded ? entity._embedded.formAttribute : null }
            entityIdentifier={ entity.formAttribute }
            face="popover" />
        )
      },
      {
        label: this.i18n('entity.FormValue.ownerId.label'),
        value: (
          entity._embedded && entity._embedded.owner
          ?
          <EntityInfo
            entityType={ Utils.Ui.getSimpleJavaType(entity._embedded.owner._dtotype) }
            entity={ entity._embedded.owner }
            entityIdentifier={ entity.ownerId }
            face="popover" />
          :
          entity.ownerId
        )
      }
    ];
  }
}

FormValueInfo.propTypes = {
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
FormValueInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true
};

function select() {
  // nothing - preloaded form value is required
}
export default connect(select)(FormValueInfo);

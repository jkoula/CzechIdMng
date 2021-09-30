import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { ProfileManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';
import TwoFactorAuthenticationTypeEnum from '../../../enums/TwoFactorAuthenticationTypeEnum';
//
const manager = new ProfileManager();

/**
 * Component for rendering information about identity profile.
 *
 * @author Radek Tomiška
 * @since 11.3.0
 */
export class ProfileInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    return false;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:cog';
  }

  getNiceLabel(entity) {
    const _entity = entity || this.getEntity();
    let label = this.i18n('entity.Profile._type');
    if (_entity && _entity._embedded && _entity._embedded.identity) {
      label = `${ label } - (${ _entity._embedded.identity.username })`;
    }
    return label;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Profile._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
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
        label: this.i18n('entity.Identity._type'),
        value: (
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded ? entity._embedded.identity : null }
            entityIdentifier={ entity.identity }
            face="popover" />
        )
      },
      {
        label: this.i18n('entity.Profile.preferredLanguage.label'),
        value: entity.preferredLanguage
      },
      {
        label: this.i18n('entity.Profile.systemInformation.label'),
        value: (entity.systemInformation ? this.i18n('label.yes') : this.i18n('label.no'))
      },
      {
        label: this.i18n('entity.Profile.twoFactorAuthenticationType.label'),
        value: (
          <Basic.EnumValue
            enum={ TwoFactorAuthenticationTypeEnum }
            value={ entity.twoFactorAuthenticationType }/>
        )
      }
    ];
  }
}

ProfileInfo.propTypes = {
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
ProfileInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(ProfileInfo);

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Managers, Utils } from 'czechidm-core';
import { Link } from 'react-router-dom';
import SystemAttributeMappingManager from '../../redux/SystemAttributeMappingManager';

const uiKey = 'references-mapping';

const scriptManager = new Managers.ScriptManager();
const systemAttributeMappingManager = new SystemAttributeMappingManager();

class ScriptReferencesMapping extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.script.references';
  }

  getUiKey() {
    return uiKey;
  }

  componentDidMount() {
    super.componentDidMount();
    this._initComponent(this.props);
  }

  _initComponent(props) {
    if (props._entity && props._entity.code) {
      this.context.store.dispatch(systemAttributeMappingManager.getScriptUsage(props._entity.code, uiKey));
    }
  }

  getNavigationKey() {
    return 'references-mapping';
  }

  _getSystemMappingAttributeLink(attributeMapping) {
    if (!attributeMapping?._embedded?.systemMapping?._embedded?.objectClass?.system || !attributeMapping.id) {
      return null;
    }

    return (
      <Link to={`/system/${attributeMapping._embedded.systemMapping._embedded.objectClass.system}/attribute-mappings/${attributeMapping.id}/detail`} >
        {attributeMapping.name}
      </Link>
    );
  }

  _getSystemMappingLink(attributeMapping) {
    if (!attributeMapping?._embedded?.systemMapping?._embedded?.objectClass?.system || !attributeMapping.systemMapping) {
      return null;
    }

    return (
      <Link to={`/system/${attributeMapping._embedded.systemMapping._embedded.objectClass.system}/mappings/${attributeMapping.systemMapping}/detail`} >
        {attributeMapping._embedded.systemMapping.name}
      </Link>
    );
  }

  _getSystemLink(attributeMapping) {
    if (!attributeMapping?._embedded?.systemMapping?._embedded?.objectClass?.system) {
      return null;
    }

    return (
      <Link to={`/system/${attributeMapping._embedded.systemMapping._embedded.objectClass.system}/detail`} >
        {attributeMapping._embedded.systemMapping._embedded.objectClass._embedded.system.name}
      </Link>
    );
  }

  render() {
    const { _entity, _usageMapping } = this.props;
    if (!_entity || !_usageMapping) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <Basic.ContentHeader icon="component:script" text={ this.i18n('header', { escape: false }) } style={{ marginBottom: 0 }}/>

        <Basic.Table
          ref="table"
          uiKey={ this.getUiKey() }
          data={ _usageMapping }>
          <Basic.Column
            cell={
              ({ rowIndex, data }) => {
                return this._getSystemMappingAttributeLink(data[rowIndex]);
              }
            }
            header={ this.i18n('acc:content.system.attributeMappingDetail.title') }/>
          <Basic.Column
            cell={
              ({ rowIndex, data }) => {
                return this._getSystemMappingLink(data[rowIndex]);
              }
            }
            header={ this.i18n('acc:content.system.mappings.title') }/>
          <Basic.Column
            cell={
              ({ rowIndex, data }) => {
                return this._getSystemLink(data[rowIndex]);
              }
            }
            header={ this.i18n('acc:entity.System.name') }/>
        </Basic.Table>
      </Basic.Div>
    );
  }
}

ScriptReferencesMapping.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string),
  uiKey: PropTypes.string.isRequired,
  script: PropTypes.object.isRequired,
  _usageMapping: PropTypes.arrayOf(PropTypes.object),
  rendered: PropTypes.bool.isRequired
};
ScriptReferencesMapping.defaultProps = {
  _entity: null,
  _permissions: null,
  _usageMapping: null,
  rendered: true
};

function select(state, component) {
  return {
    _entity: scriptManager.getEntity(state, component.match.params.entityId),
    _permissions: scriptManager.getPermissions(state, null, component.match.params.entityId),
    _usageMapping: Utils.Ui.getEntities(state, uiKey)
  };
}

export default connect(select)(ScriptReferencesMapping);

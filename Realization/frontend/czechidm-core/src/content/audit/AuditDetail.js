import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import { AuditManager, DataManager } from '../../redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import AuditDetailTable from './AuditDetailTable';
import AuditDetailInfo from './AuditDetailInfo';

const auditManager = new AuditManager();

/**
 * uiKey for diff detail values
 */
const AUDIT_DETAIL_DIFF = 'auditDiff';

/**
 * uiKey for previous version
 */
const AUDIT_PREVIOUS_VERSION = 'auditPreviousVersion';

const FIRST_ENTITY_UIKEY = 'firstEntityUiKey';
const SECOND_ENTITY_UIKEY = 'secondEntityUiKey';

/**
 * Audit detail content.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class AuditDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // showLoadingSelect is used for reload versions and diff
    this.state = {
      noVersion: false,
      showLoadingSelect: true
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._reloadComponent(this.props);
  }

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-entities';
  }

  _reloadComponent(props) {
    const { entityId, revID } = props.match.params;
    // fetch first audit detail
    this.context.store.dispatch(auditManager.fetchEntity(entityId, FIRST_ENTITY_UIKEY));
    if (revID) {
      // if exist revID (params), fetch second audit detail, also is needed set showLoadingSelect
      this.context.store.dispatch(auditManager.fetchEntity(revID, SECOND_ENTITY_UIKEY, () => {
        // just set showLoadingSelect to false, we have second revision, or not?
        this.setState({
          showLoadingSelect: false
        });
      }));
      // fetch diff between audit details
      this.context.store.dispatch(auditManager.fetchDiffBetweenVersion(entityId, revID, AUDIT_DETAIL_DIFF));
    } else {
      // fetch previous version, revID not exist
      this.context.store.dispatch(auditManager.fetchPreviousVersion(entityId, AUDIT_PREVIOUS_VERSION, (previousVersion) => {
        // if previousVersion is null then audit detail first hasn't other version
        if (previousVersion === null) {
          this.setState({
            noVersion: true,
            showLoadingSelect: false
          });
        }
        if (previousVersion) {
          this.context.history.replace(`/audit/entities/${entityId}/diff/${previousVersion.id}`);
          this.setState({
            noVersion: false
          });
        }
      }));
    }
  }

  changeSecondRevision(rev) {
    const { entityId, revID } = this.props.match.params;
    // if bouth ids are same do nothing
    if (parseInt(revID, 10) !== rev.id) {
      this.setState({
        noVersion: false,
        showLoadingSelect: true
      });
      if (rev) {
        this.context.history.replace(`/audit/entities/${entityId}/diff/${rev.id}`);
      } else {
        this.context.history.replace(`/audit/entities/${entityId}/diff`);
      }
    }
  }

  /**
   * Method check if version types are same
   */
  _sameType(firstVersion, secondRevision) {
    return firstVersion && secondRevision && firstVersion.entityId === secondRevision.entityId;
  }

  render() {
    const { auditDetailSelected, auditDetailSecond, diffValues, previousVersion } = this.props;
    const { noVersion, showLoadingSelect } = this.state;
    const auditDetailSecondFinal = auditDetailSecond !== null ? auditDetailSecond : previousVersion;
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />

        <Advanced.DetailHeader
          icon="eye-open">
          { this.i18n('header') }
          { ' ' }
          <small>{ this.i18n('detail') }</small>
        </Advanced.DetailHeader>

        <Basic.Panel>
          <Basic.PanelBody style={{ padding: '0 15px' }}>
            <Basic.Row>
              <Basic.Col md={ 6 }>
                <Basic.ContentHeader
                  className="marginable"
                  icon="component:audit">
                  { this.i18n('previousRevision') }
                </Basic.ContentHeader>
                {
                  noVersion
                  ?
                  <Basic.Alert text={ this.i18n('noPreviousRevision') } className="no-margin"/>
                  :
                  <Basic.Div>
                    <AuditDetailInfo
                      ref="detailSecond"
                      auditDetail={ auditDetailSecondFinal }
                      noVersion={ noVersion }
                      showLoading={ showLoadingSelect }
                      cbChangeSecondRev={ this.changeSecondRevision.bind(this) }
                      auditManager={ auditManager }
                      forceSearchParameters={
                        auditManager
                          .getDefaultSearchParameters()
                          .setFilter('entityId', auditDetailSelected ? auditDetailSelected.entityId : null)
                      } />
                    <AuditDetailTable
                      showLoading={ showLoadingSelect }
                      detail={ this._sameType(auditDetailSelected, auditDetailSecondFinal) ? auditDetailSecondFinal : null }
                      diffValues={ diffValues ? diffValues.diffValues : null }/>
                  </Basic.Div>
                }
              </Basic.Col>
              <Basic.Col md={ 6 }>
                <Basic.ContentHeader
                  className="marginable"
                  icon="fa:arrow-right">
                  { this.i18n('selectedRevision') }
                </Basic.ContentHeader>
                <AuditDetailInfo
                  ref="detailSelecter"
                  showLoading={ auditDetailSelected === null }
                  auditDetail={ auditDetailSelected }
                  auditManager={ auditManager } />
                <AuditDetailTable
                  detail={ auditDetailSelected }
                  showLoading={ auditDetailSelected === null }
                  diffValues={ diffValues ? diffValues.diffValues : null } />
              </Basic.Col>
            </Basic.Row>
          </Basic.PanelBody>
          <Basic.PanelFooter>
            <Basic.Button
              type="button"
              level="link"
              onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

AuditDetail.propTypes = {
  auditDetailSelected: PropTypes.object
};

AuditDetail.defaultProps = {
};

function select(state, component) {
  const { entityId, revID } = component.match.params;

  return {
    userContext: state.security.userContext,
    auditDetailSelected: auditManager.getEntity(state, entityId),
    auditDetailSecond: auditManager.getEntity(state, revID),
    previousVersion: DataManager.getData(state, AUDIT_PREVIOUS_VERSION),
    diffValues: DataManager.getData(state, AUDIT_DETAIL_DIFF)
  };
}

export default connect(select)(AuditDetail);

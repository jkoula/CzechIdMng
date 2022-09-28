import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
//
import { Basic, Utils } from 'czechidm-core';
import AccountManager from '../../redux/AccountManager';
//
const manager = new AccountManager();

/**
* Account detail
*
* @author Roman Kucera
*/
class AccountDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      values: [],
      showEdit: false
    };
  }

  getContentKey() {
    return 'acc:content.accounts';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew, entity } = this.props;
    //
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, entity || {}, null, () => {
        // this.refs.host.focus();
      }));
    } else {
      this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, () => {
        // this.refs.host.focus();
      }));
      // load values from eav
      manager.getService()
          .getConnectorObject(entityId)
          .then(json => {
            var key = 0;
            const values = [];
            if (json) {
              json.attributes.forEach(item => {
                let value;
                if (item.values) {
                  value = item.values[0];
                }
                const valuetoInsert = {
                  key: key,
                  name: item.name,
                  value: value
                }
                values[key] = valuetoInsert;
                key++;
              });
            }

            this.setState({values: values});
          })
          .catch(error => {
            this.addError(error);
          });
    }
    this.selectNavigationItems(['sys-systems-main-menu', 'accounts']);
  }

  getNavigationKey() {
    return 'account-detail';
  }

  save(event) {
    const { uiKey } = this.props;
    const { values } = this.state;

    if (event) {
      event.preventDefault();
    }
    
    // TODO
    // values are in list
    // Form definition is in entity but without attribtues
    // get attributes
    // by name get ID odf attribute 
    // create ne list of formvalues
    // craete forminstance with definition and values 
    // new FormInstance(entity._embedded.formDefinition, this.getValues())
    // set instance to entity and saev it like that
    // entity._eav: [formInstance]

    // this.setState({
    //   _showLoading: true
    // }, () => {
    //   this.refs.form.processStarted();
    //   const entity = this.refs.form.getData();
    //   //
    //   // this.context.store.dispatch(manager.updateEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
    //   // TODO save values
    //   console.log("save");
    // });
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.setState({
        _showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false
    }, () => {
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
      this.refs.password.openConfidential(false);
      if (isNew) {
        this.context.history.replace(`/remote-servers/${ entity.id }/detail`);
      }
    });
  }

  edit() {
    const {showEdit} = this.state

    this.setState({
      showEdit: !showEdit
    });
  }

  valueChange(event, key) {
    const { values } = this.state;

    // validate input

    values[key] = {key: key,
      name: values[key].name,
      value: event.target.value};

    this.setState({
      values: values
    });

  }

  render() {
    const { uiKey, entity, showLoading, _permissions } = this.props;
    const { values, showEdit } = this.state;

    //
    return (
      <form onSubmit={ this.save.bind(this) }>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }>
          <Basic.Div>
            <Grid container spacing={1}>
              <Grid container item xs={12} spacing={3}>
                <Grid item xs={1} >
                  <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={ this.edit.bind(this)}
                  icon="fa:plus">
                  {showEdit
                          ? 'view'
                          : 'edit'
                        }
                    </Basic.Button>
                </Grid>
                <Grid item xs={1}>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  icon="fa:plus"
                  rendered={!showEdit}>
                  { 'refersh' }
                  </Basic.Button>
                  <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  icon="fa:plus"
                  onClick={ this.save.bind(this)}
                  rendered={showEdit}>
                  { 'save' }
                  </Basic.Button>
                </Grid>
                <Grid item xs={2}>
                <TextField id="outlined-basic" label="Outlined" variant="outlined" />
                </Grid>
                <Grid item xs={2}>
                <TextField id="outlined-basic" label="Outlined" variant="outlined" />
                </Grid>
                <Grid item xs={2}>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  icon="fa:plus">
                  { 'cancel filter' }
                    </Basic.Button>
                </Grid>
                <Grid item xs={2}>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  icon="fa:plus">
                    {'refersh'}
                    </Basic.Button>
                  </Grid>
                </Grid>
            </Grid>
          </Basic.Div>
          <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('detail.title') } />
          <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
              <Grid container spacing={1}>
                <Grid container item xs={12} spacing={3}>
                  {
                    values.map(item => (
                      <Grid item xs={4}>
                        <p className='account-detail-label'>{item.name}</p>
                        {showEdit
                          ? <TextField onChange={(e) => this.valueChange(e, item.key)} id="outlined-basic" label="Outlined" variant="outlined" defaultValue={item.value} />
                          : item.value
                        }
                      </Grid>
                    ))
                  }
                </Grid>
              </Grid>
          </Basic.PanelBody>
        </Basic.Panel>
      </form>
    );
  }
}

AccountDetail.propTypes = {
  uiKey: PropTypes.string,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
AccountDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  const entity = manager.getEntity(state, entityId);
  //
  return {
    entity,
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(AccountDetail);

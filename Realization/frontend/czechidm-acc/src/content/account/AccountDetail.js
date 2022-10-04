import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Badge from '@material-ui/core/Badge';
import InputLabel from '@material-ui/core/InputLabel';
//
import { Basic, Utils, Managers } from 'czechidm-core';
import AccountManager from '../../redux/AccountManager';
//
const manager = new AccountManager();
const formDefinitionManager = new Managers.FormDefinitionManager();

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
      originalValues: [],
      showEdit: false,
      changed: false
    };
  }

  getContentKey() {
    return 'acc:content.accounts.detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    //
    this.getLogger().debug(`[FormDetail] loading entity detail [id:${entityId}]`);
    this.refresh();
    this.selectNavigationItems(['sys-systems-main-menu', 'accounts', 'account-detail']);
  }

  getNavigationKey() {
    return 'account-detail';
  }

  save(event) {
    const { uiKey, entity } = this.props;
    const { values, originalValues } = this.state;

    if (event) {
      event.preventDefault();
    }

    this.setState({
      _showLoading: true
    }, () => {
      //
      this.context.store.dispatch(formDefinitionManager.fetchEntity(entity.formDefinition, null, (result) => {
        const formValues = [];
        result.formAttributes.forEach(attribute => {
          values.forEach(value => {
            // TODO maybe check overriden to
            if (originalValues[value.key].value !== value.value) {
              if (value.name === attribute.code) {
                switch (attribute.persistentType) {
                  case "SHORTTEXT":
                    formValues[value.key] = {
                      formAttribute: attribute.id,
                      shortTextValue: value.value,
                      _embedded: {
                        formAttribute: attribute
                      }
                    };
                    break;
                  case "BOOLEAN":
                    formValues[value.key] = {
                      formAttribute: attribute.id,
                      booleanValue: value.value,
                      _embedded: {
                        formAttribute: attribute
                      }
                    };
                    break;
                  // TODO add more attributes, multivalue to
                  default:
                    formValues[value.key] = {
                      formAttribute: attribute.id,
                      shortTextValue: value.value,
                      _embedded: {
                        formAttribute: attribute
                      }
                    };
                }
              }
            }
          });
        });
        entity._eav = [{
          formDefinition: result,
          values: formValues
        }]
        this.context.store.dispatch(manager.updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
      }));
    });
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
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false
    }, () => {
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
    });
  }

  edit() {
    const { showEdit } = this.state

    this.setState({
      showEdit: !showEdit
    });
  }

  valueChange(event, key) {
    const { values } = this.state;

    // validate input

    values[key] = {
      key: key,
      name: values[key].name,
      value: event.target.value,
      overridenValue: values[key].overridenValue,
    };

    this.setState({
      values: values,
      changed: true
    });

  }

  discard() {
    const { showEdit } = this.state

    this.refs['confirm-discard'].show(
      this.i18n('discard.text'),
      this.i18n('discard.header')
    ).then(() => {
      this.setState({
        showEdit: !showEdit,
        changed: false
      });
    }, () => {
      // nothing
    });


  }

  refresh() {
    const { entityId } = this.props.match.params;

    this.setState({
      _showLoading: true
    }, () => {
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (entity) => {
        manager.getService()
          .getFormValues(entityId, entity.formDefinition)
          .then(eavValues => {
            // transform values from connector object to array, merge with eavs
            manager.getService()
              .getConnectorObject(entityId)
              .then(json => {
                var key = 0;
                const values = [];
                if (json) {
                  json.attributes.forEach(item => {
                    eavValues.values.forEach(eavValue => {
                      let value;
                      if (item.values) {
                        // TODO solve multivalue
                        value = item.values[0];
                      }
  
                      let overridenValue;
                      if (eavValue._embedded.formAttribute.code === item.name) {
                        overridenValue = eavValue.value;
                      }
  
                      const valuetoInsert = {
                        key: key,
                        name: item.name,
                        value: value,
                        overridenValue: overridenValue
                      }
                      values[key] = valuetoInsert;
                      key++;
                    });
                  });
                }
  
                this.setState({
                  values: values,
                  originalValues: [...values],
                  _showLoading: false
                });
              })
              .catch(error => {
                this.addError(error);
              });
          })
          .catch(error => {
            manager.getService()
              .getConnectorObject(entityId)
              .then(json => {
                var key = 0;
                const values = [];
                if (json) {
                  json.attributes.forEach(item => {
                    let value;
                    if (item.values) {
                      // TODO solve multivalue
                      value = item.values[0];
                    }
  
                    let overridenValue;
  
                    const valuetoInsert = {
                      key: key,
                      name: item.name,
                      value: value,
                      overridenValue: overridenValue
                    }
                    values[key] = valuetoInsert;
                    key++;
                  });
                }
  
                this.setState({
                  values: values,
                  originalValues: [...values],
                  _showLoading: false
                });
              })
              .catch(error => {
                this.addError(error);
              });
          });
      }));
    });
  }

  render() {
    const { entity } = this.props;
    const { values, showEdit, changed, _showLoading } = this.state;

    //
    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.Confirm ref="confirm-discard" level="danger">
          <Basic.Div style={{ marginTop: 20 }}>
            <Basic.AbstractForm ref="discard-form" uiKey="confirm-discard" >
              <Basic.Alert
                level="info"
                text={this.i18n('discard.help')} />
            </Basic.AbstractForm>
          </Basic.Div>
        </Basic.Confirm>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }>
          <Basic.Div style={{ paddingTop: 10 }}>
            <Grid container spacing={1}>
              <Grid container item xs={12} spacing={3}>
                <Grid item xs={1} >
                  <Basic.Button
                    level="link"
                    key="add_button"
                    className="btn-xs"
                    onClick={this.edit.bind(this)}
                    icon={showEdit ? "fa:th-list" : "fa:pencil"}
                    disabled={showEdit && changed}>
                    {showEdit
                      ? 'view'
                      : 'edit'
                    }
                  </Basic.Button>
                </Grid>
                <Grid item xs={1}>
                  <Basic.Button
                    level="link"
                    key="add_button"
                    className="btn-xs"
                    icon="fa:sync"
                    rendered={!showEdit}
                    onClick={this.refresh.bind(this)}>
                    {'refresh'}
                  </Basic.Button>
                  <Basic.Button
                    level="link"
                    key="add_button"
                    className="btn-xs"
                    icon="fa:save"
                    onClick={this.save.bind(this)}
                    rendered={showEdit}
                    disabled={!changed}>
                    {'save'}
                  </Basic.Button>
                </Grid>
                <Grid item xs={1}>
                  <Basic.Button
                    level="link"
                    key="add_button"
                    className="btn-xs"
                    icon="fa:trash-alt"
                    onClick={this.discard.bind(this)}
                    rendered={showEdit}
                    disabled={!changed}>
                    {'discard'}
                  </Basic.Button>
                </Grid>
                <Grid item xs={2}>
                  <TextField id="outlined-basic" label="Attribute name" variant="outlined" size="small" />
                </Grid>
                <Grid item xs={2}>
                  <TextField id="outlined-basic" label="Attribute value" variant="outlined" size="small" />
                </Grid>
                <Grid item xs={1}>
                  <Basic.Button
                    level="link"
                    key="add_button"
                    className="btn-xs"
                    style={{ minWidth: 150 }}>
                    {'cancel filter'}
                  </Basic.Button>
                </Grid>
                <Grid item xs={1}>
                  <Basic.Button
                    level="success"
                    key="add_button"
                    className="btn-xs"
                    style={{ marginLeft: 40 }}>
                    {'filter'}
                  </Basic.Button>
                </Grid>
              </Grid>
            </Grid>
          </Basic.Div>
          <Basic.PanelHeader text={this.i18n('title')} />
          <Basic.PanelBody style={{ paddingTop: 30 }} showLoading={_showLoading}>
            <Grid container spacing={1}>
              <Grid container item xs={12} spacing={3}>
                {
                  values.map(item => (
                    <Grid item xs={4}>
                      <InputLabel style={{ fontSize: 'small', paddingBottom: 10 }}>
                        {item.name}
                      </InputLabel>
                      {showEdit
                        ?
                        <div>
                          <TextField onChange={(e) => this.valueChange(e, item.key)} id="outlined-basic" defaultValue={(item.overridenValue ? item.overridenValue : item.value)} size="small" style={{ marginTop: -10 }} />
                          <Basic.Icon style={{ marginLeft: 5 }} level='warning' value='fa:unlink' rendered={item.overridenValue ? true : false} />
                        </div>
                        :
                        <div>
                          <p style={{ fontSize: 16, marginTop: -8, marginRight: 10, display: 'inline-block' }}>{item.overridenValue ? item.overridenValue : item.value}</p>
                          <Basic.Icon style={{ marginLeft: 5 }} level='warning' value='fa:unlink' rendered={item.overridenValue ? true : false} />
                        </div>
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
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(AccountDetail);

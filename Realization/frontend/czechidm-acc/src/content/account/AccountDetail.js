import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import classnames from 'classnames';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Chip from '@material-ui/core/Chip';

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
      changed: false,
      attrName: '',
      attrValue: '',
      rows: [],
      focused: ''
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

    const rows = [];
    originalValues.forEach(orginalValue => {
      if (values[orginalValue.key].reset || (values[orginalValue.key].overridenValue !== orginalValue.overridenValue && values[orginalValue.key].overridenValue !== orginalValue.value &&
        values[orginalValue.key].overridenValue !== undefined)) {
        rows.push({ name: orginalValue.name, original: orginalValue.value, new: values[orginalValue.key].overridenValue, multiValue: orginalValue.multiValue, reset: values[orginalValue.key].reset })
      }
    });

    this.setState({
      rows: rows
    });

    if (rows.length === 0) {
      this.setState({
        showModal: true
      });
    } else {
      this.refs['confirm-save'].show(
        this.i18n('save.text'),
        this.i18n('save.header')
      ).then(() => {
        this.setState({
          _showLoading: true
        }, () => {
          //
          this.context.store.dispatch(formDefinitionManager.fetchEntity(entity.formDefinition, null, (result) => {
            const formValues = [];
            const attributesMap = new Map(
              result.formAttributes.map(attribute => {
                return [attribute.code, attribute];
              }),
            );
            values.forEach(value => {
              // TODO maybe check overriden to
              if (originalValues[value.key].value !== value.value || value.reset) {
                const attribute = attributesMap.get(value.name);
                switch (attribute.persistentType) {
                  case "SHORTTEXT":
                    formValues.push({
                      formAttribute: attribute.id,
                      shortTextValue: value.reset ? null : value.value,
                      _embedded: {
                        formAttribute: attribute
                      }
                    });
                    break;
                  case "BOOLEAN":
                    formValues.push({
                      formAttribute: attribute.id,
                      booleanValue: value.value,
                      _embedded: {
                        formAttribute: attribute
                      }
                    });
                    break;
                  // TODO add more attributes, multivalue to
                }
              }
            });
            entity._eav = [{
              formDefinition: result,
              values: formValues
            }]
            this.context.store.dispatch(manager.updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
          }));
        });
      }, () => {
        // nothing
      });
    }

  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    this.refresh();
    if (error) {
      this.setState({
        _showLoading: false,
      }, () => {
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false,
      showEdit: false,
      changed: false,
      attrName: '',
      attrValue: ''
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
    const { values, originalValues } = this.state;

    // validate input

    const newValue = event.target.value;

    let overridenValue = newValue;
    if ((!originalValues[key].overridenValue && newValue === originalValues[key].value) ||
      (!originalValues[key].overridenValue && newValue === '')) {
      overridenValue = undefined;
    }
    if (originalValues[key].value && newValue === '') {
      overridenValue = '';
    }

    values[key] = {
      key: key,
      name: values[key].name,
      value: newValue,
      overridenValue: overridenValue,
      multiValue: values[key].multiValue,
      reset: false
    };

    this.setState({
      values: values,
      changed: true,
      focused: key
    }, () => {
      this.forceUpdate();
    });

  }

  focusLost() {
    this.setState({
      focused: '',
      attrValue: ''
    });
  }

  discard() {
    const { showEdit, originalValues } = this.state

    this.refs['confirm-discard'].show(
      this.i18n('discard.text'),
      this.i18n('discard.header')
    ).then(() => {
      this.setState({
        showEdit: !showEdit,
        changed: false,
        values: [...originalValues]
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
                    let value;
                    if (item.values) {
                      if (!item.multiValue) {
                        value = item.values[0];
                      } else {
                        let multiValue = '';
                        item.values.forEach(entryValue => {
                          multiValue = multiValue.concat(entryValue + "\n");
                        });
                        value = multiValue.trim();
                      }
                    }

                    let overridenValue;
                    eavValues.values.forEach(eavValue => {
                      if (eavValue._embedded.formAttribute.code === item.name) {
                        overridenValue = eavValue.value;
                      }
                    });

                    const valuetoInsert = {
                      key: key,
                      name: item.name,
                      value: value,
                      overridenValue: overridenValue,
                      multiValue: item.multiValue,
                      reset: false
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
                      overridenValue: overridenValue,
                      multiValue: item.multiValue,
                      reset: false
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

  filterNameChanged(event) {
    this.setState({
      attrName: event.target.value
    });
  }

  filterValueChanged(event) {
    this.setState({
      attrValue: event.target.value
    });
  }

  cancel() {
    this.setState({
      attrName: '',
      attrValue: ''
    });
  }

  stopOverride(event, key) {
    const { values, originalValues } = this.state;

    this.refs['confirm-stop-override'].show(
      this.i18n('override.text'),
      this.i18n('override.header')
    ).then(() => {
      values[key] = {
        key: key,
        name: values[key].name,
        value: originalValues[key].value,
        overridenValue: undefined,
        multiValue: values[key].multiValue,
        reset: true
      };

      this.setState({
        values: values,
        changed: true
      }, () => {
        this.forceUpdate();
      });
    }, () => {
      // nothing
    });
  }

  closeModal() {
    this.setState({
      showModal: false
    });
  }

  render() {
    const { entity } = this.props;
    const { rows, values, showEdit, changed, _showLoading, attrName, attrValue, showModal, focused } = this.state;

    let valuesFiltered = [...values];

    if (attrName) {
      valuesFiltered = values.filter(value => {
        return value.name.toLowerCase().includes(attrName.toLowerCase());
      });
    }
    if (attrValue) {
      valuesFiltered = valuesFiltered.filter(value => {
        if (focused === value.key) {
          return true;
        }
        if (value.overridenValue) {
          return value.overridenValue.toString().toLowerCase().includes(attrValue.toLowerCase());
        }
        if (value.value) {
          return value.value.toString().toLowerCase().includes(attrValue.toLowerCase());
        }
        return false;
      });
    }
    //
    return (
      <form onSubmit={this.save.bind(this)}>
        <Helmet title={this.i18n('tabs.account')} />
        <Basic.Confirm ref="confirm-stop-override" level="info" />
        <Basic.Modal show={showModal} onHide={this.closeModal.bind(this)} >
          <Basic.Modal.Header text={'Zadna zmena'} closeButton />
          <Basic.Modal.Body>
            <Basic.Alert
              level="info"
              text={this.i18n('save.no-change')} />
          </Basic.Modal.Body>
        </Basic.Modal>
        <Basic.Confirm ref="confirm-discard" level="danger">
          <Basic.Div style={{ marginTop: 20 }}>
            <Basic.AbstractForm ref="discard-form" uiKey="confirm-discard" >
              <Basic.Alert
                level="info"
                text={this.i18n('discard.help')} />
            </Basic.AbstractForm>
          </Basic.Div>
        </Basic.Confirm>
        <Basic.Confirm ref="confirm-save" level="danger">
          <Basic.Div style={{ marginTop: 20 }}>
            <Basic.AbstractForm ref="save-form" uiKey="confirm-save" >
              <Basic.Alert
                level="info"
                text={this.i18n('save.help')} />

              <Basic.Table
                data={rows}
                noData={this.i18n('component.basic.Table.noData')}
                className="table-bordered"
                rowClass={'warning'}>
                <Basic.Column
                  property="name"
                  header={'Attribute'}
                  cell={({ rowIndex, data }) => {
                    const row = data[rowIndex];
                    return row.name;
                  }}
                />
                <Basic.Column
                  property="original"
                  header={'Value on system'}
                  cell={({ rowIndex, data }) => {
                    const row = data[rowIndex];

                    if (row.multiValue) {
                      const splitValues = row.original.split('\n');
                      return (
                        <div>
                          {splitValues.map(splitValue => (
                            <Basic.Label
                              style={{ marginRight: 3 }}
                              text={splitValue} />
                          ))
                          }
                        </div>
                      );
                    }

                    return (<Basic.Label
                      text={row.original} />);
                  }}
                />
                <Basic.Column
                  property="new"
                  header={'New value'}
                  cell={({ rowIndex, data }) => {
                    const row = data[rowIndex];

                    if (row.multiValue) {
                      const splitValues = row.new.split('\n');
                      const splitValuesOriginal = row.original.split('\n');
                      return (
                        <div>
                          {splitValues.map(splitValue => (
                            <Basic.Label
                              level={splitValuesOriginal.includes(splitValue) ? 'default' : 'success'}
                              style={{ marginRight: 3 }}
                              text={splitValue} />
                          ))
                          }
                          {splitValuesOriginal.map(splitValue => (
                            <Basic.Label
                              level={splitValues.includes(splitValue) ? 'default' : 'error'}
                              rendered={!splitValues.includes(splitValue)}
                              style={{ marginRight: 3, textDecoration: 'line-through' }}
                              text={splitValue} />
                          ))
                          }
                        </div>
                      );
                    }

                    return (<Basic.Label
                      level={row.new ? (row.original ? 'warning' : 'success') : (row.reset ? 'default' : 'error')}
                      style={row.new || row.reset ? {} : { textDecoration: 'line-through' }}
                      text={row.new ? row.new : row.original} />);
                  }}
                />
              </Basic.Table>


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
                    key="edit_button"
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
                    key="refresh_button"
                    className="btn-xs"
                    icon="fa:sync"
                    rendered={!showEdit}
                    onClick={this.refresh.bind(this)}>
                    {'refresh'}
                  </Basic.Button>
                  <Basic.Button
                    level="link"
                    key="save_button"
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
                    key="discard_button"
                    className="btn-xs"
                    icon="fa:trash-alt"
                    onClick={this.discard.bind(this)}
                    rendered={showEdit}
                    disabled={!changed}>
                    {'discard'}
                  </Basic.Button>
                </Grid>
                <Grid item xs={2}>
                  <TextField id="outlined-basic" label="Attribute name" variant="outlined" size="small" autoComplete='off' onChange={this.filterNameChanged.bind(this)} value={attrName ? attrName : ''} />
                </Grid>
                <Grid item xs={2}>
                  <TextField id="outlined-basic" label="Attribute value" variant="outlined" size="small" autoComplete='off' onChange={this.filterValueChanged.bind(this)} value={attrValue ? attrValue : ''} />
                </Grid>
                <Grid item xs={1}>
                  <Basic.Button
                    level="link"
                    key="cancel_button"
                    className="btn-xs"
                    style={{ minWidth: 150 }}
                    onClick={this.cancel.bind(this)}>
                    {'cancel filter'}
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
                  valuesFiltered.map(item => (
                    <Grid item xs={12}>
                      {showEdit
                        ?
                        <div>
                          {item.multiValue
                            ?
                            <Basic.Div>
                              <p style={{ display: 'inline-block', marginRight: 10, minWidth: '200px' }}>{item.name}</p>
                              <TextField onBlur={this.focusLost.bind(this)} onChange={(e) => this.valueChange(e, item.key)} maxRows={4} autoComplete='off' multiline id="outlined-basic" value={(item.overridenValue != undefined ? item.overridenValue : item.value)} size="small" style={{ marginTop: -10 }} />
                              <Basic.Div rendered={item.overridenValue != undefined ? true : false} style={{ display: 'inline-block', marginTop: -10, marginLeft: 10 }}>
                                <Chip color="secondary" size="small" onDelete={(e) => this.stopOverride(e, item.key)} label='Spravováno ručně' />
                              </Basic.Div>
                            </Basic.Div>
                            :
                            <Basic.Div>
                              <p style={{ display: 'inline-block', marginRight: 10, minWidth: '200px' }}>{item.name}</p>
                              <TextField onBlur={this.focusLost.bind(this)} onChange={(e) => this.valueChange(e, item.key)} id="outlined-basic" autoComplete='off' value={(item.overridenValue != undefined ? item.overridenValue : item.value)} size="small" style={{ marginTop: -10 }} />
                              <Basic.Div rendered={item.overridenValue != undefined ? true : false} style={{ display: 'inline-block', marginTop: -10, marginLeft: 10 }}>
                                <Chip color="secondary" size="small" onDelete={(e) => this.stopOverride(e, item.key)} label='Spravováno ručně' />
                              </Basic.Div>
                            </Basic.Div>
                          }
                        </div>
                        :
                        <div>
                          {item.multiValue
                            ?
                            <Basic.Div>
                              <p style={{ display: 'inline-block', marginRight: 10, minWidth: '200px' }}>{item.name}</p>
                              <TextField disabled maxRows={4} multiline id="outlined-basic" value={(item.overridenValue != undefined ? item.overridenValue : item.value)} size="small" style={{ marginTop: -10 }} />
                              <Basic.Div rendered={item.overridenValue != undefined ? true : false} style={{ display: 'inline-block', marginTop: -10, marginLeft: 10 }}>
                                <Chip color="secondary" size="small" label='Spravováno ručně' />
                              </Basic.Div>
                            </Basic.Div>
                            :
                            <Basic.Div>
                              <p style={{ display: 'inline-block', marginRight: 10, minWidth: '200px' }}>{item.name}</p>
                              <TextField disabled id="outlined-basic" value={(item.overridenValue != undefined ? item.overridenValue : (item.value ? item.value : ''))} size="small" style={{ marginTop: -10 }} />
                              <Basic.Div rendered={item.overridenValue != undefined ? true : false} style={{ display: 'inline-block', marginTop: -10, marginLeft: 10 }}>
                                <Chip color="secondary" size="small" label='Spravováno ručně' />
                              </Basic.Div>
                            </Basic.Div>
                          }
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

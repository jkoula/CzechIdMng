import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import InvertColors from '@material-ui/icons/InvertColors';
import ColorPicker from 'material-ui-color-picker';
import Slider from '@material-ui/core/Slider';
import { makeStyles } from '@material-ui/core/styles';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { SecurityManager, ConfigurationManager, DataManager } from '../../redux';
import { RestApiService } from '../../services';

const configurationManager = new ConfigurationManager();

const useStyles = makeStyles(theme => ({
  root: props => ({
    width: '100%',
    '& .MuiInput-input': { // @TODO: uses classes instead
      backgroundColor: props.parentValue ? props.parentValue : 'transparent',
      borderRadius: theme.shape.borderRadius,
      marginBottom: theme.spacing(0.5),
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
    }
  }),
}));

/**
 * TODO: Move color picker to form component (AbstractFormComponent), if it will be used on other places.
 */
function BasicColorPicker(props) {
  const { name, label, placeholder, value, onChange, parentValue } = props;
  const classes = useStyles({ parentValue });
  //
  return (
    <ColorPicker
      name={ name }
      label={ `${ label } (${ value })` }
      defaultValue={ placeholder }
      value={ value }
      onChange={ onChange }
      className={ parentValue ? classes.root : '' }/>
  );
}

/**
 * Theme configuration.
 *
 * TODO: Move color picker to form component (AbstractFormComponent), if it will be used on other places.
 * TODO: Move slider to form component (AbstractFormComponent), if it will be used on other places.
 *
 * @author Radek Tomiška
 */
class ThemeConfiguration extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(
      this.getManager().fetchEntity('idm.pub.app.show.logo', this.getManager().getApplicationLogoKey(), (entity, error) => {
        if (error) {
          if (error.statusCode === 404) {
            // logo is not configured yet
          } else {
            this.addError(error);
          }
        }
      })
    );
    this.context.store.dispatch(
      this.getManager().fetchEntity('idm.pub.app.show.theme', this.getManager().getApplicationThemeKey(), (entity, error) => {
        if (error && error.statusCode !== 404) { // theme is not configured yet
          this.addError(error);
        }
        // set defaults by effective theme => not related to saved configuration
        const { theme } = this.props;
        this._initDefaultValues(theme);
      })
    );
  }

  _initDefaultValues(theme) {
    this.setState({
      primary: theme.palette.primary.main,
      secondary: theme.palette.secondary.main,
      primaryText: theme.palette.primary.contrastText,
      secondaryText: theme.palette.secondary.contrastText,
      borderRadius: theme.shape.borderRadius
    });
  }

  getNavigationKey() {
    return 'system-configuration';
  }

  getContentKey() {
    return 'content.theme-configuration';
  }

  getManager() {
    return configurationManager;
  }

  /**
   * Dropzone component function called after select file
   * @param file selected file (multiple is not allowed)
   */
  _onDrop(files) {
    if (this.refs.dropzone.state.isDragReject) {
      this.addMessage({
        message: this.i18n('content.identity.profile.fileRejected'),
        level: 'warning'
      });
      return;
    }
    files.forEach((file) => {
      const fileName = file.name.toLowerCase();
      if (!fileName.endsWith('.jpg') && !fileName.endsWith('.jpeg') && !fileName.endsWith('.png') && !fileName.endsWith('.gif')) {
        this.addMessage({
          message: this.i18n('content.identity.profile.fileRejected', {name: file.name}),
          level: 'warning'
        });
        return;
      }
      const objectURL = URL.createObjectURL(file);
      this.setState({
        cropperSrc: objectURL,
        showCropper: true,
        fileName: file.name
      });
    });
  }

  _closeCropper() {
    this.setState({
      showCropper: false
    });
  }

  _crop() {
    this.refs.cropper.crop((formData) => {
      // append selected fileName
      formData.fileName = this.state.fileName;
      formData.name = this.state.fileName;
      formData.append('fileName', this.state.fileName);
      /*
      this.context.store.dispatch(identityManager.uploadProfileImage(this._getIdentityIdentifier(), formData, (profile, error) => {
        if (error) {
          this.addError(error);
          return;
        }
        // new profile can be created => wee need to set id into form
        this.refs.form.setData(profile);
      })); */
    });
    this._closeCropper();
  }

  _deleteLogo() {
    this.refs['confirm-delete'].show(
      this.i18n(`action.deleteLogo.message`),
      this.i18n(`action.deleteLogo.title`)
    ).then(() => {
      // this.context.store.dispatch(identityManager.deleteProfileImage(this._getIdentityIdentifier()));
    }, () => {
      // Rejected
    });
  }

  _resetTheme() {
    this.refs['confirm-reset-theme'].show(
      this.i18n(`action.reset-theme.message`),
      this.i18n(`action.reset-theme.title`)
    ).then(() => {
      this.context.store.dispatch(
        this.getManager().deleteEntity({ id: 'idm.pub.app.show.theme' }, configurationManager.getApplicationThemeKey(), (entity, error) => {
          if (error) {
            if (error.statusCode === 404) { // default theme is already in use
              // TODO: better message
              this.addMessage({});
            } else {
              this.addError(error);
            }
          } else {
            this.context.store.dispatch(this.getManager().fetchApplicationTheme('light', (theme => {
              this._initDefaultValues(theme);
              this.addMessage({});
            })));
          }
        })
      );
    }, () => {
      // Rejected
    });
  }

  _saveTheme() {
    const { theme, themeConfigured, themeEntity } = this.props;
    const { borderRadius, primary, secondary, primaryText, secondaryText } = this.state;
    //
    let _theme = {};
    try {
      _theme = JSON.parse(themeConfigured);
    } catch (syntaxError) {
      _theme = {};
    }
    if (!theme.shape) {
      theme.shape = {};
    }
    _theme.shape.borderRadius = borderRadius;
    //
    if (!theme.palette) {
      theme.palette = {};
    }
    _theme.palette.primary = {};
    _theme.palette.secondary = {};
    _theme.palette.primary.main = primary;
    _theme.palette.secondary.main = secondary;
    _theme.palette.primary.contrastText = primaryText;
    _theme.palette.secondary.contrastText = secondaryText;
    //
    if (Utils.Entity.isNew(themeEntity)) {
      this.context.store.dispatch(
        this.getManager().createEntity(
          {
            name: 'idm.pub.app.show.theme',
            value: JSON.stringify(_theme)
          },
          configurationManager.getApplicationThemeKey(),
          this._afterSaveTheme.bind(this)
        )
      );
    } else {
      themeEntity.value = JSON.stringify(_theme);
      //
      this.context.store.dispatch(
        this.getManager().updateEntity(
          themeEntity,
          configurationManager.getApplicationThemeKey(),
          this._afterSaveTheme.bind(this)
        )
      );
    }
  }

  _afterSaveTheme(entity, error) {
    if (error) {
      this.addError(error);
    } else {
      this.context.store.dispatch(this.getManager().fetchApplicationTheme('light', (theme => {
        this.context.store.dispatch(this.getManager().fetchPermissions(entity.name, configurationManager.getApplicationThemeKey()));
        this._initDefaultValues(theme);
        this.addMessage({});
      })));
    }
  }

  render() {
    const { _logoUrl, _logoLoading, _themeLoading, themeEntity, _themePermissions, _logoPermissions, logoEntity } = this.props;
    const {
      primary,
      secondary,
      primaryText,
      secondaryText,
      borderRadius,
      showCropper,
      cropperSrc
    } = this.state;
    //
    return (
      <Basic.Container component="main" maxWidth="sm">
        <Helmet title={ this.i18n('Theme configuration') } />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-reset-theme" level="secondary"/>
        <Basic.PageHeader icon={<InvertColors/>} text="Theme configuration"/>
        <Basic.ContentHeader text="Logo"/>
        <Basic.LabelWrapper
          helpBlock="Recommended logo size is 165 x 40 px.">
          <div className="profile-image-wrapper" style={{ width: 'auto' }}>
            <Advanced.ImageDropzone
              ref="dropzone"
              accept="image/*"
              multiple={ false }
              onDrop={ this._onDrop.bind(this) }
              showLoading={ _logoLoading }
              readOnly={ !this.getManager().canSave(logoEntity, _logoPermissions) }>
              <img className="img-thumbnail" alt="profile" src={ _logoUrl } />
            </Advanced.ImageDropzone>
            <Basic.Fab
              level="danger"
              className={ _logoUrl ? 'btn-remove' : 'hidden' }
              size="small">
              <Basic.Button
                type="button"
                level="danger"
                buttonSize="xs"
                style={{ color: 'white' }}
                rendered={ !!_logoUrl && this.getManager().canSave(logoEntity, _logoPermissions) }
                titlePlacement="left"
                icon="fa:trash"
                onClick={ this._deleteLogo.bind(this) }/>
            </Basic.Fab>
          </div>

          <Basic.Modal
            bsSize="default"
            show={ showCropper }
            onHide={ this._closeCropper.bind(this) }
            backdrop="static" >
            <Basic.Modal.Body>
              <Advanced.ImageCropper
                ref="cropper"
                src={ cropperSrc }
                autoCropArea={ 1 }
                fixedAspectRatio={ false }
                width={ 165 }
                height={ 40 }/>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this._closeCropper.bind(this) }
                showLoading={ _logoLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                level="info"
                onClick={ this._crop.bind(this) }
                showLoading={ _logoLoading }>
                { this.i18n('button.crop') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </Basic.Modal>
        </Basic.LabelWrapper>

        {
          _themeLoading
          ||
          <Basic.Panel className="last">
            <Basic.PanelBody>
              <Basic.ContentHeader text="Colors" style={{ paddingTop: 0 }}/>
              <Basic.Row>
                <Basic.Col sm={ 6 }>
                  <Basic.LabelWrapper>
                    <BasicColorPicker
                      name="primary"
                      label="Primary"
                      placeholder="Primary color"
                      value={ primary }
                      onChange={ color => this.setState({ primary: color }) }/>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col sm={ 6 }>
                  <Basic.LabelWrapper>
                    <BasicColorPicker
                      name="secondary"
                      label="Secondary"
                      placeholder="Secondary color"
                      value={ secondary }
                      onChange={ color => this.setState({ secondary: color }) }/>
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>
              <Basic.Row>
                <Basic.Col sm={ 6 }>
                  <Basic.LabelWrapper>
                    <BasicColorPicker
                      name="primaryText"
                      label="Primary text"
                      placeholder="Primary contrast text color"
                      value={ primaryText }
                      parentValue={ primary }
                      onChange={ color => this.setState({ primaryText: color }) }/>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col sm={ 6 }>
                  <Basic.LabelWrapper>
                    <BasicColorPicker
                      name="secondaryText"
                      label="Secondary text"
                      placeholder="Secondary contrast text color"
                      value={ secondaryText }
                      parentValue={ secondary }
                      onChange={ color => this.setState({ secondaryText: color }) }/>
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>

              <Basic.ContentHeader text="Border radius"/>
              <Basic.LabelWrapper
                helpBlock="Border radius [px].">
                <Slider
                  value={ borderRadius }
                  aria-labelledby="discrete-slider"
                  valueLabelDisplay="auto"
                  step={ 1 }
                  marks
                  min={ 0 }
                  max={ 20 }
                  onChange={ (e, value) => this.setState({ borderRadius: value }) }
                />
              </Basic.LabelWrapper>
            </Basic.PanelBody>
            <Basic.PanelFooter>
              <Basic.Button
                type="submit"
                level="secondary"
                showLoading={ _themeLoading }
                style={{ marginRight: 5 }}
                onClick={ this._resetTheme.bind(this) }
                disabled={ themeEntity === null }
                rendered={ this.getManager().canSave(themeEntity, _themePermissions) }>
                { this.i18n('Resetovat') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="primary"
                showLoading={ _themeLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                onClick={ this._saveTheme.bind(this) }
                rendered={ this.getManager().canSave(themeEntity, _themePermissions) }>
                { this.i18n('button.set') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        }
      </Basic.Container>
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'),
    userContext: state.security.userContext,
    theme: ConfigurationManager.getApplicationTheme(state),
    themeConfigured: ConfigurationManager.getValue(state, 'idm.pub.app.show.theme'),
    themeEntity: configurationManager.getEntity(state, 'idm.pub.app.show.theme'),
    logoEntity: configurationManager.getEntity(state, 'idm.pub.app.show.logo'),
    _themePermissions: configurationManager.getPermissions(state, configurationManager.getApplicationThemeKey(), 'idm.pub.app.show.theme'),
    _logoPermissions: configurationManager.getPermissions(state, configurationManager.getApplicationLogoKey(), 'idm.pub.app.show.logo'),
    _logoLoading: DataManager.isShowLoading(state, configurationManager.getApplicationLogoKey()),
    _themeLoading: DataManager.isShowLoading(state, configurationManager.getApplicationThemeKey()),
    _logoUrl: ConfigurationManager.getApplicationLogo(state),
  };
}

export default connect(select)(ThemeConfiguration);

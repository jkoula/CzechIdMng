import React from 'react';
import PropTypes from 'prop-types';
import ReactDropzone from 'react-dropzone';
import _ from 'lodash';
//
import { withStyles } from '@material-ui/core/styles';
//
import { i18n } from '../../../services/LocalizationService';
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Well from '../Well/Well';

/**
 * Theme decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const styles = theme => ({
  root: {
    borderRadius: theme.shape.borderRadius
  }
});

/**
* Dropzone component.
*
* @author Vít Švanda
* @author Radek Tomiška
*/
export class Dropzone extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {};
  }

  render() {
    const {
      id,
      onDrop,
      multiple,
      accept,
      style,
      styleActive,
      styleReject,
      showLoading,
      rendered,
      children,
      hidden,
      readOnly,
      classes
    } = this.props;
    // TODO: move to material classes and add theme (e.g. theme.palette.divider)
    const defaultStyle = {
      style: {
        height: 'auto',
        width: 'auto',
        textAlign: 'center',
        borderWidth: '2px',
        borderStyle: 'dashed',
        padding: '30px'
      },
      styleActive: {
        borderColor: '#3d8b3d',
        backgroundColor: 'rgba(92, 184, 92, 0.06)'
      },
      styleReject: {
        borderColor: '#DE140E',
        backgroundColor: 'rgba(217, 79, 79, 0.12)'
      },
      disabledStyle: {
        opacity: '0.5',
        filter: 'alpha(opacity=50)'
      }
    };
    //
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return <Well showLoading/>;
    }
    let content = children;
    if (!content) {
      if (readOnly) {
        content = i18n('component.basic.Dropzone.readOnly');
      } else {
        content = i18n('component.basic.Dropzone.infoText');
      }
    }
    let _id = id;
    if (!_id) {
      _id = _.uniqueId('dropzone_');
    }
    //
    return (
      <div className={ hidden ? 'hidden' : '' }>
        <ReactDropzone
          id={ _id }
          ref="dropzone"
          style={{ ...defaultStyle.style, ...style }}
          activeStyle={ styleActive || defaultStyle.styleActive }
          rejectStyle={ styleReject || defaultStyle.styleReject }
          multiple={ multiple }
          accept={ accept }
          disablePreview
          disabled={ readOnly }
          onDrop={ onDrop }
          className={ classes ? classes.root : null }>
          <div style={{ color: '#777' }}>
            { content }
          </div>
        </ReactDropzone>
      </div>
    );
  }
}

Dropzone.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * Component identifier
   */
  id: PropTypes.string,
  /**
  * Hidden component
  */
  hidden: PropTypes.bool,
  /**
  * Function call after droped or selected any files
  */
  onDrop: PropTypes.func.isRequired,
  /**
  * Can be select multiple files
  */
  multiple: PropTypes.bool,
  /**
  * Define accepted file extension
  */
  accept: PropTypes.string,
  /**
  * Object with styles for dropzone
  */
  style: PropTypes.object,
  /**
  * Object with styles for active state (when are files accepted)
  */
  styleActive: PropTypes.object,
  /**
  * Object with styles for reject state (when are files rejected)
  */
  styleReject: PropTypes.object,
  /**
   * html readonlye]}
   */
  readOnly: PropTypes.bool
};

Dropzone.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  multiple: true,
  hidden: false,
  readOnly: false
};
Dropzone.STYLES = styles;

export default withStyles(styles, { withTheme: true })(Dropzone);

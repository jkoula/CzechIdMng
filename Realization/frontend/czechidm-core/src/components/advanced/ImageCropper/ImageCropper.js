import React from 'react';
import PropTypes from 'prop-types';
import Cropper from 'react-cropper';
//
import * as Basic from '../../basic';
import Well from '../../basic/Well/Well';

/**
* Component for image crop.
*
* @author Petr Hanák
* @author Radek Tomiška
*/
export default class ImageCropper extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      src: null
    };
    this.cropperRef = React.createRef();
  }

  setDragMode(option) {
    this._getCropper().setDragMode(option);
  }

  reset() {
    this._getCropper().reset();
  }

  clear() {
    this._getCropper().clear();
  }

  rotateLeft() {
    this._getCropper().rotate(-90);
  }

  rotateRight() {
    this._getCropper().rotate(90);
  }

  zoomIn() {
    this._getCropper().zoom(0.1);
  }

  zoomOut() {
    this._getCropper().zoom(-0.1);
  }

  crop(cb) {
    const canvas = this._getCropper().getCroppedCanvas({
      width: 300, height: 300
    });
    //
    if (!canvas) {
      this.addMessage({ level: 'warning', message: this.i18n('error.IDENTITYIMAGE_WRONG_FORMAT.message') });
    } else if (canvas.toBlob !== undefined) {
      canvas.toBlob((blob) => {
        const formData = new FormData();
        formData.append('data', blob);
        cb(formData);
      });
    } else if (canvas.msToBlob !== undefined) {
      const formData = new FormData();
      formData.append('data', canvas.msToBlob());
      cb(formData);
    } else {
      // TODO: manually convert Data-URI to Blob for older browsers https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toBlob#Browser_compatibility
      LOGGER.error('[ImageCropper]: toBlog polyfill is not available');
    }
  }

  _getCropper() {
    return this.cropperRef.current.cropper;
  }

  render() {
    const { showLoading, rendered, src } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return <Well showLoading/>;
    }
    //
    return (
      <Basic.Div>
        <Cropper
          ref={ this.cropperRef }
          src={ src }
          viewMode={ 2 }
          dragMode="move"
          style={{ maxHeight: 568 }}
          autoCropArea={ 0.6 }
          aspectRatio={ 1 / 1 } />

        <Basic.Div
          className="btn-group"
          role="group"
          style={{
            padding: 10,
            position: 'absolute',
            bottom: 20,
            left: '50%',
            transform: 'translateX(-50%)'
          }} >
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.setDragMode.bind(this, 'move') }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="arrows" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.setDragMode.bind(this, 'crop') }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="crop" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.zoomIn.bind(this) }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="search-plus" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.zoomOut.bind(this) }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="search-minus" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.rotateLeft.bind(this) }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="rotate-left" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.rotateRight.bind(this) }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="rotate-right" />
          </Basic.Button>
          <Basic.Button
            type="button"
            level="info"
            onClick={ this.reset.bind(this) }
            className="btn-sm" >
            <Basic.Icon type="fa" icon="reply-all" />
          </Basic.Button>
        </Basic.Div>
      </Basic.Div>
    );
  }
}

ImageCropper.PropTypes = {
  /**
  * Rendered component
  */
  rendered: PropTypes.bool,
  /**
  * Show loading in component
  */
  showLoading: PropTypes.bool,
};

ImageCropper.defaultProps = {
  rendered: true,
  showLoading: false,
};

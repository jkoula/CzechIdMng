# ImageCropper

> JavaScript image cropper.

Based on: https://github.com/react-cropper/react-cropper
Doc: https://github.com/fengyuanchen/cropperjs

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| src | string |  url to image (objectUrl)  |  |
| autoCropArea | nubmer |  It should be a number between 0 and 1. Define the automatic cropping area size (percentage).  | 0.6 |
| fixedAspectRatio | bool |  Free or fixed crop box ratio.  | true |
| width | nubmer | Crop box ratio width.  | 300 |
| height | nubmer |  Crop box ratio height.  | 300 |

## Usage

```javascript
...
this.setState({
  cropperSrc: URL.createObjectURL(file) // e.g. file from Advanced.Dropzone component
});

...
_crop() {
  this.refs.cropper.crop((formData) => {
    // append selected fileName
    formData.fileName = this.state.fileName;
    formData.name = this.state.fileName;
    formData.append('fileName', this.state.fileName);
    //
    // ... and use formData for upload to BE
  });
}
...
```

```html
<Advanced.ImageCropper
  ref="cropper"
  src={ cropperSrc }/>
<Basic.Button
  level="info"
  onClick={ this._crop.bind(this) }
  showLoading={ showLoading }>
  { this.i18n('button.crop') }
</Basic.Button>  
```

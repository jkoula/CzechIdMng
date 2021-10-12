# ProgressBar Component

Wrapped Material-UI Progress.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| min | number | Start count | 0 |
| max | number.isRequired | End count |  |
| now | number | Actual counter | 0 |
| label | oneOfType([string, bool])] | Label |  |
| active | bool | Adds animation, when max is not given. | true |
| bsStyle | string | Color of progress bar (success, info, error, warning) | info |

## Usage

```html
<Basic.ProgressBar min={0} max={4} now={2} />
```

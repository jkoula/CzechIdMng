import React from 'react';
import classnames from 'classnames';
//
import * as Basic from '../../basic';
import { i18n } from '../../../services/LocalizationService';

/**
 * ProgressBar with default label from localization.
 *
 * @author Radek Tomiška
 */
export default function ProgressBar(props) {
  const {
    rendered,
    active,
    className,
    bars,
    showLoading,
    label,
    min,
    max,
    now,
    style,
    bsStyle,
    children
  } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  // add component className
  const classNames = classnames(
    'advanced-progress-bar',
    className
  );
  //
  //
  let _label = label;
  const _barLabels = [];
  let _now = now;
  let _bsStyle = bsStyle;
  if (bars) {
    _now = 0;
    bars.forEach(bar => {
      if (bar.now > 0) {
        _now += bar.now;
        if (_bsStyle !== 'error' && _bsStyle !== 'danger' && (bar.bsStyle === 'warning' || bar.bsStyle === 'danger' || bar.bsStyle === 'error')) {
          // ~ error has higher priority
          _bsStyle = bar.bsStyle;
        }
        _barLabels.push(
          <Basic.Label
            level={ bar.bsStyle }
            value={ bar.now }
            style={{ marginLeft: 3 }}/>
        );
      }
    });
    if (_barLabels.length > 0) {
      _label = [];
      _label.push(i18n('component.basic.ProgressBar.processed'));
      _barLabels.forEach(barLabel => {
        _label.push(barLabel);
      });
      _label.push(` / ${ max || '?' }`);
    }
  }
  if (!_label) { // label was not given
    // resolve default label from localization
    if ((_now === 0 || max === 0) && active) {
      // start label
      _label = i18n('component.basic.ProgressBar.start');
    } else {
      _label = i18n('component.basic.ProgressBar.label', {
        escape: false,
        min,
        max: (max === null ? '?' : max || '?'),
        now: (_now === null ? '?' : _now)
      });
    }
  }
  //
  return (
    <span className={ classNames }>
      <Basic.ProgressBar
        showLoading={ showLoading }
        min={ min }
        max={ max }
        now={ _now }
        active={ active }
        style={ style }
        bsStyle={ _bsStyle }>
        { children }
      </Basic.ProgressBar>
      <div className="text-center" style={{ marginTop: 10 }}>
        { _label }
      </div>
    </span>
  );
}

ProgressBar.propTypes = {
  ...Basic.ProgressBar.propTypes
};

ProgressBar.defaultProps = {
  ...Basic.ProgressBar.defaultProps
};

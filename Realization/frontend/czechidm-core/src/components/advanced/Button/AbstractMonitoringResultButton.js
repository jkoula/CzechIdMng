import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';

/**
 * Target button for monitoring result.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
export default class AbstractMonitoringResultButton extends Basic.AbstractContextComponent {

  /**
   * Button icon.
   *
   * @param  {IdmMonitoringResultDto} monitoringResult monitoring result
   * @return {string} icon
   */
  getIcon(/* monitoringResult */) {
    return 'fa:angle-double-right';
  }

  /**
   * Button level.
   *
   * @param  {IdmMonitoringResultDto} monitoringResult monitoring result
   * @return {string}                  level
   */
  getLevel(monitoringResult) {
    if (!monitoringResult) {
      return null;
    }
    if (!monitoringResult.level) {
      return monitoringResult.level;
    }
    if (!monitoringResult.result || !monitoringResult.result.model) {
      return null;
    }
    return monitoringResult.result.model.level;
  }

  /**
   * Button label.
   *
   * @param  {IdmMonitoringResultDto} monitoringResult monitoring result
   * @return {string} label
   */
  getLabel(/* monitoringResult */) {
    return this.i18n('component.advanced.EntityInfo.link.detail.label');
  }

  /**
   * Button onClick function.
   *
   * @param  {IdmMonitoringResultDto} monitoringResult monitoring result
   */
  onClick(/* monitoringResult, event */) {
    // override if needed
  }

  render() {
    const { monitoringResult, buttonSize, onClick } = this.props;
    //
    return (
      <Basic.Button
        icon={ this.getIcon(monitoringResult) }
        level={ this.getLevel(monitoringResult) || 'default' }
        buttonSize={ buttonSize }
        text={ this.getLabel(monitoringResult) }
        onClick={ (event) => {
          if (onClick) {
            onClick();
          }
          this.onClick(monitoringResult, event);
        }}/>
    );
  }
}

AbstractMonitoringResultButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Moniroting result.
   *
   * @type {IdmMonitoringResultDto}
   */
  monitoringResult: PropTypes.object.isRequired,
  /**
   * Button size (by bootstrap).
   */
  buttonSize: PropTypes.oneOf(['default', 'xs', 'sm', 'lg'])
};
AbstractMonitoringResultButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  buttonSize: 'xs'
};

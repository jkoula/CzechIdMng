import React from 'react';
import { useSelector } from 'react-redux';
import clsx from 'clsx';
//
import { i18n } from '../../../services/LocalizationService';
import { ConfigurationManager } from '../../../redux';

/**
 * Environment in navigation.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function NavigationEnvironment() {
  const environment = useSelector((state) => ConfigurationManager.getEnvironmentStage(state));
  //
  if (!environment) {
    return null;
  }
  //
  const environmentClassName = clsx(
    'label',
    { 'label-success': environment === 'development' },
    { 'label-warning': environment !== 'development' },
    { hidden: environment === 'production'}
  );
  return (
    <div className="navbar-text hidden-xs" title={ i18n(`environment.${ environment }.title`, { defaultValue: environment }) }>
      <span className={ environmentClassName }>
        <span className="hidden-sm">{ i18n(`environment.${ environment }.label`, { defaultValue: environment }) }</span>
        <span className="visible-sm-inline">{ i18n(`environment.${ environment }.short`, { defaultValue: environment }) }</span>
      </span>
    </div>
  );
}

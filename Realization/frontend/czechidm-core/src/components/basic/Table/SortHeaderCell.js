import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import SortIcon from './SortIcon';

const ASC = 'ASC';
const DESC = 'DESC';

function _activeSort(searchParameters, sortProperty, property) {
  //
  if (!searchParameters) {
    return null;
  }
  const sort = searchParameters.getSort(sortProperty || property);
  if (sort === null) {
    return null;
  }
  return sort === true ? ASC : DESC;
}

function _handleSort(props, order, event) {
  if (event) {
    event.preventDefault();
  }
  const { shiftKey } = event;
  const { sortProperty, property, sortHandler } = props;
  if (!sortHandler) {
    // if handleSort is not set, then its nothing to do
    return null;
  }
  return sortHandler(sortProperty || property, order, shiftKey);
}

/**
 * Header with sort action.
 *
 * @author Radek Tomiška
 */
export default function SortHeaderCell(props) {
  const { header, title, property, sortHandler, showLoading, searchParameters, sortProperty, className } = props;
  const active = _activeSort(searchParameters, sortProperty, property);
  const content = header || property;
  const classNames = classnames(
    'sort-header-cell',
    className
  );
  return (
    <div className={ classNames } title={ title }>
      <a
        href="#"
        onClick={ (event) => _handleSort(props, active === 'ASC' ? 'DESC' : 'ASC', event) }
        className={ !sortHandler ? 'disabled' : '' }>
        { content }
        {
          (sortHandler || active !== null)
          ?
          <SortIcon active={ active } showLoading={ showLoading }/>
          :
          null
        }
      </a>
    </div>
  );
}

SortHeaderCell.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Property for header and sorting
   */
  property: PropTypes.string,
  /**
   * Property for sorting - higher priority than property
   */
  sortProperty: PropTypes.string,
  /**
   * Column header text - if isn't set, then property is shown
   */
  header: PropTypes.string,
  /**
   * Current searchparameters - sort
   */
  searchParameters: PropTypes.object,
  /**
   * Callback action for data sorting

   * @param string property
   * @param string order [ASC, DESC]
   * @param bool shiftKey - append sort property, if shift is pressed.
   */
  sortHandler: PropTypes.func,
  /**
   * loadinig indicator
   */
  showLoading: PropTypes.bool
};

SortHeaderCell.defaultProps = {
  ...AbstractComponent.defaultProps,
  showLoading: false
};

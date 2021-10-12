import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

/**
 * Bootstrap column.
 *
 * TODO: clean up bootstrap styles, use material-ui grid.
 *
 * @author Radek Tomiška
 */
export default function Column(props) {
  const { rendered, children, className, style, lg, sm, md, xs } = props;
  if (!rendered) {
    return null;
  }
  //
  const classNames = classnames(
    { [`col-lg-${ lg }`]: (lg > 0) },
    { [`col-sm-${ sm }`]: (sm > 0) },
    { [`col-md-${ md }`]: (md > 0) },
    { [`col-xs-${ xs }`]: (xs > 0) },
    className
  );
  return (
    <div className={ classNames } style={ style }>
      { children }
    </div>
  );
}

Column.propTypes = {
  /**
   * If component is rendered on page
   */
  rendered: PropTypes.bool,
  /**
   * Column widths
   */
  lg: PropTypes.number,
  md: PropTypes.number,
  sm: PropTypes.number,
  xs: PropTypes.number
};

Column.defaultProps = {
  rendered: true,
  lg: undefined,
  md: undefined,
  sm: undefined,
  xs: undefined
};

import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

/**
 * Bootstrap row.
 *
 * TODO: clean up bootstrap styles, use material-ui grid.
 *
 * @author Radek Tomiška
 */
export default function Row(props) {
  const { rendered, children, className, style } = props;
  //
  if (!rendered) {
    return null;
  }
  //
  const classNames = classnames(
    'row',
    className
  );
  return (
    <div className={ classNames } style={ style }>
      { children }
    </div>
  );
}

Row.propTypes = {
  /**
   * If component is rendered on page
   */
  rendered: PropTypes.bool
};

Row.defaultProps = {
  rendered: true
};

import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';

/**
 * "<hr/>" in navigation.
 *
 * @author Radek Tomiška
 */
export default class NavigationSeparator extends Basic.AbstractContextComponent {

  render() {
    const { text, rendered } = this.props;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <li className="nav-separator divider" role="separator">
        { text }
      </li>
    );
  }
}

NavigationSeparator.propTypes = {
  rendered: PropTypes.bool,
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.arrayOf(PropTypes.oneOf([ PropTypes.node, PropTypes.object ]))
  ])
};

NavigationSeparator.defaultProps = {
  rendered: true,
  text: null
};

import React from 'react';
import PropTypes from 'prop-types';
//
import Tooltip from '@material-ui/core/Tooltip';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Overlay with tooltip.
 *
 * @author Radek Tomiška
 */
export default class BasicTooltip extends AbstractComponent {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      open: false
    };
  }

  /**
   * Shows tooltip
   */
  show() {
    this.setState({ open: true });
  }

  hide() {
    this.setState({ open: false });
  }

  render() {
    const { rendered, children, value, delayShow, placement } = this.props;
    const { open } = this.state;
    //
    if (!rendered || !children) {
      return null;
    }
    if (!value) {
      // return children;
    }
    //
    return (
      <Tooltip
        title={ value }
        arrow
        open={ open && value }
        onClose={ this.hide.bind(this) }
        onOpen={ this.show.bind(this) }
        enterDelay={ delayShow }
        placement={ placement }>
        { children }
      </Tooltip>
    );
  }
}

BasicTooltip.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * tooltip value / text
   */
  value: PropTypes.string,
  /**
   * Tooltip position
   */
  placement: PropTypes.oneOf(['top', 'bottom', 'right', 'left']),
  /**
   * A millisecond delay amount before showing the Tooltip once triggered.
   */
  delayShow: PropTypes.number
};

BasicTooltip.defaultProps = {
  ...AbstractComponent.defaultProps,
  placement: 'bottom',
  delayShow: 1000
};

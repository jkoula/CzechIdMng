import React from 'react';
import PropTypes from 'prop-types';
//
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
//
import * as Basic from '../../basic';

/**
 * Close button - return to link.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class CloseButton extends Basic.AbstractContextComponent {

  getComponentKey() {
    return 'component.advanced.CloseButton';
  }

  render() {
    const { rendered, showLoading, to } = this.props;
    //
    if (!rendered || !to) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="fa:close" showLoading/>
      );
    }
    //
    return (
      <IconButton
        style={{ color: '#ccc' }}
        title={ this.i18n('title') }
        onClick={ () => this.context.history.push(to) }>
        <CloseIcon/>
      </IconButton>
    );
  }
}

CloseButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  to: PropTypes.string.isRequired
};

CloseButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};

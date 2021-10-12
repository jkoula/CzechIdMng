import React from 'react';
import PropTypes from 'prop-types';
import * as Basic from '../../basic';

/**
 * Refresh button for table. Can be used even without advanced table (some css are hard coded to fit table toolbar - props can be added if needed).
 *
 * @author Radek Tomiška
 */
export default class RefreshButton extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, title, onClick, readOnly } = this.props;
    if (!rendered) {
      return null;
    }
    // default detail title
    const _title = title || this.i18n('button.refresh');
    //
    return (
      <Basic.Button
        title={ _title }
        onClick={ onClick }
        disabled={ readOnly }
        titlePlacement="bottom"
        showLoading={ showLoading }
        style={{ marginLeft: 5 }}
        icon="fa:refresh"/>
    );
  }
}

RefreshButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * onClick callback
   */
  onClick: PropTypes.func.isRequired,
  /**
   * Buttons tooltip, otherwise default 'button.refresh' will be used
   */
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.element])
};
RefreshButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};

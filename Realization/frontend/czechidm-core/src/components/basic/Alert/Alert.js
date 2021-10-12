import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import Alert from '@material-ui/lab/Alert';
import AlertTitle from '@material-ui/lab/AlertTitle';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Alert box.
 *
 * @author Radek Tomiška
 */
class BasicAlert extends AbstractComponent {

  constructor(props) {
    super(props);
    //
    this.state = {
      closed: false
    };
  }

  _onClose(event) {
    const { onClose } = this.props;
    this.setState({
      closed: true
    }, () => {
      if (onClose) {
        onClose(event);
      }
    });
  }

  render() {
    const {
      level,
      title,
      text,
      className,
      icon,
      onClose,
      rendered,
      showLoading,
      children,
      style,
      buttons,
      showHtmlText
    } = this.props;
    const { closed } = this.state;
    if (!rendered || closed || (!text && !title && !children)) {
      return null;
    }
    const classNames = classnames(
      'alert',
      'basic-alert',
      { 'text-center': showLoading },
      className
    );
    if (showLoading) {
      return (
        <div className={ classNames } style={ style }>
          <Icon type="fa" icon="refresh" showLoading/>
        </div>
      );
    }
    //
    return (
      <Alert
        severity={ Utils.Ui.toLevel(level) }
        className={ classNames }
        style={ style }
        onClose={ onClose ? this._onClose.bind(this) : null }
        icon={ icon ? <Icon icon={ icon }/> : null }>
        {
          !title
          ||
          <AlertTitle>{ title }</AlertTitle>
        }
        { showHtmlText ? <span dangerouslySetInnerHTML={{ __html: text}}/> : text }
        { children }
        {
          (!buttons || buttons.length === 0)
          ||
          <div className="buttons">
            { buttons }
          </div>
        }
      </Alert>
    );
  }
}

BasicAlert.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Alert level / css / class
   */
  level: PropTypes.oneOf(['success', 'warning', 'info', 'danger', 'error']),
  /**
   * Alert strong title content
   */
  title: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  /**
   * Alert text content
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  /**
   * glyphicon suffix name
   */
  icon: PropTypes.string,
  /**
   * Close function - if it's set, then close icon is shown and this method is called on icon click
   */
  onClose: PropTypes.func,
  /**
   * Alert action buttons
   */
  buttons: PropTypes.arrayOf(PropTypes.node),
  /**
   * Show text as html (dangerouslySetInnerHTML)
   */
  showHtmlText: PropTypes.bool
};

BasicAlert.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'info',
  onClose: null,
  buttons: [],
  showHtmlText: false
};

export default BasicAlert;

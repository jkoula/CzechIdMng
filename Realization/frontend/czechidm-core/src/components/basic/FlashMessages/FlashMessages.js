import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import NotificationSystem from 'react-notification-system';
//
import { ConfigurationManager } from '../../../redux';
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';

/**
 * Closable notification messages.
 *
 * @author Radek Tomiška
 */
export class FlashMessages extends AbstractContextComponent {

  UNSAFE_componentWillReceiveProps(nextProps) {
    const unprocessedMessages = this._getUnproccessedMessages(nextProps.messages, this.props.messages);
    unprocessedMessages.added.map(message => {
      this._showMessage(message);
    });
    unprocessedMessages.hidden.map(message => {
      this._hideMessage(message);
    });
  }

  /**
  * Returns newly {added, hidden} mesaages
  */
  _getUnproccessedMessages(newMessages, oldMessages) {
    let added = [];
    const hidden = [];
    if (!oldMessages || !oldMessages.length) {
      added = newMessages;
    } else {
      // show or hide new
      newMessages.map(message => {
        if (!message.hidden) {
          added[message.id] = message;
        } else {
          hidden[message.id] = message;
        }
      });
    }
    return {
      added,
      hidden
    };
  }

  _hideMessage(message) {
    if (message === null) {
      return;
    }
    this.refs.messages.removeNotification(message.id);
  }

  _onRemove(notification) {
    this.context.store.dispatch(this.getFlashManager().hideMessage(notification.uid));
  }

  _getAutoDismiss(message) {
    let autoDismiss = 10;
    if (message.level === 'error') {
      autoDismiss = 20;
    } else if (message.level === 'success') {
      autoDismiss = 5;
    }
    return autoDismiss;
  }

  /**
  * Adds message to UI
  */
  _showMessage(options) {
    if (!options) {
      return;
    }
    const message = this.getFlashManager().createMessage(options);
    //
    if (message.hidden) {
      // skip hidden messages
      return;
    }
    // show max 3 messages
    let messageCounter = 0;
    this.props.messages.map(m => {
      if (!m.hidden) {
        messageCounter += 1;
        if (messageCounter > this.props.maxShown) {
          this._hideMessage(m);
        }
      }
    });
    //
    this.refs.messages.addNotification({
      uid: message.id,
      title: message.title,
      message: message.message,
      level: message.level,
      position: message.position || 'tr',
      onRemove: (n) => this._onRemove(n),
      dismissible: message.dismissible,
      autoDismiss: this._getAutoDismiss(message),
      action: message.action,
      children: message.children
    });
  }

  _getNotificationSystemStyles() {
    const { theme } = this.props;
    //
    const styles = {
      Containers: {
        DefaultStyle: { // Applied to every notification, regardless of the notification level
          margin: '10px 15px 2px 0px'
        },
        tr: {
          top: '55px',
          bottom: 'auto',
          left: 'auto',
          right: '0px'
        },
        tc: {
          width: '600px',
          marginLeft: -300
        },
        bl: {
          bottom: '0px'
        }
      },
      NotificationItem: { // Override the notification item
        DefaultStyle: {
          borderRadius: `${ theme.shape.borderRadius }px`
        },
        success: { // Applied only to the success notification item
          color: theme.palette.success.contrastText,
          borderTop: `3px solid ${ theme.palette.success.main }`,
          backgroundColor: theme.palette.success.light,
        },
        warning: { // Applied only to the success notification item
          color: theme.palette.warning.contrastText,
          borderTop: `3px solid ${ theme.palette.warning.main }`,
          backgroundColor: theme.palette.warning.light,
        },
        error: { // Applied only to the success notification item
          color: theme.palette.error.contrastText,
          borderTop: `3px solid ${ theme.palette.error.main }`,
          backgroundColor: theme.palette.error.light,
        },
        info: { // Applied only to the success notification item
          color: theme.palette.info.contrastText,
          borderTop: `3px solid ${ theme.palette.info.main }`,
          backgroundColor: theme.palette.info.light,
        }
      },
      Title: {
        success: {
          color: theme.palette.success.contrastText
        },
        error: {
          color: theme.palette.error.contrastText
        },
        warning: {
          color: theme.palette.warning.contrastText
        },
        info: {
          color: theme.palette.info.contrastText
        }
      },
      Dismiss: {
        success: {
          color: theme.palette.success.contrastText,
          backgroundColor: theme.palette.success.main,
        },
        error: {
          color: theme.palette.error.contrastText,
          backgroundColor: theme.palette.error.main,
        },
        warning: {
          color: theme.palette.warning.contrastText,
          backgroundColor: theme.palette.warning.main,
        },
        info: {
          color: theme.palette.info.contrastText,
          backgroundColor: theme.palette.info.main,
        }
      },
      ActionWrapper: {
        DefaultStyle: {
          textAlign: 'right'
        }
      }
    };
    return styles;
  }

  render() {
    const styles = this._getNotificationSystemStyles();
    //
    return (
      <div id="flash-messages">
        <NotificationSystem ref="messages" style={ styles }/>
      </div>
    );
  }
}

FlashMessages.propTypes = {
  messages: PropTypes.array,
  maxShown: PropTypes.number
};
FlashMessages.defaultProps = {
  maxShown: 4
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    messages: state.messages.messages.toArray(),
    theme: ConfigurationManager.getApplicationTheme(state)
  };
}

// Wrap the component to inject dispatch and state into it
// this.refs.form.submit() - could call connected instance
export default connect(select)(FlashMessages);

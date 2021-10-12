import React from 'react';
import PropTypes from 'prop-types';
//
import DialogTitle from '@material-ui/core/DialogTitle';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import HelpIcon from '../HelpIcon/HelpIcon';
import Icon from '../Icon/Icon';

const useStyles = makeStyles((theme) => ({
  root: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    '& h2': {
      paddingTop: theme.spacing(0),
      paddingBottom: theme.spacing(0),
      marginTop: theme.spacing(0),
      marginBottom: theme.spacing(0),
    }
  }
}));

/**
 * Wrapped material-ui modal header.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function BasicModalHeader(props) {
  const { rendered, text, children, help, showLoading, icon, buttons, closeButton, onClose, onHide } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  const _onClose = () => {
    if (onHide) {
      onHide();
    }
    if (onClose) {
      onClose();
    }
  };
  //
  return (
    <DialogTitle
      id="scroll-dialog-header"
      onHide={ onHide }
      classes={ classes }>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <Icon type="fa" icon="refresh" showLoading rendered={ showLoading } />
        <div style={{ flex: 1 }}>
          {
            showLoading || text
            ?
            <>
              <Icon value={ icon } showLoading={ showLoading } style={{ marginRight: 5 }}/>
              {
                React.isValidElement(text)
                ?
                text
                :
                <span dangerouslySetInnerHTML={{ __html: text }}/>
              }
            </>
            :
            null
          }
          { children }
        </div>
        { buttons }
        <HelpIcon buttonSize="default" content={ help }/>
        {
          !closeButton || !onClose
          ||
          <IconButton>
            <CloseIcon onClick={ _onClose }/>
          </IconButton>
        }
      </div>
    </DialogTitle>
  );
}

BasicModalHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Header text.
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ])
};

BasicModalHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

BasicModalHeader.__ModalHeader__ = true;

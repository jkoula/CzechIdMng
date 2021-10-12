import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
//
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Button from '../Button/Button';
import Tooltip from '../Tooltip/Tooltip';
import HelpContent from '../../../domain/HelpContent';

/**
 * Help icon opens modal window with user documentation.
 *
 * @author Radek Tomiška
 */
export default class HelpIcon extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showModal: false
    };
  }

  close() {
    this.setState({ showModal: false });
  }

  open(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({ showModal: true });
  }

  getHeaderText(content) {
    const lines = content.split('\n');
    if (lines[0].lastIndexOf('# ', 0) === 0) { // startsWith - this work even in ie
      return lines[0].substr('# '.length);
    }
    return this.i18n('component.basic.HelpIcon.title');
  }

  getContentText(content) {
    const lines = content.split('\n');
    if (lines[0].lastIndexOf('# ', 0) === 0) { // startsWith - this work even in ie
      lines.shift();
      return lines.join('\n');
    }
    return content;
  }

  render() {
    const { content, rendered, showLoading, titlePlacement, buttonSize, tabIndex } = this.props;
    if (!rendered || !content) {
      return null;
    }
    let _header = null;
    let _body = null;
    //
    if (content instanceof HelpContent) {
      _header = content.getHeader();
      _body = content.getBody();
    } else if (_.isObject(content) && content.body) {
      _header = content.header;
      _body = content.body;
    } else {
      _body = (<span dangerouslySetInnerHTML={{ __html: content }}/>);
    }
    if (!_header) {
      _header = this.i18n('component.basic.HelpIcon.title');
    }

    return (
      <span className="help-icon-container">
        <Tooltip placement={ titlePlacement } id="button-tooltip" value={ this.i18n('component.basic.HelpIcon.title') }>
          <Button
            className="help-icon"
            onClick={ this.open.bind(this) }
            buttonSize={ buttonSize || 'xs' }
            color="inherit"
            level="success"
            icon="question-sign"
            showLoading={ showLoading }
            tabIndex={ tabIndex }/>
        </Tooltip>
        <Dialog
          open={ this.state.showModal }
          onClose={ this.close.bind(this) }
          maxWidth="lg"
          scroll="paper"
          aria-labelledby="scroll-dialog-title"
          aria-describedby="scroll-dialog-description" >

          <HelpDialogTitle text={ _header } onClose={ this.close.bind(this) } />

          <DialogContent dividers>
            <DialogContentText
              id="scroll-dialog-description"
              tabIndex={-1}>
              { _body }
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button level="link" onClick={ this.close.bind(this) }>
              { this.i18n('button.close') }
            </Button>
          </DialogActions>
        </Dialog>
      </span>
    );
  }
}

HelpIcon.propTypes = {
  ...AbstractContextComponent.propTypes,
  content: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object
  ]),
  /**
   * Help icon title position
   */
  titlePlacement: PropTypes.oneOf(['top', 'bottom', 'right', 'left'])
};

HelpIcon.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  titlePlacement: 'bottom'
};

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

function HelpDialogTitle(props) {
  const { text, onClose } = props;
  const classes = useStyles();
  //
  return (
    <DialogTitle
      id="scroll-dialog-title"
      classes={ classes }>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <div style={{ flex: 1 }}>
          { text }
        </div>
        <IconButton>
          <CloseIcon onClick={ onClose }/>
        </IconButton>
      </div>
    </DialogTitle>
  );
}

HelpDialogTitle.propTypes = {
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  onClose: PropTypes.func.isRequired
};

HelpDialogTitle.defaultProps = {
  text: null
};

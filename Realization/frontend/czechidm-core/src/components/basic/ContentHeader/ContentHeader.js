import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';

const useStyles = makeStyles((theme) => ({
  root: {
    margin: '0 0 10px 0',
    padding: '10px 0',
    borderBottomWidth: 1,
    borderBottomStyle: 'solid',
    borderBottomColor: theme.palette.secondary.main,
    '& h2': {
      margin: 0,
      padding: 0,
      lineHeight: '25px',
      fontSize: '20px'
    },
    '&.marginable': {
      marginLeft: -15,
      marginRight: -15,
      paddingLeft: 15,
      paddingRight: 15
    }
  }
}));

/**
 * Content header.
 *
 * @author Radek Tomiška
 */
export default function ContentHeader(props) {
  const { rendered, showLoading, children, className, help, text, icon, buttons, ...others } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  if (!text && !children) {
    return null;
  }

  const classNames = classnames(
    'content-header',
    classes.root,
    className
  );
  //
  return (
    <div className={ classNames } { ...others }>
      <h2>
        {
          showLoading
          ?
          <Icon type="fa" icon="refresh" showLoading className="icon-loading"/>
          :
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Icon value={ icon } style={{ marginRight: 7 }}/>
            <div style={{ flex: 1 }}>
              { text }
              { children }
            </div>
            <div style={{ fontSize: '0.7em' }}>
              { buttons }
            </div>
            <HelpIcon content={ help }/>
          </div>
        }
      </h2>
    </div>
  );
}

ContentHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Header text.
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * help content
   * @type {[type]}
   */
  help: PropTypes.string
};

ContentHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

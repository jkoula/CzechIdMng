import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

const useStyles = makeStyles((theme) => ({
  root: {
    margin: '0 0 10px 0',
    padding: '0 0 10px 0',
    borderBottomWidth: 1,
    borderBottomStyle: 'solid',
    borderBottomColor: theme.palette.primary.main,
    '& h1': {
      margin: 0,
      padding: 0,
      lineHeight: 1
    }
  }
}));

/**
 * Page header.
 *
 * @author Radek Tomiška
 */
export default function PageHeader(props) {
  const {
    rendered,
    showLoading,
    icon,
    children,
    className,
    text,
    ...others
  } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  const classNames = classnames(
    'page-header',
    classes.root,
    className
  );
  return (
    <div className={ classNames } { ...others }>
      <h1>
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
          </div>
        }
      </h1>
    </div>
  );
}

PageHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Table Header
   */
  text: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * Header left icon
   */
  icon: PropTypes.string
};

PageHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

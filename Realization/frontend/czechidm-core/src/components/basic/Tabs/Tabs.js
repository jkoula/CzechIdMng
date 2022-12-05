import React from 'react';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import AppBar from '@material-ui/core/AppBar';
//
import * as Utils from '../../../utils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

const useStyles = makeStyles((theme) => ({
  tabContent: {
    borderColor: theme.palette.divider,
    borderWidth: 1,
    borderStyle: 'solid',
    borderBottomLeftRadius: theme.shape.borderRadius,
    borderBottomRightRadius: theme.shape.borderRadius
  },
  activeTab: {
  }
}));

/**
 * Wrapped bootstrap Tabbs
 * - adds default styles
 * - adds rendered supported
 *
 * @author Radek Tomiška
 */
export default function BasicTabs(props) {
  const { rendered, activeKey, onSelect, className, style, unmountOnExit, children } = props;
  const [ _activeKey, setActiveKey ] = React.useState(activeKey);
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  const _mergedActiveKey = onSelect ? `${ activeKey }` : _activeKey;
  //
  const handleChange = (event, newValue) => {
    if (onSelect) {
      onSelect(newValue);
    } else {
      setActiveKey(newValue);
    }
  };
  // resolve valid Tab component as children
  const _children = children.filter(child => {
    return child.props.rendered && React.isValidElement(child);
  });
  if (_children.length === 0) { // no valid and rendered child found => nothing to render
    return null;
  }
  //
  // prepare rendered tabs
  let value = onSelect ? `${ activeKey }` : _mergedActiveKey;
  const _tabs = _children.map((child, index) => {
    let eventKey = child.props.value;
    if (!eventKey) {
      // if no value was specified, try to use eventKey
      eventKey = child.props.eventKey
    } else if (!eventKey && eventKey !== 0) {
      // if no value or eventKey was specified, use index instead
      eventKey = index;
    } else {
      eventKey = `${ eventKey }`;
    }
    if (Utils.Ui.isEmpty(value)) {
      value = eventKey;
    }
    //
    return (
      <Tab
        id={ `basic-tab-${ eventKey }` }
        value={ `${eventKey}` }
        aria-controls={ `basic-tabpanel-${ eventKey }` }
        disabled={ child.props.disabled }
        className={
          classnames(
            { [classes.activeTab]: value === `${eventKey}` },
            className
          )
        }
        style={ child.props.style }
        label={ child.props.title }/>
    );
  });
  //
  return (
    <div className="tab-horizontal" style={ style }>
      <AppBar position="static">
        <Tabs
          value={ value }
          onChange={ handleChange }
          aria-label="basic tabs"
          className={ className }>
          { _tabs }
        </Tabs>
      </AppBar>
      {
        _children.map((child, index) => {
          let eventKey = child.props.value;
          if (!eventKey) {
            // if no value was specified, try to use eventKey
            eventKey = child.props.eventKey
          } else if (!eventKey && eventKey !== 0) {
            // if no value or eventKey was specified, use index instead
            eventKey = index;
          } else {
            eventKey = `${ eventKey }`;
          }
          //
          return (
            <div
              role="tabpanel"
              hidden={ value !== `${eventKey}` }
              id={ `basic-tabpanel-${ eventKey }` }
              aria-labelledby={ `basic-tab-${ eventKey }` }
              className={ classes.tabContent }
              style={ child.props.style }>
              {
                value === `${eventKey}` || !unmountOnExit
                ?
                child
                :
                null
              }
            </div>
          );
        })
      }
    </div>
  );
}

BasicTabs.propTypes = {
  ...AbstractComponent.propTypes
};

BasicTabs.defaultProps = {
  ...AbstractComponent.defaultProps
};

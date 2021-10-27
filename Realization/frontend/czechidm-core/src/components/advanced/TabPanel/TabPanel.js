import React from 'react';
import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router';
import classnames from 'classnames';
import _ from 'lodash';
//
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
//
import TabPanelItem from './TabPanelItem';
import { getNavigationItems, resolveNavigationParameters } from '../../../redux/config/actions';
import { i18n } from '../../../services/LocalizationService';
import * as Basic from '../../basic';
import * as Utils from '../../../utils';

const ITEM_HEIGTH = 45; // item heigth for dynamic content resize

const useStyles = makeStyles((theme) => {
  return {
    root: {
      width: '100%'
    },
    activeItem: {
      backgroundColor: theme.palette.background.paper,
      '& a.basic-item-active': {
        borderBottomColor: theme.palette.background.paper
      }
    },
    sidebar: {
      position: 'absolute',
      width: 175,
      '& a.basic-item-active': {
        borderLeftColor: `${ theme.palette.secondary.main } !important`
      }
    },
    content: {
      backgroundColor: theme.palette.background.paper
    },
    sidebarContent: {
      position: 'inherit',
      margin: '0 0 0 175px',
      padding: '0 15px',
      borderLeftStyle: 'solid',
      borderLeftWidth: 1,
      borderLeftColor: '#bbb' // TODO: from theme
    },
    tabPane: {
      backgroundColor: 'transparent',
      borderLeft: '#bbb 1px solid', // TODO: from theme
      borderRight: '#bbb 1px solid', // TODO: from theme
      borderBottom: '#bbb 1px solid', // TODO: from theme
      borderBottomRightRadius: theme.shape.borderRadius,
      borderBottomLeftRadius: theme.shape.borderRadius
    }
  };
});

/**
 * Sidebar renders tabs by given navigation parent (parentId).
 *
 * @author Radek Tomiška
 */
export default function TabPanel(props) {
  const { position, parentId, match, children } = props;
  const userContext = useSelector((state) => state.security.userContext);
  const navigation = useSelector((state) => state.config.get('navigation'));
  const selectedNavigationItems = useSelector((state) => state.config.get('selectedNavigationItems'));
  const history = useHistory();
  const classes = useStyles();
  //
  let activeItemId = null;
  const navigationItems = [...getNavigationItems(navigation, parentId, null, userContext, match.params).map(item => {
    // resolve label
    const labelParams = resolveNavigationParameters(userContext, match.params);
    labelParams.defaultValue = item.label;
    let label = item.label;
    if (item.labelKey) {
      label = (<span>{ i18n(item.labelKey, labelParams) }</span>);
    } else if (item.titleKey) {
      // label from title
      label = (<span>{ i18n(item.titleKey, { defaultValue: item.title }) }</span>);
    }

    // Some path doesn't starts with slash ... we need to
    // ensure absolute path, so we need to add slash to the start.
    if (item.to) {
      item.to = `/${ Utils.Ui.trimSlash(item.to) }`;
    }
    const active = _.includes(selectedNavigationItems, item.id);
    if (active) {
      activeItemId = item.id;
    }
    //
    switch (item.type) {
      case 'TAB':
      case 'DYNAMIC': {
        if (position === 'top') {
          return (
            <Tab
              id={ `advanced-tab-${ item.id }` }
              label={ label }
              value={ item.id }
              disabled={ item.disabled }
              icon={
                item.icon
                ?
                <Basic.Icon icon={ item.icon } color={ item.iconColor }/>
                :
                null
              }
              title={ i18n(item.titleKey, { defaultValue: item.title }) }
              onClick={ () => history.push(item.to) }/>
          );
        }
        return (
          <TabPanelItem
            id={ `nav-item-${item.id}` }
            key={ `nav-item-${item.id}` }
            to={ item.to }
            icon={ item.icon }
            iconColor={ item.iconColor }
            title={ i18n(item.titleKey, { defaultValue: item.title }) }
            active={ active }
            className={ active ? classes.activeItem : null }>
            { label }
          </TabPanelItem>
        );
      }
      default: {
        return null;
      }
    }
  }).values()];
  //
  if (position === 'top') {
    return (
      <div className="tab-horizontal">
        <AppBar position="static">
          <Tabs
            value={ activeItemId }
            aria-label="advanced tabs">
            { navigationItems }
          </Tabs>
        </AppBar>
        <div className={ classnames(classes.content, 'tab-content') }>
          <div className={ classnames(classes.tabPane, 'tab-pane', 'active') }>
            { children }
          </div>
        </div>
      </div>
    );
  }
  //
  // left
  return (
    <Basic.Panel className="clearfix">
      <div className={ classnames(classes.root, 'tab-vertical', 'clearfix') }>
        <ul className={ classnames(classes.sidebar, 'nav', 'nav-pills', 'nav-stacked') }>
          { navigationItems }
        </ul>
        <div
          className={ classnames(classes.content, classes.sidebarContent, 'tab-content') }
          style={{ minHeight: navigationItems.length * ITEM_HEIGTH }}>
          { children }
        </div>
      </div>
    </Basic.Panel>
  );
}

TabPanel.propTypes = {
  match: PropTypes.object.isRequired,
  /**
   * which navigation parent wil be rendered - sub menus to render
   */
  parentId: PropTypes.string,
  /**
   * Tabs position
   *
   * @param  {[type]} ['left' [description]
   * @param  {[type]} 'top']  [description]
   * @return {[type]}         [description]
   */
  position: PropTypes.oneOf(['left', 'top'])
};
TabPanel.defaultProps = {
  position: 'left',
  parentId: null
};

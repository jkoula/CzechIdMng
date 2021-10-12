import React from 'react';
import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import classnames from 'classnames';
import _ from 'lodash';
//
import { makeStyles } from '@material-ui/core/styles';
//
import TabPanelItem from './TabPanelItem';
import { getNavigationItems, resolveNavigationParameters } from '../../../redux/config/actions';
import { i18n } from '../../../services/LocalizationService';
import * as Basic from '../../basic';
import * as Utils from '../../../utils';

const ITEM_HEIGTH = 45; // item heigth for dynamic content resize

const useStyles = makeStyles((theme) => {
  return {
    activeItem: {
      backgroundColor: theme.palette.background.paper,
      '& a.basic-item-active': {
        borderBottomColor: theme.palette.background.paper
      }
    },
    content: {
      backgroundColor: theme.palette.background.paper
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
  const classes = useStyles();
  //
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
    //
    switch (item.type) {
      case 'TAB':
      case 'DYNAMIC': {
        return (
          <TabPanelItem
            id={`nav-item-${item.id}`}
            key={`nav-item-${item.id}`}
            to={item.to}
            icon={item.icon}
            iconColor={item.iconColor}
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
        <ul className="nav nav-tabs">
          { navigationItems }
        </ul>
        <div className={ classnames(classes.content, 'tab-content') }>
          <div className="tab-pane active">
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
      <div className="tab-panel tab-vertical clearfix">
        <ul className="tab-panel-sidebar nav nav-pills nav-stacked">
          { navigationItems }
        </ul>
        <div
          className={ classnames(classes.content, 'tab-panel-content', 'tab-content') }
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

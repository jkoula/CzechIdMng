import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Immutable from 'immutable';
//
import * as Basic from '../../basic';
import ComponentService from '../../../services/ComponentService';
import ConfigLoader from '../../../utils/ConfigLoader';
import { SecurityManager } from '../../../redux';
import {
  getNavigationItems,
  resolveNavigationParameters,
  selectNavigationItems
} from '../../../redux/config/actions';
import NavigationItem from './NavigationItem';
import NavigationSeparator from './NavigationSeparator';
import NavigationMaterial from './NavigationMaterial';
//
const componentService = new ComponentService();

/**
 * Top navigation.
 *
 * TODO: move / split navigation item rendering into main / system / sidebar / identity menu
 *
 * @author Radek Tomiška
 */
export class Navigation extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      modals: new Immutable.Map({})
    };
  }

  renderNavigationItems(section = 'main', dynamicOnly = true, face = 'list') {
    const { navigation, userContext, selectedNavigationItems } = this.props;
    //
    const items = getNavigationItems(navigation, null, section, userContext, null, dynamicOnly);
    //
    return this._renderNavigationItems(items, userContext, selectedNavigationItems, face);
  }

  _renderNavigationItems(items, userContext, selectedNavigationItems, face = 'list') {
    if (!items) {
      return null;
    }
    const renderedItems = [];
    for (const item of items) {
      const renderedItem = this.renderNavigationItem(item, userContext, selectedNavigationItems[0], null, face);
      if (renderedItem) { // can be null
        renderedItems.push(renderedItem);
      }
    }
    return renderedItems;
  }

  _resolveNavigationItemText(item, userContext) {
    const labelParams = resolveNavigationParameters(userContext);
    labelParams.defaultValue = item.label;

    if (item.labelKey) {
      return (
        <span>{ this.i18n(item.labelKey, labelParams) }</span>
      );
    }
    if (item.label) {
      return (
        <span>{item.label}</span>
      );
    }
    return null;
  }

  renderNavigationItem(item, userContext, activeItem, titlePlacement = 'bottom', face = 'list') {
    switch (item.type) {
      case 'DYNAMIC': {
        const { modals } = this.state;
        //
        let ModalComponent = null;
        let onClick = null;
        if (item.modal) {
          // resolve modal component
          ModalComponent = componentService.getComponent(item.modal);
          onClick = (event) => {
            if (event) {
              event.preventDefault();
            }
            this.setState({
              modals: modals.set(item.modal, { show: true })
            });
          };
        }
        //
        return (
          <NavigationItem
            id={ `nav-item-${item.id}` }
            key={ `nav-item-${item.id}` }
            to={ item.to }
            title={ this.i18n(item.titleKey, { defaultValue: item.title }) }
            titlePlacement={ titlePlacement }
            icon={ item.icon }
            iconColor={ item.iconColor }
            active={ activeItem === item.id }
            text={ this._resolveNavigationItemText(item, userContext) }
            onClick={ onClick }
            face={ face }>
            {
              !ModalComponent
              ||
              <ModalComponent
                show={ modals.has(item.modal) ? modals.get(item.modal).show : false }
                onHide={ () => { this.setState({ modals: modals.set(item.modal, { show: false }) }); } }/>
            }
          </NavigationItem>
        );
      }
      case 'TAB': {
        // tab is not visible in menu
        return null;
      }
      case 'SEPARATOR': {
        return (
          <NavigationSeparator
            id={ `nav-item-${item.id}` }
            key={ `nav-item-${item.id}` }
            text={ this._resolveNavigationItemText(item, userContext) } />
        );
      }
      default: {
        this.getLogger().warn(`[Advanced.Navigation] - [${ item.type }] type not implemeted for item id [${ item.id }]`);
        return null;
      }
    }
  }

  toogleNavigationItem(item, level, isActive, redirect = true, event) {
    if (event) {
      event.preventDefault();
    }
    if (!redirect) {
      // prevent to redirect on click on arrow - toogle navigation only
      event.stopPropagation();
    } else if (item.to) {
      this.context.history.push(item.to);
    }
    const { selectedNavigationItems } = this.props;
    const newNavigationState = level > 0 ? selectedNavigationItems.slice(0, level - 1) : [];
    if (!isActive) {
      // show another level
      newNavigationState.push(item.id);
    }
    //
    this.context.store.dispatch(selectNavigationItems(newNavigationState));
    // prevent default link
    return false;
  }

  renderSidebarItems(parentId = null, level = 0) {
    const { navigation, navigationCollapsed, userContext, selectedNavigationItems } = this.props;
    level += 1;
    const levelItems = getNavigationItems(navigation, parentId, 'main', userContext, null, true);
    if (!levelItems || levelItems.length === 0) {
      return null;
    }

    const items = [];
    for (const levelItem of levelItems) {
      if (levelItem.type !== 'DYNAMIC' && levelItem.type !== 'SEPARATOR') {
        continue;
      }
      const childrenItems = getNavigationItems(navigation, levelItem.id, 'main', userContext, null, true);
      //
      if (childrenItems.length === 1 && childrenItems[0].path === levelItem.path) {
        // if menu contains only one subitem, which leeds to the same path - sub menu is truncated
        const item = this.renderNavigationItem(
          levelItem,
          userContext,
          selectedNavigationItems.length >= level ? selectedNavigationItems[level - 1] : null,
          'right'
        );
        if (item) {
          items.push(item);
        }
      } else {
        const children = this.renderSidebarItems(levelItem.id, level);
        let isActive = selectedNavigationItems.length >= level && selectedNavigationItems[level - 1] === levelItem.id;
        const isExpanded = isActive;
        // last active child exists => not active
        if (isActive && childrenItems && selectedNavigationItems.length > level) {
          const nextSelectedItemId = selectedNavigationItems[level];
          const child = childrenItems.find(c => c.id === nextSelectedItemId);
          if (child) {
            isActive = false;
          }
        }
        let parentRedirect = false; // show content, when menu is expanded
        if (navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).has(levelItem.id)) {
          const childAlias = navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).get(levelItem.id).toArray()
            .find(c => c.path === levelItem.path); // childer by the same path
          //
          if (childAlias && childAlias.type === 'MAIN-MENU') {
            parentRedirect = !childAlias.access || SecurityManager.hasAccess(childAlias.access);
          }
        }
        //
        if (children && !navigationCollapsed) {
          items.push(
            <li
              key={ `nav-item-${ levelItem.id }` }
              className={ isExpanded ? 'has-children expanded' : 'has-children'}>
              <Basic.Tooltip
                id={ `${ levelItem.id }-tooltip` }
                placement="right"
                value={ this.i18n(levelItem.titleKey, { defaultValue: levelItem.title }) }>
                <Link
                  to={ levelItem.to || '#' }
                  onClick={
                    this.toogleNavigationItem.bind(
                      this,
                      levelItem,
                      level,
                      isActive,
                      parentRedirect
                    )
                  }
                  className={ isActive && parentRedirect ? 'active' : '' }>
                  <Basic.Icon icon={ levelItem.icon } color={ levelItem.iconColor }/>
                  {
                    navigationCollapsed
                    ?
                    null
                    :
                    <span>
                      { this._resolveNavigationItemText(levelItem, userContext) }
                      <Basic.Icon
                        onClick={ this.toogleNavigationItem.bind(this, levelItem, level, isActive, false) }
                        value={ `fa:angle-${ isActive ? 'down' : 'left' }` }
                        className="arrow-icon" />
                    </span>
                  }
                </Link>
              </Basic.Tooltip>
              { children }
            </li>
          );
        } else {
          const item = this.renderNavigationItem(
            levelItem,
            userContext,
            selectedNavigationItems.length >= level ? selectedNavigationItems[level - 1] : null,
            'right'
          );
          if (item) {
            items.push(item);
          }
        }
      }
    }

    if (items.length === 0) {
      return null;
    }

    const classNames = classnames(
      'nav',
      { 'nav-second-level': level === 2 },
      { 'nav-third-level': level === 3 },
      { hidden: (level > 3 || (navigationCollapsed && level > 1)) }, // only three levels are supported
      { in: (selectedNavigationItems.length > level - 1 && selectedNavigationItems[level - 2]) === parentId },
      { collapse: parentId
        && !this._isSelected(selectedNavigationItems, parentId)
        && (selectedNavigationItems.length > level - 1
        && selectedNavigationItems[level - 2]) !== parentId }
    );
    return (
      <ul
        id={ level === 1 ? 'side-menu' : `side-menu-${ level }` }
        className={ classNames }>
        { items }
      </ul>
    );
  }

  _isSelected(selectedNavigationItems, parentId) {
    if (!parentId || !selectedNavigationItems) {
      return false;
    }
    for (const selectedNavigationItem of selectedNavigationItems) {
      if (parentId === selectedNavigationItem) {
        return true;
      }
    }
    return false;
  }

  render() {
    const {
      userContext,
      rendered,
      location
    } = this.props;
    //
    if (!rendered) {
      return false;
    }
    //
    const systemItems = this.renderNavigationItems('system', null, 'button');
    const sidebarItems = this.renderSidebarItems();
    //
    return (
      <NavigationMaterial
        userContext={ userContext }
        systemItems={ systemItems }
        sidebarItems={ sidebarItems }
        location={ location }>
        { this.props.children }
      </NavigationMaterial>
    );
  }
}

Navigation.propTypes = {
  rendered: PropTypes.bool,
  navigation: PropTypes.object,
  navigationCollapsed: PropTypes.bool,
  selectedNavigationItems: PropTypes.array,
  environment: PropTypes.string,
  userContext: PropTypes.object,
  i18nReady: PropTypes.string
};

Navigation.defaultProps = {
  rendered: true,
  navigation: null,
  navigationCollapsed: false,
  selectedNavigationItems: null,
  environment: null,
  userContext: null,
  i18nReady: null
};

function select(state) {
  return {
    navigation: state.config.get('navigation'),
    navigationCollapsed: state.security.userContext.navigationCollapsed,
    selectedNavigationItems: state.config.get('selectedNavigationItems'),
    userContext: state.security.userContext,
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(Navigation);

import React from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router';
//
import Hidden from '@material-ui/core/Hidden';
import Button from '@material-ui/core/Button';
import Person from '@material-ui/icons/Person';
import ExpandMore from '@material-ui/icons/ExpandMore';
import Menu from '@material-ui/core/Menu';
import MenuList from '@material-ui/core/MenuList';
import Divider from '@material-ui/core/Divider';
//
import * as Basic from '../../basic';
import { i18n } from '../../../services/LocalizationService';
import ComponentService from '../../../services/ComponentService';
import {
  getNavigationItems,
} from '../../../redux/config/actions';
import {
  IdentityManager,
  SecurityManager,
  FlashMessagesManager,
  DataManager
} from '../../../redux';
//
const identityManager = new IdentityManager();
const flashMessagesManager = new FlashMessagesManager();
const securityManager = new SecurityManager();
const componentService = new ComponentService();
const dataManager = new DataManager();

/**
 * Identity menu in navigation.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
function NavigationIdentity(props) {
  const { userContext } = props;
  const [ identityMenuShowLoading, setIdentityMenuShowLoading ] = React.useState(false);
  const [ anchorEl, setAnchorEl ] = React.useState(null);
  const dispatch = useDispatch();
  const history = useHistory();
  const profileUiKey = identityManager.resolveProfileUiKey(userContext.username);
  const profile = useSelector((state) => DataManager.getData(state, profileUiKey));
  const _imageUrl = profile ? profile.imageUrl : null;
  const identity = useSelector((state) => identityManager.getEntity(state, userContext.username));
  const isSwitchedUser = userContext.originalUsername && userContext.originalUsername !== userContext.username;
  const navigation = useSelector((state) => state.config.get('navigation'));
  const items = getNavigationItems(navigation, null, 'identity-menu', userContext, null, false);
  const modals = useSelector((state) => DataManager.getModals(state));
  //
  if (userContext.isExpired || !SecurityManager.isAuthenticated(userContext)) {
    return null;
  }
  const handleSwitchUserLogout = (event) => {
    if (event) {
      event.preventDefault();
    }
    const username = userContext.originalUsername;
    //
    dispatch(securityManager.switchUserLogout((result) => {
      if (result) {
        dispatch(flashMessagesManager.addMessage({
          level: 'success',
          key: 'core-switch-user-success',
          message: i18n('content.identity.switch-user.message.success', { username })
        }));
        history.replace(`/`);
      }
      localStorage.removeItem("switchUser");
    }));
  };
  //
  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };
  //
  const handleClose = () => {
    setAnchorEl(null);
  };
  //
  return (
    <div>
      <Button
        color={ !isSwitchedUser ? 'inherit' : 'secondary' }
        variant={ isSwitchedUser ? 'contained' : '' }
        size={ isSwitchedUser ? 'small' : 'default'}
        aria-controls="identity-menu"
        aria-haspopup="true"
        role="button"
        startIcon={ isSwitchedUser ? <Basic.Icon value="component:switch-user"/> : <Person /> }
        endIcon={ <ExpandMore fontSize="small" /> }
        title={
          !isSwitchedUser
          ||
          i18n('content.identity.switch-user.switched.title', {
            originalUsername: userContext.originalUsername,
            username: userContext.username
          })
        }
        onClick={ (event) => {
          // load identity ... and icon
          handleClick(event);
          setIdentityMenuShowLoading(true);
          dispatch(identityManager.downloadProfileImage(userContext.username));
          dispatch(identityManager.fetchEntityIfNeeded(userContext.username, null, () => {
            setIdentityMenuShowLoading(false);
          }));
        }}>
        <Hidden smDown>
          <Basic.ShortText
            value={ userContext.username }
            cutChar=""
            maxLength={ 30 }
            title={
              !isSwitchedUser
              ||
              i18n('content.identity.switch-user.switched.title', {
                originalUsername: userContext.originalUsername,
                username: userContext.username
              })
            }/>
        </Hidden>
      </Button>
      <Menu
        id="identity-menu"
        anchorEl={ anchorEl }
        keepMounted
        open={ Boolean(anchorEl) }
        onClose={ handleClose }>
        <div className="identity-menu">
          {
            identityMenuShowLoading
            ?
            <Basic.Loading isStatic show/>
            :
            <div>
              <div className="identity-image">
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <Basic.Avatar src={ _imageUrl } alt="profile">
                    <Basic.Icon
                      value="component:identity"
                      identity={ identity }
                      style={{ width: 'auto', marginRight: 0 }}/>
                  </Basic.Avatar>
                  <div style={{ flex: 1, paddingLeft: 7 }}>
                    <div>
                      <Basic.ShortText value={ userContext.username } cutChar="" maxLength="40" style={{ fontSize: '1.1em', fontWeight: 'normal' }}/>
                    </div>
                    <Basic.ShortText value={ identityManager.getFullName(identity) } cutChar="" maxLength="40"/>
                  </div>
                </div>
                <Basic.Div rendered={ isSwitchedUser } style={{ marginTop: 5 }}>
                  <Basic.Button
                    level="success"
                    buttonSize="xs"
                    onClick={ handleSwitchUserLogout }
                    showLoading={ userContext.showLoading }>
                    { i18n('content.identity.switch-user.button.logout') }
                    <span style={{ marginLeft: 5 }}>
                      (
                      <Basic.ShortText
                        value={ userContext.originalUsername }
                        cutChar=""
                        maxLength={ 30 }
                        style={{ fontSize: '1.1em', fontWeight: 'bold' }}/>
                      )
                    </span>
                  </Basic.Button>
                </Basic.Div>
              </div>
              <MenuList>
                <Divider />
                {
                  items.map((item, index) => {
                    let ModalComponent = null;
                    if (item.modal) {
                      // resolve modal component
                      ModalComponent = componentService.getComponent(item.modal);
                    }
                    if (item.type === 'SEPARATOR') {
                      return (
                        <Divider />
                      );
                    }
                    if (item.type === 'DYNAMIC') {
                      return (
                        <div>
                          <Basic.MenuItem
                            eventKey={ index }
                            icon={ item.icon }
                            onClick={ (event) => {
                              event.preventDefault();
                              //
                              if (item.modal) {
                                dispatch(dataManager.setModals(modals.set(item.modal, { show: true })));
                              } else if (item.to) {
                                history.push(item.to);
                              } else if (item.onClick) {
                                item.onClick();
                              }
                              handleClose();
                            }}>
                            { i18n(item.labelKey || item.label) }
                          </Basic.MenuItem>
                          {
                            !ModalComponent
                            ||
                            <ModalComponent
                              show={ modals.has(item.modal) ? modals.get(item.modal).show : false }
                              onHide={ () => { dispatch(dataManager.setModals(modals.set(item.modal, { show: false }))); } }/>
                          }
                        </div>
                      );
                    }
                    //
                    return null;
                  })
                }
              </MenuList>
            </div>
          }
        </div>
      </Menu>
    </div>
  );
}

NavigationIdentity.propTypes = {
  userContext: PropTypes.object.isRequired
};

export default NavigationIdentity;

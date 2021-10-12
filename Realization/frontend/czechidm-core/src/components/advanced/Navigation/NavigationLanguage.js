import React from 'react';
import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';
//
import Translate from '@material-ui/icons/Translate';
import MenuItem from '@material-ui/core/MenuItem';
import Menu from '@material-ui/core/Menu';
import Button from '@material-ui/core/Button';
import ExpandMore from '@material-ui/icons/ExpandMore';
//
import { SecurityManager, IdentityManager } from '../../../redux';
import { LocalizationService } from '../../../services';
import { i18nChange } from '../../../redux/config/actions';
//
const identityManager = new IdentityManager();

/**
 * Navigation - language selector.
 *
 * @author Radek Tomiška
 * @since 11.3.0
 */
function NavigationLanguage(props) {
  const { userContext } = props;
  const [ anchorEl, setAnchorEl ] = React.useState(null);
  const supportedLanguages = LocalizationService.getSupportedLanguages();
  const dispatch = useDispatch();
  //
  // one language is supported only => cahne is not needed
  if (!supportedLanguages || supportedLanguages.length === 0) {
    return null;
  }
  //
  const handleOpenMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleCloseMenu = () => {
    setAnchorEl(null);
  };

  const handleLanguageChange = (event, lng) => {
    if (event) {
      event.preventDefault();
    }
    setAnchorEl(null);
    //
    dispatch(i18nChange(lng, () => {
      // RT: reload is not needed anymore, most of component was refectored to listen redux state.
      // RT: filled form values are not rerendered (e.g. filled filters), when locale is changed, but i think is trivial issue
      // window.location.reload();
      //
      if (SecurityManager.isAuthenticated(userContext)) {
        dispatch(identityManager.saveCurrentProfile(userContext.id, {
          preferredLanguage: lng
        }));
      }
    }));
  };
  //
  return (
    <div>
      <Button
        aria-label="Change language"
        aria-controls="language-menu"
        aria-haspopup="true"
        onClick={ handleOpenMenu }
        endIcon={ <ExpandMore fontSize="small" /> }
        color="inherit">
        { LocalizationService.getCurrentLanguage() }
      </Button>
      <Menu
        id="language-menu"
        anchorEl={ anchorEl }
        keepMounted
        open={ Boolean(anchorEl) }
        onClose={ handleCloseMenu }>
        {
          supportedLanguages.map((lng) => (
            <MenuItem
              key={ `locale-${ lng }` }
              onClick={ (event) => handleLanguageChange(event, lng) }>
              { lng }
            </MenuItem>
          ))
        }
      </Menu>
    </div>
  );
}

NavigationLanguage.propTypes = {
  userContext: PropTypes.object.isRequired
};

NavigationLanguage.defaultProps = {
};

export default NavigationLanguage;

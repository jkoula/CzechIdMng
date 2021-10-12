import React from 'react';
import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';
import InputBase from '@material-ui/core/InputBase';
import { alpha, makeStyles } from '@material-ui/core/styles';
import { i18n } from '../../../services/LocalizationService';
//
import {
  IdentityManager,
  RoleManager,
  SecurityManager,
  FlashMessagesManager
} from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';
//
const identityManager = new IdentityManager();
const roleManager = new RoleManager();
const flashMessagesManager = new FlashMessagesManager();

/**
 * Search box in navigation.
 * @FIXME: Search single identity and role is supported now only (implement service registration).
 * @FIXME: both READ authorities are required now to show search box - search by permission.
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
const useStyles = makeStyles((theme) => ({
  search: {
    position: 'relative',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: alpha(theme.palette.common.white, 0.15),
    '&:hover': {
      backgroundColor: alpha(theme.palette.common.white, 0.25),
    },
    marginLeft: 0,
    width: '100%',
    [theme.breakpoints.up('sm')]: {
      marginLeft: theme.spacing(1),
      width: 'auto'
    },
    [theme.breakpoints.down('xs')]: {
      display: 'none'
    }
  },
  searchIcon: {
    padding: theme.spacing(0, 2),
    height: '100%',
    position: 'absolute',
    // pointerEvents: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  inputRoot: {
    color: 'inherit'
  },
  inputInput: {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('sm')]: {
      width: '24ch',
      '&:focus': {
        width: '34ch'
      }
    }
  }
}));

function NavigationSearch(props) {
  const { userContext } = props;
  const [ showLoading, setShowLoading ] = React.useState(false);
  const [ text, setText ] = React.useState('');
  const dispatch = useDispatch();
  const history = useHistory();
  const classes = useStyles();
  //
  if (!SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'ROLE_READ'], userContext)) {
    return null;
  }
  //
  const search = (event) => {
    if (event) {
      event.preventDefault();
    }
    if (!text) {
      return;
    }
    //
    setShowLoading(true);

    let counter = 0;
    dispatch(identityManager.fetchEntity(text, 'search', (identity, e1) => {
      if (e1 && (e1.statusCode === 404 || e1.statusCode === 403)) {
        dispatch(roleManager.fetchEntity(text, 'search2', (role, e2) => {
          if (e2 && (e2.statusCode === 404 || e2.statusCode === 403)) {
            // text search is used => when at least record is found, then first detial is shown. Warning message is shown otherwise.
            identityManager.getService()
              .search(new SearchParameters().setFilter('text', text).setSort('disabled', true).setSort('username', true))
              .then(json => {
                // ok
                const entities = json._embedded[identityManager.getCollectionType()] || [];
                if (entities.length === 1) {
                  history.push(identityManager.getDetailLink(entities[0]));
                  setShowLoading(false);
                } else {
                  counter += entities.length;
                  roleManager.getService()
                    .search(new SearchParameters().setFilter('text', text).setSort('disabled', true).setSort('code', true))
                    .then(json2 => {
                      // ok
                      const entities2 = json2._embedded[roleManager.getCollectionType()] || [];
                      if (entities2.length === 1) {
                        history.push(`/role/${ encodeURIComponent(entities2[0].id) }/detail`);
                        setText('');
                      } else if (entities2.length > 1) {
                        counter += entities2.length;
                        // find more
                        dispatch(flashMessagesManager.addMessage({
                          key: 'search-result',
                          level: 'info',
                          title: i18n('component.advanced.NavigationSearch.message.foundMore.title'),
                          message: i18n('component.advanced.NavigationSearch.message.foundMore.message', { counter, text })
                        }));
                      } else {
                        // not found message
                        dispatch(flashMessagesManager.addMessage({
                          key: 'search-result',
                          level: 'info',
                          title: i18n('component.advanced.NavigationSearch.message.notFound.title'),
                          message: i18n('component.advanced.NavigationSearch.message.notFound.message', { text })
                        }));
                      }
                      setShowLoading(false);
                    })
                    .catch(error => {
                      dispatch(flashMessagesManager.addError(error));
                      setShowLoading(false);
                    });
                }
              })
              .catch(error => {
                dispatch(flashMessagesManager.addError(error));
                setShowLoading(false);
              });
          } else if (e2) {
            dispatch(flashMessagesManager.addError(e2));
          } else {
            history.push(`/role/${ encodeURIComponent(role.id) }/detail`);
            setText('');
          }
          setShowLoading(false);
        }));
      } else if (e1) {
        dispatch(flashMessagesManager.addError(e1));
        setShowLoading(false);
      } else {
        setText('');
        setShowLoading(false);
        //
        history.push(identityManager.getDetailLink(identity));
      }
    }));
  };
  //
  return (
    <form className={ classes.search } onSubmit={ search }>
      <div className={ classes.searchIcon }>
        {
          showLoading
          ?
          <RefreshIcon />
          :
          <SearchIcon />
        }
      </div>
      <InputBase
        placeholder={ `${ i18n('component.advanced.NavigationSearch.search.placeholder') } ...` }
        classes={{
          root: classes.inputRoot,
          input: classes.inputInput,
        }}
        value={ text }
        inputProps={{ 'aria-label': 'search' }}
        onChange={ e => setText(e.target.value) }
        disabled={ showLoading }
      />
    </form>
  );
}

NavigationSearch.propTypes = {
  userContext: PropTypes.object.isRequired
};

export default NavigationSearch;

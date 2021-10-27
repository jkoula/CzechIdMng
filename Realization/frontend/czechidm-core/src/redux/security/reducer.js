import _ from 'lodash';
import {
  REQUEST_LOGIN,
  RECEIVE_LOGIN,
  RECEIVE_LOGIN_EXPIRED,
  RECEIVE_LOGIN_ERROR,
  RECEIVE_PROFILE, // @since 9.3.0
  LOGOUT,
  RECEIVE_REMOTE_LOGIN_ERROR,
  REQUEST_REMOTE_LOGIN
} from './SecurityManager';
import { Actions } from '../config/constants';

function INITIAL_THEME(themeType) {
  return {
    palette: {
      type: themeType,
      primary: {
        main: themeType === 'dark' ? '#90caf9' : '#1976d2', // #5cb85c
        contrastText: '#ffffff'
      },
      secondary: {
        light: '#ff4081',
        main: '#f50057',
        dark: '#c51162',
        contrastText: '#fff'
      },
      success: {
        main: '#4caf50',
        contrastText: '#ffffff'
      },
      warning: {
        main: '#ff9800',
        contrastText: '#ffffff'
      },
      action: {
        loading: themeType === 'dark' ? 'rgba(0, 0, 0, 0.26)' : 'rgba(255, 255, 255, 0.7)'
      },
      background: {
        paper: themeType === 'dark' ? '#424242' : '#fff',
        default: themeType === 'dark' ? '#303030' : '#fafafa'
      }
    },
    shape: {
      borderRadius: 4
    },
    overrides: {
      MuiCssBaseline: {
        '@global': {
          a: {
            color: '#f50057', // ~ secondary
            textDecoration: 'none',
            '&:visited': {
              color: '#f50057',
              textDecoration: 'none',
            },
            '&:focus': {
              color: '#c51162', // ~ secondary dark
              textDecoration: 'underline',
            },
            '&:hover': {
              color: '#c51162', // ~ secondary dark
              textDecoration: 'underline',
            }
          }
        }
      }
    }
  };
}

// TODO: integrate immutable map with redux-localstorage
const INITIAL_STATE = {
  userContext: { // logged userContext {id, username, token, isGuest etc .}
    id: null, // logged identity id
    showLoading: false,
    isExpired: false,
    username: null, // logged identity username
    isAuthenticated: false,
    isTryRemoteLogin: true,
    loginTargetPath: null, // target path before login redirection
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [], // identity authorities
    profile: null, // identity profile @since 9.3.0
    navigationCollapsed: false,
    originalUsername: null, // before switch
    twoFactorToken: null,
    theme: INITIAL_THEME('light')
  }
};

/**
 * Security context storage
 *
 * @author Radek Tomiška
 */
export default function reduce(state = INITIAL_STATE, action) {
  switch (action.type) {
    case REQUEST_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: true
        })
      });
    }
    case REQUEST_REMOTE_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: true,
          isTryRemoteLogin: false
        })
      });
    }
    case RECEIVE_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge(
          {
            isAuthenticated: true,
            showLoading: false,
            isExpired: false
          },
          action.userContext
        )
      });
    }
    case RECEIVE_LOGIN_ERROR: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: false,
          tokenCIDMST: null,
          isAuthenticated: false
        })
      });
    }
    case RECEIVE_REMOTE_LOGIN_ERROR: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: false,
          isTryRemoteLogin: false
        })
      });
    }
    case RECEIVE_LOGIN_EXPIRED: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          isAuthenticated: false,
          isExpired: true,
          showLoading: false,
          isTryRemoteLogin: true,
          tokenCIDMST: null,
          twoFactorToken: action.twoFactorToken === undefined ? null : action.twoFactorToken
        })
      });
    }
    case RECEIVE_PROFILE: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          profile: action.profile
        })
      });
    }
    case Actions.COLLAPSE_NAVIGATION: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          navigationCollapsed: action.collapsed
        })
      });
    }
    case 'THEME': {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          theme: INITIAL_THEME(action.theme)
        })
      });
    }
    case LOGOUT: {
      return _.merge({}, INITIAL_STATE, {
        userContext: _.merge({}, INITIAL_STATE.userContext, {
          isTryRemoteLogin: action.isTryRemoteLogin === null || action.isTryRemoteLogin === undefined ? true : action.isTryRemoteLogin
        })
      });
    }
    default:
      return state;
  }
}

/**
 * Simple function wrapper to assing name to default export
 */
export function security(state, action) {
  return reduce(state, action);
}

import React from 'react';
import PropTypes from 'prop-types';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import clsx from 'clsx';
//
import AppBar from '@material-ui/core/AppBar';
import Divider from '@material-ui/core/Divider';
import Drawer from '@material-ui/core/Drawer';
import Hidden from '@material-ui/core/Hidden';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import Brightness4Icon from '@material-ui/icons/Brightness4';
import Brightness7Icon from '@material-ui/icons/Brightness7';
import Toolbar from '@material-ui/core/Toolbar';
import { makeStyles } from '@material-ui/core/styles';
//
import * as Utils from '../../../utils';
import { IdentityManager, SecurityManager } from '../../../redux';
import { collapseNavigation } from '../../../redux/config/actions';
import NavigationSearch from './NavigationSearch';
import NavigationMonitoring from './NavigationMonitoring';
import NavigationLanguage from './NavigationLanguage';
import NavigationIdentity from './NavigationIdentity';
import NavigationEnvironment from './NavigationEnvironment';
//
const identityManager = new IdentityManager();
const drawerWidth = 240;

/**
 * Nagigation - top + sidebar.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => ({
  grow: {
    flexGrow: 1
  },
  root: {
    display: 'flex'
  },
  appBarRoot: {
    backgroundColor: theme.palette.type === 'dark' ? '#333' : theme.palette.primary.main,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    [theme.breakpoints.up('md')]: {
      width: `calc(100% - ${ drawerWidth }px)`,
      marginLeft: drawerWidth
    },
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen
    })
  },
  appBarClose: {
    zIndex: theme.zIndex.drawer + 1,
    [theme.breakpoints.up('md')]: {
      width: `calc(100% - ${ theme.spacing(7) + 1 }px)`,
      marginLeft: theme.spacing(7) + 1
    },
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen
    })
  },
  appBarShift: {
    [theme.breakpoints.up('md')]: {
      width: `calc(100% - ${ drawerWidth }px)`,
      marginLeft: drawerWidth
    },
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen
    })
  },
  menuButton: {
    marginRight: theme.spacing(2),
    [theme.breakpoints.up('md')]: {
      display: 'none'
    }
  },
  // necessary for content to be below app bar
  toolbar: theme.mixins.toolbar,
  drawer: {
  },
  drawerPaper: {
    width: drawerWidth
  },
  drawerOpen: {
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen
    })
  },
  drawerClose: {
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen
    }),
    overflowX: 'hidden',
    width: theme.spacing(7) + 1,
    '& .home.collapsed': {
      display: 'none'
    }
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(3),
    marginLeft: -35, // FIXME: where is gutter?
    [theme.breakpoints.up('sm')]: {
      marginLeft: -50 // FIXME: where is gutter?
    }
  },
  sidebar: {
    backgroundColor: theme.palette.type === 'dark' ? '#333' : 'transparent',
    '& .nav li': {
      '&:hover': {
        color: theme.palette.text.primary,
        backgroundColor: theme.palette.action.hover,
      },
      '& a': {
        color: theme.palette.text.secondary,
        '&:hover': {
          color: theme.palette.text.primary,
        },
        '&:focus': {
          color: theme.palette.text.primary,
        },
        '&:visited': {
          color: theme.palette.text.secondary,
        },
        '&.active': {
          color: theme.palette.text.primary,
          backgroundColor: theme.palette.action.selected,
          borderLeftColor: theme.palette.primary.main
        }
      }
    }
  },
  nested: {
    paddingLeft: theme.spacing(4)
  },
  hide: {
    display: 'none',
  }
}));

function NavigationMaterial(props) {
  const {
    userContext,
    systemItems,
    sidebarItems,
    location,
    children
  } = props;
  const classes = useStyles();
  const [ mobileOpen, setMobileOpen ] = React.useState(false);
  const navigationCollapsed = userContext.navigationCollapsed;
  const themeType = useSelector((state) => Utils.Ui.getTheme(state).palette.type);
  const dispatch = useDispatch();
  //
  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };
  //
  const handleDrawerOpen = () => {
    // FE
    dispatch(collapseNavigation(false));
    // BE save navigation is collapsed
    if (SecurityManager.isAuthenticated(userContext)) {
      dispatch(identityManager.saveCurrentProfile(userContext.id, {
        navigationCollapsed: false
      }));
    }
  };
  //
  const handleDrawerClose = () => {
    dispatch(collapseNavigation(true));
    // BE save navigation is collapsed
    if (SecurityManager.isAuthenticated(userContext)) {
      dispatch(identityManager.saveCurrentProfile(userContext.id, {
        navigationCollapsed: true
      }));
    }
  };
  //
  if (!SecurityManager.isAuthenticated(userContext)) {
    return (
      <div>
        <div className={ classes.grow }>
          <AppBar
            position="fixed"
            className={ classes.appBarRoot }>
            <Toolbar>
              <Link to="/" className="home light">
                {' '}
              </Link>
              { /*
                userContext.isExpired
                ||
                mainItems
              */}
              <div className={ classes.grow }/>
              <NavigationEnvironment />
              <NavigationLanguage />
              <IconButton
                color="inherit"
                onClick={() => {
                  dispatch({
                    type: 'THEME',
                    theme: themeType === 'light' ? 'dark' : 'light'
                  });
                }}>
                { themeType === 'light' ? <Brightness4Icon /> : <Brightness7Icon />}
              </IconButton>
            </Toolbar>
          </AppBar>
          <Toolbar />
        </div>
        { children }
      </div>
    );
  }
  //
  const drawer = (
    <div className={ clsx(classes.sidebar, { sidebar: true, collapsed: navigationCollapsed }) }>
      <div className={ classes.toolbar } style={{ display: 'flex', alignItems: 'center'}}>
        <Link to="/" className={ !navigationCollapsed ? 'home' : 'home collapsed' }>
          {' '}
        </Link>
        <div className={ !navigationCollapsed ? classes.grow : 'hidden' }/>
        <Hidden smDown>
          <IconButton
            color="inherit"
            aria-label="open close drawer"
            edge="start"
            onClick={ !navigationCollapsed ? handleDrawerClose : handleDrawerOpen }
            style={ !navigationCollapsed ? {} : { marginLeft: 5 } }>
            { !navigationCollapsed ? <ChevronLeftIcon /> : <MenuIcon /> }
          </IconButton>
        </Hidden>
      </div>
      <Divider />
      {
        sidebarItems
      }
    </div>
  );

  return (
    <div className={ classes.root }>
      <AppBar
        position="fixed"
        className={
          clsx(
            classes.appBarRoot,
            classes.appBar,
            {
              [classes.appBarShift]: !navigationCollapsed,
              [classes.appBarClose]: navigationCollapsed
            }
          )
        }>
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={ handleDrawerToggle }
            className={ classes.menuButton } >
            <MenuIcon />
          </IconButton>

          <NavigationSearch userContext={ userContext }/>

          <div className={ classes.grow }/>

          <NavigationEnvironment />

          <NavigationLanguage userContext={ userContext }/>

          <NavigationIdentity userContext={ userContext }/>

          {
            !SecurityManager.hasAuthority('MONITORINGRESULT_READ')
            ||
            <NavigationMonitoring location={ location }/>
          }

          <IconButton
            color="inherit"
            onClick={() => {
              dispatch({
                type: 'THEME',
                theme: themeType === 'light' ? 'dark' : 'light'
              });
            }}>
            { themeType === 'light' ? <Brightness4Icon /> : <Brightness7Icon />}
          </IconButton>

          {
            userContext.isExpired
            ||
            systemItems
          }
        </Toolbar>
      </AppBar>
      <Toolbar />
      <nav className={ classes.drawer }>
        {/* The implementation can be swapped with js to avoid SEO duplication of links. */}
        <Hidden mdUp>
          <Drawer
            variant="temporary"
            open={ mobileOpen }
            onClose={ handleDrawerToggle }
            classes={{
              paper: classes.drawerPaper,
            }}
            ModalProps={{
              keepMounted: true, // Better open performance on mobile.
            }}>
            { drawer }
          </Drawer>
        </Hidden>
        <Hidden smDown>
          <Drawer
            variant="permanent"
            className={
              clsx(
                classes.drawer,
                {
                  [classes.drawerOpen]: !navigationCollapsed,
                  [classes.drawerClose]: navigationCollapsed,
                }
              )
            }
            classes={{
              paper: clsx(
                {
                  [classes.drawerOpen]: !navigationCollapsed,
                  [classes.drawerClose]: navigationCollapsed,
                }
              ),
            }}
            open>
            { drawer }
          </Drawer>
        </Hidden>
      </nav>
      <main className={ classes.content }>
        <div className={ classes.toolbar }/>
        { children }
      </main>
    </div>
  );
}

NavigationMaterial.propTypes = {
  userContext: PropTypes.object.isRequired,
  location: PropTypes.object,
  systemItems: PropTypes.arrayOf(PropTypes.object).isRequired,
  sidebarItems: PropTypes.arrayOf(PropTypes.object).isRequired
};

NavigationMaterial.defaultProps = {
  location: null
};

export default NavigationMaterial;

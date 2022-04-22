import React from 'react';
import Select from 'react-select';
import {useHistory} from 'react-router';
import SearchIcon from '@material-ui/icons/Search';
import {alpha, makeStyles} from '@material-ui/core/styles';
import {i18n} from '../../../services/LocalizationService';
import OptionDecorator from '../../basic/SelectBox/OptionDecorator';
import EntityInfo from '../EntityInfo/EntityInfo';
import UiUtils from '../../../utils/UiUtils';
import ComponentService from '../../../services/ComponentService';
import UniversalSearchManager from '../../../redux/data/UniversalSearchManager';
import Div from '../../basic/Div/Div';
import SelectBox from '../../basic/SelectBox/SelectBox';
//
//
const universalSearchManager = new UniversalSearchManager();
const componentService = new ComponentService();

/**
 * Search box in navigation.
 *
 * @author Radek Tomiška
 * @author Vít Švanda
 * @since 10.3.0
 */
const useStyles = makeStyles((theme) => {
  return (
    {
      search: {
        position: 'relative',
        borderRadius: theme.shape.borderRadius,
        backgroundColor: alpha(theme.palette.common.white, 0.20),
        '&:hover': {
          backgroundColor: alpha(theme.palette.common.white, 0.30),
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
      selectBoxRoot: {
        '& .Select-control': {
          width: 450,
          border: 0,
          color: theme.palette.common.white,
          fontSize: '1rem'
        },
        '& .Select-value-label': {
          color: theme.palette.common.white
        },
        '& .Select-placeholder': {
          color: theme.palette.primary.contrastText
        },
        '& .Select-option': {
          backgroundColor: theme.palette.background.paper,
          paddingLeft: 8,
          paddingRight: 8,
          paddingTop: 4,
          paddingBottom: 4,
          '&:hover': {
            backgroundColor: theme.palette.type === 'dark' ? '#515151' : theme.palette.action.hover
          },
          '&.Uni-search-footer': {
            backgroundColor: theme.palette.background.default
          },
          '&.Uni-search-header': {
            backgroundColor: theme.palette.background.paper
          },
          '& .Uni-search-header': {
            backgroundColor: theme.palette.background.paper,
            color: theme.palette.text.disabled
          }
        },
        '& .Select-menu-outer': {
          backgroundColor: theme.palette.background.paper
        },
        '& .MuiSvgIcon-root': {
          color: theme.palette.primary.contrastText
        },
        '& .Search-placeholder': {
          color: theme.palette.primary.contrastText
        }
      }
    });
});

function NavigationSearch(props, context) {
  const [selectedItem, setSelectedItem] = React.useState(null);
  const history = useHistory();
  const classes = useStyles();
  const universalSearch = React.createRef();

  const handleMouseDown = (onSelect, fullObject, event) => {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    if (event && event.target && event.target.className.includes('basic-icon')) {
      // User clicked on info component, select event will be stopped now.
      // Workaround: The 'basic-icon' is here only because three in role-info (I need to stop select event if user clicked on tree).
      return;
    }
    if (onSelect) {
      onSelect(false, fullObject);
    }
  };

  const _onSelectChange = (selectItem, dto) => {
    if (!dto) {
      return;
    }
    // Clear input.
    universalSearch.current.setValue(null);
    universalSearch.current.resetInputValue();
    universalSearch.current.isOpen(false);
    setSelectedItem(selectItem ? dto : null);

    // We need to obtain link to the detail. We will use info component for this.
    const InfoComponent = EntityInfo.getComponent(UiUtils.getSimpleJavaType(dto.ownerType)).component.WrappedComponent;
    const InfoComponentInstance = (new InfoComponent({entityIdentifier: dto.ownerId}, context));

    const link = InfoComponentInstance.getLink(dto.ownerDto);
    // Push link only if is different then current location -> prevent of warning in console.
    if (history.location.pathname !== link) {
      history.push(link);
    }
  };

  const menuRenderer = (params) => {
    // Use default renderer in a hacky way.
    const menu = Select.defaultProps.menuRenderer(params);
    const result = [];
    const types = new Map();

    menu
      .filter(item => item.props.option && item.props.option.type)
      .forEach(item => {
        const type = item.props.option.type;
        types.set(type.id, type);
      });

    const numberTypes = types.size;
    let i = 0;
    types.forEach(type => {
      i += 1;
      // Find and render a header component.
      const universalSearchTypeComponent = componentService
        .getUniversalSearchTypeComponent(type.id);
      if (universalSearchTypeComponent) {
        result.push(
          <OptionDecorator key={`${type.id}-header`} className="Select-option Uni-search-header" option={{disable: true}}>
            <universalSearchTypeComponent.component
              universalSearchType={type}
              searchValue={params.inputValue}
              header
            />
          </OptionDecorator>
        );
      }
      menu
        .filter(item => item.props.option && item.props.option.type && item.props.option.type.id === type.id)
        .forEach(item => {
          const fullObject = item.props.option;
          result.push(
            <OptionDecorator
              key={item.key}
              className={item.props.className}
              option={fullObject}
              onFocus={item.props.onFocus}
              focusOption={item.props.focusOption}
            >
              {/* eslint-disable-next-line jsx-a11y/no-static-element-interactions */}
              <div
                onMouseDown={handleMouseDown.bind(this, _onSelectChange, fullObject)}
              >
                <Div
                  rendered={!!fullObject.ownerType}
                >
                  <EntityInfo
                    entityType={UiUtils.getSimpleJavaType(fullObject.ownerType)}
                    entityIdentifier={fullObject.ownerId}
                    face="popover"
                    trigger="hover"
                    entity={fullObject.ownerDto}
                    showLink={ false }
                    showSystemInformation={ false }
                    showEntityType
                    showIcon/>
                </Div>
              </div>
              <Div rendered={!fullObject.ownerType}>
                {item.props.children}
              </Div>
            </OptionDecorator>
          );
        });
      if (universalSearchTypeComponent) {
        result.push(
          <OptionDecorator key={`${type.id}-footer`} className="Select-option Uni-search-footer" option={{disable: true}}>
            <universalSearchTypeComponent.component
              universalSearchType={type}
              isLast={i === numberTypes}
              searchValue={params.inputValue}
              header={false}
            />
          </OptionDecorator>
        );
      }
    });

    return result;
  };

  const _onOpenSelect = () => {
    if (universalSearch) {
      setSelectedItem(null);
      universalSearch.current.setValue(null);
      universalSearch.current.resetInputValue();
    }
  };
  //
  return (
    <div>
      <div className={classes.search}>
        <SelectBox
          ref={universalSearch}
          classes={{
            root: classes.selectBoxRoot
          }}
          style={{marginBottom: 0}}
          onOpen={_onOpenSelect.bind(this)}
          onChange={_onSelectChange.bind(this, true)}
          menuStyle={{maxHeight: 900}}
          menuContainerStyle={{maxHeight: 900}}
          manager={universalSearchManager}
          label={null}
          onSelectResetsInput
          menuRenderer={menuRenderer.bind(this)}
          placeholder={
            <Div rendered={!selectedItem}>
              <SearchIcon style={{marginTop: 8}} titleAccess={i18n('component.advanced.NavigationSearch.search.placeholder')}/>
              <span className="Search-placeholder" style={{marginLeft: 5, verticalAlign: 'top'}}>{i18n('component.advanced.NavigationSearch.search.placeholder')} ...</span>
            </Div>
          }/>
      </div>
    </div>
  );
}

export default NavigationSearch;

import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { AutoAffix } from 'react-overlays';
//
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';

//
const useStyles = makeStyles((theme) => {
  return {
    root: {
      backgroundColor: theme.palette.background.paper,
      borderTopLeftRadius: theme.shape.borderRadius,
      borderTopRightRadius: theme.shape.borderRadius
    }
  };
});

/**
 * Toolbar panel.
 *
 * @author Radek Tomiška
 */
export default function Toolbar(props) {
  const { className, rendered, showLoading, viewportOffsetTop, container, children, style } = props;
  const classes = useStyles();
  //
  if (!rendered) {
    return null;
  }
  //
  const classNames = classnames(
    'basic-toolbar',
    'form-inline',
    classes.root,
    className,
  );
  let render = (
    <div className={ classNames } style={ style }>
      <Loading className="simple" showLoading={ showLoading } showAnimation={ false } >
        { children }
      </Loading>
    </div>
  );
  if (viewportOffsetTop !== undefined) { // affix decorator, when viewportOffsetTop is defined
    render = (
      <AutoAffix viewportOffsetTop={ viewportOffsetTop || 64 } container={ container }>
        { render }
      </AutoAffix>
    );
  }
  //
  return render;
}

Toolbar.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * When affixed, pixels to offset from top of viewport
   */
  viewportOffsetTop: PropTypes.number,
  /**
   * The logical container node or component for determining offset from bottom of viewport, or a function that returns it
   */
  container: PropTypes.object
};

Toolbar.defaultProps = {
  ...AbstractComponent.defaultProps
};

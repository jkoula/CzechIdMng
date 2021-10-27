import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
//
import Icon from '../Icon/Icon';

const ASC = 'ASC';
const DESC = 'DESC';

const useStyles = makeStyles((theme) => ({
  root: {
    color: theme.palette.text.hint
  },
  active: {
    color: theme.palette.text.primary
  }
}));

/**
 * Sort icon.
 *
 * @since 12.0.0
 */
export default function SortIcon(props) {
  const { active, showLoading } = props;
  const classes = useStyles();
  //
  const ascClassName = classnames(
    classes.root,
    'sort-icon sort-asc',
    { [classes.active]: active === ASC}
  );
  const descClassName = classnames(
    classes.root,
    'sort-icon sort-desc',
    { [classes.active]: active === DESC }
  );

  return (
    <span className="sort-icons">
      {
        (showLoading && active)
        ?
        <Icon type="fa" icon="refresh" showLoading className="sort-icon active"/>
        :
        <span>
          <Icon icon="triangle-top" className={ ascClassName }/>
          <Icon icon="triangle-bottom" className={ descClassName }/>
        </span>
      }
    </span>
  );
}

SortIcon.propTypes = {
  /**
   * current order for current property
   */
  active: PropTypes.oneOf([ASC, DESC]),
  /**
   * loadinig indicator
   */
  showLoading: PropTypes.bool
};
SortIcon.defaultProps = {
  active: null,
  showLoading: false
};

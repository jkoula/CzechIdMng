import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import { makeStyles } from '@material-ui/core/styles';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Theme decorator.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
const useStyles = makeStyles((theme) => {
  return {
    root: {
      margin: '2em 0',
      lineHeight: 0,
      textAlign: 'center',
      '& span': {
        backgroundColor: theme.palette.background.paper,
        padding: '1em'
      },
      '&:before': {
        content: '" "',
        display: 'block',
        borderTop: `1px solid ${ theme.palette.divider }`,
        borderBottom: `1px solid ${ theme.palette.divider }`
      }
    }
  };
});

/**
 * Divider with centered text.
 *
 * @author Radek Tomiška
 */
export default function TextDivider(props) {
  const { rendered, text, value, className, style } = props;
  const _text = text || value;
  const classes = useStyles();
  //
  if (!rendered || !_text) {
    return null;
  }
  //
  const classNames = classnames(
    'text-divider',
    classes.root,
    className
  );
  //
  return (
    <div className={ classNames } style={ style }>
      <span>{ _text }</span>
    </div>
  );
}

TextDivider.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Label text content
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ]),
  /**
   * Label text content (text alias - text has higher priority)
   */
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.number
  ])
};

TextDivider.defaultProps = {
  ...AbstractComponent.defaultProps
};

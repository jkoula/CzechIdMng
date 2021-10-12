import React from 'react';
import Container from '@material-ui/core/Container';

/**
 * Container decorator.
 *
 * TODO: rendered
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
export default function BasicContainer(props) {
  return (
    <Container { ...props }/>
  );
}

import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Tab - function wrapper only.
* @see Basic.Tabs
 * @author Radek Tomiška
 */
export default function BasicTab(props) {
  const { rendered, children } = props;
  //
  if (!rendered) {
    return null;
  }
  return children;
}

BasicTab.propTypes = {
  ...AbstractComponent.propTypes
};

BasicTab.defaultProps = {
  ...AbstractComponent.defaultProps
};

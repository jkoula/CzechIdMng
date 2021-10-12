import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
//
import * as Basic from '../../basic';
import {SelectBox} from '../../basic/SelectBox/SelectBox';

/**
 * Boolean select box used in filters
 *
 * @author Radek Tomiška
 */
export class FilterBooleanSelectBox extends Basic.BooleanSelectBox {

}

FilterBooleanSelectBox.propTypes = {
  ...Basic.BooleanSelectBox.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ', 'NEQ'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.BooleanSelectBox.defaultProps; // labelSpan etc. override
FilterBooleanSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(FilterBooleanSelectBox);

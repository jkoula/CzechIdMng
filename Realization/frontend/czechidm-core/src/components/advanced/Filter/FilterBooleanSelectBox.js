import PropTypes from 'prop-types';
//
import { withStyles } from '@material-ui/core/styles';
//
import { SelectBox } from '../../basic/SelectBox/SelectBox';
import { BooleanSelectBox } from '../../basic/BooleanSelectBox/BooleanSelectBox';

/**
 * Boolean select box used in filters
 *
 * @author Radek Tomiška
 */
export class FilterBooleanSelectBox extends BooleanSelectBox {

}

FilterBooleanSelectBox.propTypes = {
  ...BooleanSelectBox.propTypes,
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
const { labelSpan, componentSpan, ...otherDefaultProps } = BooleanSelectBox.defaultProps; // labelSpan etc. override
FilterBooleanSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(FilterBooleanSelectBox);

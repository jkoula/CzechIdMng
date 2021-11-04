import PropTypes from 'prop-types';
//
import { withStyles } from '@material-ui/core/styles';
//
import { DateTimePicker } from '../../basic/DateTimePicker/DateTimePicker';

/**
 * DateTimePicker used in filters
 *
 * @author Radek Tomiška
 */
export class FilterDateTimePicker extends DateTimePicker {

}

FilterDateTimePicker.propTypes = {
  ...DateTimePicker.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ', 'NEQ', 'LT', 'LE', 'GT', 'GE', 'IN', 'IS_NULL', 'IS_NOT_NULL', 'IS_EMPTY', 'IS_NOT_EMPTY'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = DateTimePicker.defaultProps; // labelSpan etc. override
FilterDateTimePicker.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};

export default withStyles(DateTimePicker.STYLES, { withTheme: true })(FilterDateTimePicker);

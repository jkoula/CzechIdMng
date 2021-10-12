import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import {SelectBox} from '../../basic/SelectBox/SelectBox';
import {EnumSelectBox} from '../../basic/EnumSelectBox/EnumSelectBox';

/**
 * Enum select box used in filters.
 *
 * @author Radek Tomiška
 */
export class FilterEnumSelectBox extends EnumSelectBox {

}

FilterEnumSelectBox.propTypes = {
  ...EnumSelectBox.propTypes,
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
const { labelSpan, componentSpan, ...otherDefaultProps } = EnumSelectBox.defaultProps; // labelSpan etc. override
FilterEnumSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(FilterEnumSelectBox);

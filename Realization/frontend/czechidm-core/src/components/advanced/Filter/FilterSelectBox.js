import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import {SelectBox} from '../../basic/SelectBox/SelectBox';

/**
 *  Select box used in filters
 *
 * @author Radek Tomiška
 */
export class FilterSelectBox extends SelectBox {

}

FilterSelectBox.propTypes = {
  ...SelectBox.propTypes,
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
const { labelSpan, componentSpan, disableable, ...otherDefaultProps } = SelectBox.defaultProps; // labelSpan etc. override
FilterSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  disableable: false
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(FilterSelectBox);

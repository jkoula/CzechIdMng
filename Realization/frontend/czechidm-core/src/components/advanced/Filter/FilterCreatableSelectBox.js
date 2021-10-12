import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import CreatableSelectBox from '../CreatableSelectBox/CreatableSelectBox';
import {SelectBox} from '../../basic/SelectBox/SelectBox';

/**
 * Select box supports creating new options used in filters.
 *
 * @author Ondřej Kopr
 */
export class FilterCreatableSelectBox extends CreatableSelectBox {

  getValue() {
    const value = super.getValue();

    if (value && value.length === 0) {
      return null;
    }
    return value;
  }
}

FilterCreatableSelectBox.propTypes = {
  ...CreatableSelectBox.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ'])
};
//
const { labelSpan, componentSpan, ...otherDefaultProps } = CreatableSelectBox.defaultProps; // labelSpan etc. override
FilterCreatableSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ'
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(FilterCreatableSelectBox);

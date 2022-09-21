import { Basic, Utils } from 'czechidm-core';

/**
 * Identity select value decorator.
 *
 * @author Radek Tomiška
 * @since 10.1.0
 */
export default class AccountValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (!entity) {
      // default
      return 'component:identity';
    }
    if (Utils.Entity.isDisabled(entity) || (entity._disabled && entity._disabled === true)) {
      // disabled (+ _disabled by not disableable select box)
      return 'component:disabled-identity';
    }
    // enabled
    return 'component:enabled-identity';
  }

}

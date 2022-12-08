import { Basic } from 'czechidm-core';

/**
 * Identity select option decorator.
 *
 * @author Peter Štrunc
 * @since 13.0.0
 */
export default class AccountOptionDecorator extends Basic.SelectBox.OptionDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
      return 'component:account';
  }

}

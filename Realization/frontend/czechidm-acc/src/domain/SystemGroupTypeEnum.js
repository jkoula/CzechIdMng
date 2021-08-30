import { Enums } from 'czechidm-core';

/**
 * Type for system group.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
export default class SystemGroupTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SystemGroupTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getIcon(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.CROSS_DOMAIN: {
        return 'fa:layer-group';
      }
      default: {
        return null;
      }
    }
  }
}

SystemGroupTypeEnum.CROSS_DOMAIN = Symbol('CROSS_DOMAIN');

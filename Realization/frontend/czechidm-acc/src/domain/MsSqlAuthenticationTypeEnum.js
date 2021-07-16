import {Enums} from 'czechidm-core';

/**
 * Authentication type for MS-SQL.
 *
 * @author Vít Švanda
 * @since 11.2.0
 */
export default class MsSqlAuthenticationTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.MsSqlAuthenticationTypeEnum.${key}`);
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
      case this.WINDOWS_AUTHENTICATION: {
        return 'fa:window-restore';
      }
      case this.SQL_SERVER_AUTHENTICATION: {
        return 'fa:key';
      }
      default: {
        return null;
      }
    }
  }
}

MsSqlAuthenticationTypeEnum.WINDOWS_AUTHENTICATION = Symbol('WINDOWS_AUTHENTICATION');
MsSqlAuthenticationTypeEnum.SQL_SERVER_AUTHENTICATION = Symbol('SQL_SERVER_AUTHENTICATION');

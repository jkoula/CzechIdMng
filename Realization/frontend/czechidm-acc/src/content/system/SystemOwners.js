import React from "react";
import Helmet from "react-helmet";
import { Basic } from "czechidm-core";
import SystemOwnerRole from "./SystemOwnerRole";
import SystemOwner from "./SystemOwner";
import { Domain } from "czechidm-core";

export default class systemOwners extends Basic.AbstractContent {
  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return "acc:content.system.owner";
  }

  getNavigationKey() {
    return this.getRequestNavigationKey(
      "system-owner-role",
      this.props.match.params
    );
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter(
      "system",
      this.props.match.params.entityId
    );
    return (
      <Basic.Div>
        <Helmet title={this.i18n("title")} />
        {
          <Basic.Div>
            <Basic.ContentHeader
              icon="component:roles"
              text={this.i18n("acc:entity.SystemOwnerRole.header")}
              style={{ marginBottom: 0 }}
            />
            <SystemOwnerRole
              uiKey="system-owner-role"
              forceSearchParameters={forceSearchParameters}
              className="no-margin"
              match={this.props.match}
            />
          </Basic.Div>
        }
        {
          <Basic.Div>
            <Basic.ContentHeader
              icon="fa:group"
              text={this.i18n("acc:entity.SystemOwner.header")}
              style={{ marginBottom: 0 }}
            />
            <SystemOwner
              uiKey="system-owner"
              forceSearchParameters={forceSearchParameters}
              className="no-margin"
              match={this.props.match}
            />
          </Basic.Div>
        }
      </Basic.Div>
    );
  }
}

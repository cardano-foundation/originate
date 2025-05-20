import Keycloak from "keycloak-js";

/* global process */

export const keyCloakClient = new Keycloak({
  url: process.env.KEYCLOAK_HOST,
  realm: process.env.KEYCLOAK_REALM_NAME || "",
  clientId: process.env.KEY_CLOAK_CLIENT_CLIENT_ID || "",
});

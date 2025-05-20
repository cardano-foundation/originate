/* eslint-disable no-undef */
import { isPlatform } from "@ionic/react";

export const API_URL = `${process.env.BOLNISI_BACKEND_API}/api/v1`;
export const KEYCLOAK_SERVER = `${process.env.KEYCLOAK_HOST}/realms/${process.env.KEYCLOAK_REALM_NAME}`;
export const KEYCLOAK_CLIENT_ID = process.env.MOBILE_APP_KEYCLOAK_CLIENT_ID;
export const KEYCLOAK_REDIRECT_URI = isPlatform("capacitor")
  ? process.env.MOBILE_APP_KEYCLOAK_REDIRECT_URI
  : window.location.origin;
export const SCANTRUST_SCAN_URL = process.env.SCANTRUST_SCAN_URL;

export const ROLE_SYSTEM = {
  ADMIN: "ADMIN",
  NWA: "NWA",
  PROVIDER: "DATA_PROVIDER",
  WINERY: "WINERY",
};

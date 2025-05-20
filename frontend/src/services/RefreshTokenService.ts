import { axiosClient } from "./AxiosInterceptor";
/* global process */

export const RefreshTokenService = {
  refreshToken: (refreshToken: string) => {
    const url = `${process.env.KEYCLOAK_HOST}/realms/${process.env.KEYCLOAK_REALM_NAME}/protocol/openid-connect/token`;
    return axiosClient.post(
      url,
      {
        client_id: `${process.env.KEY_CLOAK_CLIENT_CLIENT_ID}`,
        grant_type: "refresh_token",
        refresh_token: refreshToken,
      },
      {
        headers: { "content-type": "application/x-www-form-urlencoded" },
      }
    );
  },
};

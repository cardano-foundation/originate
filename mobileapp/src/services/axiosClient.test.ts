import { waitFor } from "@testing-library/react";
import { instance } from "./AuthContext";
import { axiosClient } from "./axiosClient";

describe("axiosClient", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("should add authorization header when access token is present", () => {
    const config = {
      headers: {},
    };
    const tokenResponse = {
      access_token: "test",
    };
    window.localStorage.setItem(
      "token_response",
      JSON.stringify(tokenResponse)
    );
    (axiosClient.interceptors.request as any).handlers[0].fulfilled(config);
    expect((config.headers as any).Authorization).toBe("Bearer test");
  });

  test("should not add authorization header when access token is not present", () => {
    const config = {
      headers: {},
    };
    window.localStorage.removeItem("token_response");
    (axiosClient.interceptors.request as any).handlers[0].fulfilled(config);
    expect((config.headers as any).Authorization).toBeUndefined();
  });

  test("should return the response when status is 200", async () => {
    expect(
      (axiosClient.interceptors.response as any).handlers[0].fulfilled({
        data: "success",
      })
    ).toStrictEqual({ data: "success" });
  });

  test("should handle 401 response by refreshing the token and retrying the request", async () => {
    const originalRequestConfig = {
      url: "resources",
      method: "get",
      headers: {},
      baseURL: "https://base-url.com",
    };

    const error = {
      config: {
        url: "https://base-url.com/resources",
        headers: {},
        _retry: false,
      },
      response: {
        status: 401,
      },
    };

    const getValidTokenMock = jest.spyOn(instance, "getValidToken");
    getValidTokenMock.mockResolvedValueOnce({
      accessToken: "refreshed-access-token",
      expiresIn: 3600,
      tokenType: "bearer",
      refreshToken: "refresh-token",
      scope: "scope",
      idToken: "id-token",
      issuedAt: 123456789,
      toJson: jest.fn(),
      isValid: jest.fn(),
    });
    (axiosClient.interceptors.response as any).handlers[0].rejected({
      response: {
        status: 401,
      },
      config: originalRequestConfig,
    });
    const retryPromise = (
      axiosClient.interceptors.response as any
    ).handlers[0].rejected(error);
    await waitFor(() => {
      expect(getValidTokenMock).toHaveBeenCalled();
    });
    getValidTokenMock.mockRestore();
    await waitFor(() => {
      expect(retryPromise).rejects.toBeDefined();
    });
  });
});

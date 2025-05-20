import { ReactNode, useEffect, useRef } from "react";
import axios, { AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { useAuth } from "../contexts/AuthContext";
import { RefreshTokenService } from "./RefreshTokenService";
import { keyCloakClient } from "./Instances/KeyCloakServices";

/* global process */

const axiosClient = axios.create({
  baseURL: process.env.BOLNISI_BACKEND_API,
  headers: {
    "Content-Type": "application/json",
  },
});

interface AxiosInterceptorProps {
  children: ReactNode;
}

const AxiosInterceptor = ({ children }: AxiosInterceptorProps) => {
  const { validToken, isAuthenticated, setValidToken } = useAuth();
  const refreshToken = useRef(validToken?.refreshToken);

  const resInterceptor = (response: AxiosResponse) => {
    return response;
  };

  const errInterceptor = async (error: any) => {
    if (error.response.status === 401) {
      const originalRequest = error.config;
      const isRefreshTokenErrorApi = error.config.url.includes("refresh-token");
      if (
        error.response.status === 401 &&
        !originalRequest._retry &&
        !isRefreshTokenErrorApi &&
        refreshToken.current
      ) {
        originalRequest._retry = true;
        try {
          const { data } = await RefreshTokenService.refreshToken(
            refreshToken.current
          );
          originalRequest.headers.Authorization = `Bearer ${data?.access_token}`;
          originalRequest.headers["Content-Type"] =
            error.config?.data?.constructor.name === "FormData"
              ? "multipart/form-data"
              : "application/json";
          setValidToken({
            accessToken: data?.access_token || "",
            role: validToken?.role || "",
            refreshToken: data?.refresh_token || "",
          });
          return axiosClient(originalRequest);
        } catch (error) {
          keyCloakClient.logout({
            redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}?${process.env.REACT_APP_REDIRECT_KEY_CLOAK_LOGOUT_AUTHORIZE_QUERY}`,
          });
        }
      }
    }

    return Promise.reject(error.response);
  };

  axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    if (validToken?.accessToken) {
      config.headers.Authorization =
        config?.headers?.Authorization ?? `Bearer ${validToken.accessToken}`;
    }

    return config;
  });

  useEffect(() => {
    refreshToken.current = validToken?.refreshToken;
  }, [validToken?.refreshToken]);

  useEffect(() => {
    if (isAuthenticated) {
      axiosClient.interceptors.response.use(resInterceptor, errInterceptor);
    }
  }, [isAuthenticated]);

  return <>{children}</>;
};

export { AxiosInterceptor, axiosClient };

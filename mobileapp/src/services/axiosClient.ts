import axios, { AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { API_URL } from "../utils";
import { instance } from "./AuthContext";

const axiosClient = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const tokenResponse = window.localStorage.getItem("token_response");
  const accessToken = tokenResponse
    ? JSON.parse(tokenResponse)?.access_token
    : null;
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

axiosClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },

  async (error) => {
    const config = error.config;
    if (error?.response?.status === 401 && !config._retry) {
      config._retry = true;
      const response = await instance.getValidToken();
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${response.accessToken}`,
      };
      return axiosClient(config);
    }
    return Promise.reject(error);
  }
);

export { axiosClient };

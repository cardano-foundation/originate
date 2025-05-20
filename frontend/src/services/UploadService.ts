import { AxiosRequestConfig } from "axios";
import { axiosClient } from "./AxiosInterceptor";
import { BASE_URL } from "./contants";

export const UploadService = {
  uploadWinerySCM: (
    wineryId: string,
    data: File,
    config?: AxiosRequestConfig
  ) => {
    const formData = new FormData();
    formData.append("data", data);
    return axiosClient.post(`${BASE_URL}${wineryId}`, formData, {
      headers: {
        "content-type": "multipart/form-data",
      },
      ...config,
    });
  },
  uploadBottleMapping: (
    wineryId: string,
    data: File,
    config?: AxiosRequestConfig
  ) => {
    const formData = new FormData();
    formData.append("data", data);
    return axiosClient.post(`api/v1/bottles/${wineryId}`, formData, {
      headers: {
        "content-type": "multipart/form-data",
      },
      ...config,
    });
  },
};

import { axiosClient } from "./AxiosInterceptor";
import { BASE_URL } from "./contants";

export const GetDataSCMService = {
  getDataTable: (WineryId: string) => {
    const url = `${BASE_URL}${WineryId}`;
    return axiosClient.get(url);
  },
};

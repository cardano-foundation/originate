import { axiosClient } from "./AxiosInterceptor";

export const WineryService = {
  getUserWinery: () => {
    const url = "/api/v1/user/winery";
    return axiosClient.get(url);
  },
};

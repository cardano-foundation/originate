import { axiosClient } from "./AxiosInterceptor";

export const ViewBottleMappingDataSCMService = {
  viewBottleMapping: (WineryId: string) => {
    const url = `api/v1/bottles/${WineryId}`;
    return axiosClient.get(url);
  },
};

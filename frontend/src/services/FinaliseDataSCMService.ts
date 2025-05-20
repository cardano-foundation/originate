import { axiosClient } from "./AxiosInterceptor";
import { BASE_URL } from "./contants";

export const FinaliseDataSCMService = {
  finaliseDataSCM: (WineryId: string, lotIds: string[]) => {
    const url = `${BASE_URL}${WineryId}/finalise`;
    return axiosClient.put(url, lotIds);
  },
};

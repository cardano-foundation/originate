import { axiosClient } from "./AxiosInterceptor";
import { BASE_URL } from "./contants";

export const ApproveDataSCMService = {
  approveDataSCM: (WineryId: string, lotIds: string[]) => {
    const url = `${BASE_URL}${WineryId}/approve`;
    return axiosClient.put(url, lotIds);
  },
};

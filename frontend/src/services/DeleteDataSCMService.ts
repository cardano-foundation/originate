import { axiosClient } from "./AxiosInterceptor";
import { BASE_URL } from "./contants";

export const DeleteDataSCMService = {
  deleteDataSCM: (WineryId: string, lotIds: string[]) => {
    const url = `${BASE_URL}${WineryId}/delete`;
    return axiosClient.post(url, lotIds);
  },
};

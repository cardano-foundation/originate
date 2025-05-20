import { axiosClient } from "./AxiosInterceptor";

export const TermsAcceptService = {
  termsAccept: () => {
    const url = "/api/v1/user/terms/accept";
    return axiosClient.post(url);
  },
};

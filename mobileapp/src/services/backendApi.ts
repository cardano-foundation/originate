import { AxiosResponse } from "axios";
import { Certificate, DataSaveBottle, ScanRange } from "../ui/common/responses";
import { axiosClient } from "./axiosClient";
import { BottleByCertLot } from "../ui/hooks/types";

const BackendAPI = {
  getCert: (wineryId: string): Promise<AxiosResponse<Certificate[]>> => {
    const url = `certs/winery/${wineryId}`;
    return axiosClient.get(url);
  },
  getBottleByLot: (
    lotId: string,
    wineryId: string
  ): Promise<AxiosResponse<BottleByCertLot[]>> => {
    const url = `bottles/${wineryId}/lots/${lotId}`;
    return axiosClient.get(url);
  },
  saveAndContinue: (
    wineryId: string,
    lotId: string,
    certId: string,
    data: DataSaveBottle
  ): Promise<AxiosResponse> => {
    const url = `bottles/${wineryId}/certs/${certId}/${lotId}`;
    return axiosClient.put(url, data);
  },
  getWinery: (): Promise<AxiosResponse> => {
    const url = "user/winery";
    return axiosClient.get(url);
  },
  agreeTerm: (): Promise<AxiosResponse> => {
    const url = "user/terms/accept";
    return axiosClient.post(url);
  },
  getInfoBottle: (wineryId: string, bottleId: string) => {
    const url = `bottles/${wineryId}/bottle/${bottleId}`;
    return axiosClient.get(url);
  },
  saveScanRangeApi: (
    wineryId: string,
    lotId: string,
    certId: string,
    data: ScanRange
  ) => {
    const url = `bottles/range-scan/${wineryId}/certs/${certId}/${lotId}`;
    return axiosClient.put(url, data);
  },
};

export { BackendAPI };

import { BottleByCertLot } from "../hooks/types";

export interface ErrorResponseBase {
  meta: {
    code: string;
    message: string;
    internalMessage: string;
  };
}

export interface CertificateProduct {
  lotId: string;
  scanningStatus: "APPROVED" | "SCANNING" | "NOT_STARTED";
}

export interface Certificate {
  id: string;
  certificateNumber: string;
  certificateType: string;
  lotEntries: CertificateProduct[];
}

export interface CertsByCategory {
  listRequired: Certificate[];
  listCompleted: Certificate[];
}

export interface Winery {
  wineryId: string;
  wineryName: string;
}

export interface BottleListResponse {
  lotId: string;
  bottles: BottleByCertLot[];
  certId: string;
}

export interface Bottle {
  bottleId: string;
  reelNumber: number;
  sequentialNumber: number;
}

export interface DataSaveBottle {
  add: string[];
  remove: string[];
  finalise: boolean;
}

export interface ScanRange {
  startRange: string;
  endRange: string;
  isSequentialNumber: boolean;
  finalise: boolean;
}

export type BottleInfo = {
  certId: string;
  certNumber: string;
  certType: string;
  lotId: string;
  scanningStatus: string;
  sequentialNumber: string;
  reelNumber: string;
};

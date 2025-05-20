import { MutableRefObject } from "react";
import { CertProductDetails } from "../components";
import { Winery } from "../common/responses";

export type BottleByCertLot = {
  id: string;
  lotId: string;
  sequentialNumber: number;
  reelNumber: number;
  certificateId: string | null;
};

export type BarcodeScanResult = {
  content: string;
  hasContent: boolean;
};

export type UseScanner = {
  checkPermission: () => Promise<boolean>;
  startScan: () => Promise<BarcodeScanResult>;
  stopScan: () => Promise<void>;
  startScanning: () => Promise<void>;
  setCurrentLot: (lot?: CertProductDetails) => void;
  setShowModalError: (show: boolean) => void;
  setCurrentScanned?: (data: BottleByCertLot[]) => void;
  handleModalError?: (msg: string, textBtn: string) => void;
  setIsRangeScan: (isRangeScan: boolean) => void;
  scannedStatus: boolean;
  modalErrorConfirmText: string;
  errorMessage: string;
  showModalError: boolean;
  totalScanned: number;
  currentLot: CertProductDetails | undefined;
  isGetLotError: boolean;
  currentScanned?: BottleByCertLot[];
  winery?: Winery;
  currentScannedRef: MutableRefObject<BottleByCertLot[]>;
  listRemoveRef: MutableRefObject<BottleByCertLot[]>;
  isRangeScan: boolean;
  sortableScannedRange: BottleByCertLot[];
  popupConfirmProps(): PopupConfirmProps;
  isPopupConfirmOpen: boolean;
  unableToSaveStatus: RangeScanSaveStatus;
  setUnableToSaveStatus: (status: RangeScanSaveStatus) => void;
  confirmRescanLot: boolean;
  rangeScanningStatus: RangeScanSaveStatus;
  confirmApproveRangeScan: boolean;
  setRangeScanningStatus: (status: RangeScanSaveStatus) => void;
  setConfirmApproveRangeScan: (approve: boolean) => void;
  setConfirmRescanLot: (rescan: boolean) => void;
};

export type PopupConfirmProps = {
  title: string;
  okText: string;
  cancelAction?: () => void;
};

export type RangeScanSaveStatus = {
  error: boolean;
  approve?: boolean;
}

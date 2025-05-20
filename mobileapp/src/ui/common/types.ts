export enum QRType {
  ONE_LOT = "ONE_LOT",
  MANY_LOTS = "MANY_LOTS",
  COMPLETED = "COMPLETED",
}

export enum LanguageType {
  English = "en",
  Georgian = "ka",
}

export enum BottleType {
  NOT_SCAN = "NOT_STARTED",
  SCANNED = "SCANNING",
  SCANNED_APPROVED = "APPROVED",
}

export enum LotStatus {
  NOT_STARTED = "NOT_STARTED",
  APPROVED = "APPROVED",
  SCANNING = "SCANNING",
}

export enum CertificateType {
  REQUIRED = "REQUIRED",
  COMPLETED = "COMPLETED",
}

export enum ListBottleType {
  SCANNING = "SCANNING",
  APPROVE = "APPROVE",
}

export enum ToastMessageType {
  SUCCESS = "SUCCESS",
  ERROR = "ERROR",
}

export enum ActionBottle {
  APPROVE = "APPROVE",
  DELETE_BOTTLE = "DELETE_BOTTLE",
}

export enum TypeScan {
  SCAN_BY_RANGE = "SCAN_BY_RANGE",
  SCAN_BY_BOTTLE = "SCAN_BY_BOTTLE",
}

export enum ModalViewed {
  SCAN_RANGE_INFO = "SCAN_RANGE_INFO",
  SCAN_BOTTLES_INFO = "SCAN_BOTTLES_INFO",
}

export enum TypePreferences {
  LANGUAGE = "language",
  ID_TOKEN = "idToken",
}

export interface TitleConfirm {
  title?: string;
  okText?: string;
  cancelText?: string;
}

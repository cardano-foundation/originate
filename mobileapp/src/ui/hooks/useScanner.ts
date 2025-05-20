import {
  BarcodeScanner,
  SupportedFormat,
} from "@capacitor-community/barcode-scanner";
import { isPlatform } from "@ionic/react";
import { useCallback, useContext, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { ToastMessageContext } from "../../context";
import { BackendAPI } from "../../services";
import { SCANTRUST_SCAN_URL } from "../../utils";
import { CertProductDetails } from "../components/CertOrProductCard";
import { Winery } from "../common/responses";
import { ToastMessageType } from "../common/types";
import {
  BarcodeScanResult,
  BottleByCertLot,
  PopupConfirmProps,
  RangeScanSaveStatus,
  UseScanner,
} from "./types";

const useScanner = (): UseScanner => {
  const { showToast } = useContext(ToastMessageContext);
  const [currentLot, setCurrentLot] = useState<CertProductDetails>();
  const [showModalError, setShowModalError] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [modalErrorConfirmText, setModalErrorConfirmText] =
    useState<string>("");
  const [scannedStatus, setScannedStatus] = useState<boolean>(false);
  const [currentScanned, setCurrentScanned] = useState<BottleByCertLot[]>([]);
  const currentScannedRef = useRef<BottleByCertLot[]>([]);
  const scannedSuccess = useRef<boolean>(false);
  const [isGetLotError, setIsGetLotError] = useState<boolean>(false);
  const { t } = useTranslation();

  //state for scan range
  const [isRangeScan, setIsRangeScan] = useState<boolean>(false);
  const [confirmApproveRangeScan, setConfirmApproveRangeScan] =
    useState<boolean>(false);
  const [confirmRescanLot, setConfirmRescanLot] = useState<boolean>(false);
  const [rangeScanningStatus, setRangeScanningStatus] = useState<RangeScanSaveStatus>({ error: false });
  const [unableToSaveStatus, setUnableToSaveStatus] = useState<RangeScanSaveStatus>({ error: false });
  
  const [listBottleByCertLot, setListBottleByCertLot] = useState<
    BottleByCertLot[]
  >([]);
  const listRemoveRef = useRef<BottleByCertLot[]>([]);

  const initScan = useCallback(async () => {
    if (isPlatform("ios") || isPlatform("android")) {
      await BarcodeScanner.prepare();
    }
  }, []);

  const [winery, setWinery] = useState<Winery>({
    wineryId: "",
    wineryName: "",
  });

  const getWinery = async () => {
    try {
      const response = await BackendAPI.getWinery();
      if (response.data.length > 0) {
        setWinery({
          wineryId: response.data[0].wineryId,
          wineryName: response.data[0].wineryName,
        });
      }
    } catch (err: any) {
      showToast(t("somethingWentWrongPleaseTryAgain"), ToastMessageType.ERROR);
    }
  };

  useEffect(() => {
    getWinery();
  }, []);

  const getBottleByLot = useCallback(async () => {
    try {
      if (!currentLot?.lotId || !winery.wineryId) return;
      const response = await BackendAPI.getBottleByLot(
        currentLot?.lotId,
        winery.wineryId
      );
      const currentScan = response.data.filter(
        (dt) =>
          dt.certificateId !== null && dt.certificateId === currentLot.certId
      );
      setListBottleByCertLot(response.data);
      if (!isRangeScan) {
        setCurrentScanned(currentScan);
        currentScannedRef.current = currentScan;
      }
      setIsGetLotError(false);
    } catch (error: any) {
      if (error?.response?.status === 404) {
        setIsGetLotError(true);
      } else {
        showToast(
          t("somethingWentWrongPleaseTryAgain"),
          ToastMessageType.ERROR
        );
      }
    }
  }, [
    currentLot?.certId,
    currentLot?.lotId,
    showToast,
    t,
    isRangeScan,
    winery.wineryId,
  ]);

  useEffect(() => {
    getBottleByLot();
  }, [getBottleByLot]);

  useEffect(() => {
    const timeout = setTimeout(() => {
      if (scannedStatus) {
        setScannedStatus(false);
        scannedSuccess.current = false;
        const isFinishRangeScan =
          isRangeScan && currentScannedRef.current.length >= 2;
        if (!isFinishRangeScan) {
          BarcodeScanner.resumeScanning();
        }
      }
    }, 2000);
    return () => {
      clearTimeout(timeout);
    };
  }, [scannedStatus, isRangeScan]);

  useEffect(() => {
    initScan();
    return () => {
      BarcodeScanner.stopScan();
    };
  }, [initScan]);

  const checkPermission = async () => {
    const status = await BarcodeScanner.checkPermission({ force: true });
    if (status.granted) {
      return true;
    }
    const c = confirm(
      "If you want to grant permission for using your camera, enable it in the app settings and try again."
    );
    if (c) {
      BarcodeScanner.openAppSettings();
    }
    return false;
  };

  const startScan = async (): Promise<BarcodeScanResult> => {
    await BarcodeScanner.hideBackground();
    return new Promise((resolve) => {
      BarcodeScanner.startScanning(
        {
          targetedFormats: [SupportedFormat.QR_CODE],
        },
        (result) => {
          resolve(result as BarcodeScanResult);
        }
      );
    });
  };

  const stopScan = async () => {
    await BarcodeScanner.stopScan();
    await BarcodeScanner.showBackground();
  };

  const startScanning = async () => {
    await BarcodeScanner.hideBackground();
    await BarcodeScanner.startScanning(
      {
        targetedFormats: [SupportedFormat.QR_CODE],
      },
      async (result) => {
        if (result.hasContent) {
          try {
            const scanningItem: string[] = [
              ...currentScannedRef.current.map((item) => item.id),
            ];
            const qrContent = result.content;
            const strippedQrContent = qrContent.replace(
              `${SCANTRUST_SCAN_URL}`,
              ""
            );
            const findBottle = listBottleByCertLot.find(
              (item) => item.id === strippedQrContent
            );
            if (scanningItem.length === 2 && isRangeScan) {
              await BarcodeScanner.pauseScanning();
            } else if (!qrContent.startsWith(`${SCANTRUST_SCAN_URL}`)) {
              handleModalError("scanNotRecognized", "tryScanAgain");
            } else if (!findBottle) {
              handleModalError("scanExtendIdWrong", "scanNewQrCode");
            } else if (
              findBottle.certificateId &&
              findBottle.certificateId === currentLot?.certId &&
              !listRemoveRef.current.includes(findBottle)
            ) {
              handleModalError("scanExtendIdExisted", "scanNewQrCode");
            } else if (
              findBottle.certificateId &&
              findBottle.certificateId !== currentLot?.certId &&
              !listRemoveRef.current.includes(findBottle)
            ) {
              handleModalError(
                "scanExtendIdAlreadyAssociated",
                "scanNewQrCode"
              );
            } else if (
              scanningItem.includes(strippedQrContent) &&
              !scannedSuccess.current &&
              !listRemoveRef.current.includes(findBottle)
            ) {
              handleModalError("scanExtendIdExisted", "scanNewQrCode");
            } else if (!scanningItem.includes(strippedQrContent)) {
              scanningItem.push(strippedQrContent);
              setCurrentScanned((prev) => {
                const currentScan = [findBottle, ...prev];
                currentScannedRef.current = currentScan;
                return currentScan;
              });
              listRemoveRef.current = listRemoveRef.current.filter(
                (item) => item !== findBottle
              );
              scannedSuccess.current = true;
              setScannedStatus(true);
              await BarcodeScanner.pauseScanning();
            }
          } catch (err) {
            handleModalError("scanNotRecognized", "tryScanAgain");
          }
        }
      }
    );
  };

  const handleModalError = (msg: string, textBtn: string) => {
    setErrorMessage(msg);
    setModalErrorConfirmText(textBtn);
    setShowModalError(true);
    BarcodeScanner.pauseScanning();
  };

  //functions for scan range
  const sortableScannedRange = currentScanned.sort((a, b) => {
    return a.sequentialNumber - b.sequentialNumber;
  });

  const handleCancelScanningIssue = () => {
    showToast(rangeScanningStatus.approve ? t("unableToApproveLot") : t("unableToSaveLot"), ToastMessageType.ERROR);
    setRangeScanningStatus({ error: false });
  };

  const handleCancelUnableToSave = () => {
    showToast(unableToSaveStatus.approve ? t("unableToApproveLot") : t("unableToSaveLot"), ToastMessageType.ERROR);
    setUnableToSaveStatus({ error: false });
  };

  const popupConfirmProps = (): PopupConfirmProps => {
    if (confirmApproveRangeScan) {
      return {
        title: t("titleApproveLot"),
        okText: t("yesApprove"),
        cancelAction: () => setConfirmApproveRangeScan(false),
      };
    } else if (confirmRescanLot) {
      return {
        title: t("confirmRescan"),
        okText: t("yesRescan"),
        cancelAction: () => setConfirmRescanLot(false),
      };
    } else if (rangeScanningStatus.error) {
      return {
        title: t("scanningRangeIssue"),
        okText: t("scanDifferentRange"),
        cancelAction: handleCancelScanningIssue,
      };
    } else if (unableToSaveStatus.error) {
      return {
        title: t(unableToSaveStatus.approve ? "unableToApprove" : "unableToSave", {
          certNumber: currentLot?.certNumber,
          certType: currentLot?.certType,
          lotId: currentLot?.lotId,
        }),
        okText: t("trySavingAgain"),
        cancelAction: handleCancelUnableToSave,
      };
    } else {
      return {
        okText: t(modalErrorConfirmText),
        title: t(errorMessage),
        cancelAction: undefined,
      };
    }
  };

  const isPopupConfirmOpen =
    showModalError ||
    confirmRescanLot ||
    confirmApproveRangeScan ||
    rangeScanningStatus.error ||
    unableToSaveStatus.error;
  return {
    checkPermission,
    startScan,
    stopScan,
    startScanning,
    setCurrentLot,
    setShowModalError,
    setCurrentScanned,
    handleModalError,
    scannedStatus,
    modalErrorConfirmText,
    errorMessage,
    showModalError,
    totalScanned: currentScanned.length,
    currentLot,
    currentScanned,
    winery,
    currentScannedRef,
    listRemoveRef,
    isGetLotError,
    //scan range
    popupConfirmProps,
    isPopupConfirmOpen,
    unableToSaveStatus,
    setUnableToSaveStatus,
    confirmRescanLot,
    rangeScanningStatus,
    setRangeScanningStatus,
    isRangeScan,
    sortableScannedRange,
    confirmApproveRangeScan,
    setConfirmApproveRangeScan,
    setConfirmRescanLot,
    setIsRangeScan,
  };
};

export { useScanner };

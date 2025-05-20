import { BarcodeScanner } from "@capacitor-community/barcode-scanner";
import {
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonList,
  IonPage,
  IonRow,
  IonTitle,
  IonToolbar,
} from "@ionic/react";
import { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { Preferences } from "@capacitor/preferences";
import { ToastMessageContext } from "../../../context";
import { BackendAPI } from "../../../services";
import {
  BackButton,
  CertProductDetails,
  ModalConfirm,
  PopupListBottle,
  PopupSave,
  CertOrProductCard,
  ScanningView,
} from "../../components";
import { PopupCancelScan } from "../../components/PopupCancelScan";
import {
  Certificate,
  DataSaveBottle,
  CertificateProduct,
  ScanRange,
} from "../../common/responses";
import {
  ListBottleType,
  LotStatus,
  ModalViewed,
  QRType,
  ToastMessageType,
  TypeScan,
} from "../../common/types";
import { useScanner } from "../../hooks/useScanner";
import "./style.scss";

export type IData = {
  certId: string;
  lotId: string;
};

const CertProductView = () => {
  const { t } = useTranslation();
  const { showToast } = useContext(ToastMessageContext);
  const [showModal, setShowModal] = useState<boolean>(false);
  const [showPopupCancel, setShowPopupCancel] = useState<boolean>(false);
  const [title, setTitle] = useState<string>("");
  const [textConfirm, setTextConfirm] = useState<string>(`${t("yesLetScan")}`);
  const [openScan, setOpenScan] = useState<boolean>(false);
  const [certData, setCertData] = useState<Certificate | undefined>();
  const [showErrorModal, setShowErrorModal] = useState<boolean>(false);
  const [isShowPopupListBottle, setIsShowPopupListBottle] =
    useState<boolean>(false);
  const [isShowPopupSave, setIsShowPopupSave] = useState<boolean>(false);
  const [listBottleType, setListBottleType] = useState<ListBottleType>(
    ListBottleType.SCANNING
  );
  const [showScanInfoModal, setShowScanInfoModal] = useState<boolean>(false);
  const [titleScanInfoModal, setTitleScanInfoModal] = useState<string>("");
  const [typeScan, setTypeScan] = useState<string>("");

  const {
    stopScan,
    checkPermission,
    setCurrentLot,
    startScanning,
    setShowModalError,
    setCurrentScanned,
    currentLot,
    scannedStatus,
    currentScanned,
    winery,
    currentScannedRef,
    listRemoveRef,
    isGetLotError,
    isPopupConfirmOpen,
    isRangeScan,
    sortableScannedRange,
    setConfirmApproveRangeScan,
    setConfirmRescanLot,
    unableToSaveStatus,
    setUnableToSaveStatus,
    confirmRescanLot,
    rangeScanningStatus,
    confirmApproveRangeScan,
    setRangeScanningStatus,
    popupConfirmProps,
    setIsRangeScan,
  } = useScanner();
  const history = useHistory();

  useEffect(() => {
    if (
      currentScanned &&
      currentScanned.length >= 2 &&
      openScan &&
      isRangeScan
    ) {
      setIsShowPopupSave(true);
    }
  }, [currentScanned, openScan, isRangeScan]);

  useEffect(() => {
    const historyState: any = history?.location?.state;
    setCertData(historyState);
  }, [history?.location?.state]);

  useEffect(() => {
    const pauseScan = async () => {
      await BarcodeScanner.pauseScanning();
    };
    if (isShowPopupListBottle) {
      pauseScan();
    }
  }, [isShowPopupListBottle]);

  const handleClickCard = (data: CertProductDetails, isScanning: boolean) => {
    setShowModal(true);
    setTitle(() => {
      if (isScanning) {
        return `${t("areYouReadyToContinueScan", {
          certNumber: data.certNumber,
          lotId: data.lotId,
          certType: data.certType,
        })}`;
      }

      return `${t("areYouReadyToScan", {
        certNumber: data.certNumber,
        lotId: data.lotId,
        certType: data.certType,
      })}`;
    });
    setTextConfirm(() => {
      return `${t("scanByRange")}`;
    });

    setCurrentLot(data);
  };

  //handleScanByRange
  const handleScanByRange = async () => {
    if (!(await checkPermission())) {
      return;
    }
    currentScannedRef.current = [];
    setTypeScan(TypeScan.SCAN_BY_RANGE);
    setTitleScanInfoModal(`${t("titleScanRangeModal")}`);
    if (isGetLotError) {
      setShowErrorModal(true);
      setShowModal(false);
      return;
    }
    setIsRangeScan(true);
    setCurrentScanned?.([]);
    setShowModal(false);
    const { value } = await Preferences.get({
      key: ModalViewed.SCAN_RANGE_INFO,
    });
    if (value) {
      setOpenScan(true);
      await startScanning();
    } else {
      setShowScanInfoModal(true);
    }
  };

  const handleCancel = () => {
    setShowModal(false);
  };

  //handleScanByBottle
  const handleScanByBottle = async () => {
    if (!(await checkPermission())) {
      return;
    }
    if (isGetLotError) {
      setShowErrorModal(true);
      setShowModal(false);
      return;
    }
    setShowModal(false);
    setIsRangeScan(false);
    setTypeScan(TypeScan.SCAN_BY_BOTTLE);
    setTitleScanInfoModal(
      `${t("titleScanBottleModal", {
        certNumber: currentLot?.certNumber,
        lotId: currentLot?.lotId,
        certType: currentLot?.certType,
      })}`
    );
    const { value } = await Preferences.get({
      key: ModalViewed.SCAN_BOTTLES_INFO,
    });
    if (value) {
      setOpenScan(true);
      await startScanning();
    } else {
      setShowScanInfoModal(true);
    }
  };

  const handleStopScan = async () => {
    handleShowPopupCancel();
  };

  const handleConfirmPopupScanningView = async () => {
    if (confirmRescanLot || rangeScanningStatus.error) {
      setRangeScanningStatus({ error: false });
      setConfirmRescanLot(false);
      onResetRangeScan();
    } else if (unableToSaveStatus.error) {
      setUnableToSaveStatus({ error: false });
      await saveOrApproveBottles(false);
    } else if (confirmApproveRangeScan) {
      setConfirmApproveRangeScan(false);
      await saveOrApproveBottles(true);
    } else {
      await BarcodeScanner.resumeScanning();
      setShowModalError(false);
    }
  };

  const handleCancelScan = async () => {
    setShowPopupCancel(false);
    setOpenScan(false);
    setCurrentScanned?.([]);
    currentScannedRef.current = [];
    listRemoveRef.current = [];
    setCurrentLot(undefined);
    await stopScan();
  };

  const handleShowPopupCancel = () => {
    if (listRemoveRef && listRemoveRef.current.length > 0) {
      setShowPopupCancel(true);
      return;
    }

    if (currentScanned?.length) {
      const tmpScanned = currentScanned?.filter((item) => !item.certificateId);
      if (tmpScanned.length && tmpScanned.length > 0) {
        setShowPopupCancel(true);
        return;
      }
    }
    handleCancelScan();
  };

  const handleLeaveListBottle = async () => {
    setIsShowPopupListBottle(false);
    await BarcodeScanner.resumeScanning();
  };

  const updateListLot = async () => {
    try {
      if (certData && winery) {
        const certs = await BackendAPI.getCert(winery.wineryId);
        const listUpdatedData = certs.data.find(
          (cert) => cert.id === certData.id
        );
        setCertData(listUpdatedData);
      }
    } catch (err) {
      showToast(t("somethingWentWrongPleaseTryAgain"), ToastMessageType.ERROR);
    }
  };

  const handleDelete = async (bottleId: string) => {
    const dataAfterRemove =
      currentScanned?.filter((el) => el.id !== bottleId) || [];

    const removeItem = currentScanned?.find((el) => el.id === bottleId);
    if (removeItem) {
      listRemoveRef.current = [...listRemoveRef.current, removeItem];
    }
    setCurrentScanned?.(dataAfterRemove);
    currentScannedRef.current = dataAfterRemove;
  };

  const handleShowListBottle = async () => {
    setIsShowPopupListBottle(true);
    setListBottleType(ListBottleType.SCANNING);
  };

  const saveOrApproveBottles = async (approve: boolean) => {
    try {
      const lotId = currentLot?.lotId || "";
      const certId = currentLot?.certId || "";
      const certNumber = currentLot?.certNumber || "";
      const wineryId = winery?.wineryId || "";
      if (isRangeScan) {
        const data: ScanRange = {
          startRange: `${sortableScannedRange[0]?.id}`,
          endRange: `${sortableScannedRange[1]?.id}`,
          isSequentialNumber: false,
          finalise: approve,
        };
        await BackendAPI.saveScanRangeApi(wineryId, lotId, certId, data);
      } else {
        const addBottlesId =
          currentScanned
            ?.filter((item) => item.certificateId === null)
            .map(({ id }) => id) || [];
        const removeBottlesId = listRemoveRef.current
          .filter((item) => item.certificateId !== null)
          .map((i) => i.id);
        const data: DataSaveBottle = {
          add: addBottlesId,
          remove: removeBottlesId,
          finalise: approve,
        };
        await BackendAPI.saveAndContinue(wineryId, lotId, certId, data);
      }
      setIsShowPopupSave(false);
      const msg = approve ? "approveSuccess" : "saveSuccess";
      showToast(t(msg, { lotId, certNumber }), ToastMessageType.SUCCESS);
      setIsShowPopupListBottle(false);
      setOpenScan(false);
      listRemoveRef.current = [];
      setCurrentScanned?.([]);
      currentScannedRef.current = [];
      setCurrentLot(undefined);
      updateListLot();
    } catch (err: any) {
      if (isRangeScan && err?.response?.status === 409) {
        setRangeScanningStatus({ error: true, approve });
      } else if (isRangeScan) {
        setUnableToSaveStatus({ error: true, approve });
      } else {
        setIsShowPopupSave(false);
        showToast(
          t("somethingWentWrongPleaseTryAgain"),
          ToastMessageType.ERROR
        );
      }
    }
  };

  const handleReviewAndApprove = async () => {
    if (isRangeScan) {
      setConfirmApproveRangeScan(true);
    } else {
      setIsShowPopupSave(false);
      setIsShowPopupListBottle(true);
      setListBottleType(ListBottleType.APPROVE);
    }
  };

  const generateTitleAndSubTitle = (
    popupType: ListBottleType
  ): {
    title: "string";
    subTitle: "string";
  } => {
    switch (popupType) {
      case ListBottleType.SCANNING:
        return {
          title: t("bottleListTitle"),
          subTitle: t("bottleListSubTitle"),
        };
      case ListBottleType.APPROVE:
        return {
          title: t("bottleListTitleApprove"),
          subTitle: t("bottleListSubTitleApprove"),
        };
    }
  };

  const onResetRangeScan = async () => {
    setCurrentScanned?.([]);
    currentScannedRef.current = [];
    setIsShowPopupSave(false);
    await BarcodeScanner.resumeScanning();
  };

  const handleAcceptScanInfoModal = async () => {
    setShowScanInfoModal(false);

    const key =
      typeScan === TypeScan.SCAN_BY_RANGE
        ? ModalViewed.SCAN_RANGE_INFO
        : ModalViewed.SCAN_BOTTLES_INFO;
    await Preferences.set({
      key,
      value: "true",
    });
    await startScanning();
    setOpenScan(true);
  };
  return (
    <IonPage>
      {!openScan && (
        <IonHeader
          collapse="fade"
          className="ion-no-border"
          hidden={openScan}
        >
          <IonToolbar
            className="header"
            mode="ios"
          >
            <BackButton handleBack={() => history.goBack()} />
            <IonTitle
              className="title"
              data-testid="title"
            >
              {certData?.certificateNumber}
              <span className="sub-title">{certData?.certificateType}</span>
            </IonTitle>
          </IonToolbar>
        </IonHeader>
      )}
      <IonContent
        className={openScan ? "hide-content" : ""}
        fullscreen
      >
        <IonList className="list-container">
          <IonGrid
            className={["ion-no-padding", openScan ? "hide-content" : ""].join(
              " "
            )}
          >
            <IonRow>
              {certData?.lotEntries?.map(
                (e: CertificateProduct, index: number) => (
                  <IonCol key={index}>
                    <CertOrProductCard
                      type={
                        e.scanningStatus === LotStatus.APPROVED
                          ? QRType.COMPLETED
                          : QRType.ONE_LOT
                      }
                      certId={certData.id}
                      certNum={certData.certificateNumber}
                      certType={certData.certificateType}
                      lotId={e?.lotId}
                      isScanning={e.scanningStatus === LotStatus.SCANNING}
                      multiLotCert={true}
                      onClick={(
                        data: CertProductDetails,
                        isScanning: boolean
                      ) => {
                        if (e.scanningStatus === LotStatus.APPROVED) return;
                        handleClickCard(data, isScanning);
                      }}
                    />
                  </IonCol>
                )
              )}
            </IonRow>
          </IonGrid>
        </IonList>
        <ModalConfirm
          textConfirm={textConfirm}
          title={title}
          isOpen={showModal}
          onConfirm={handleScanByRange}
          onCancel={handleCancel}
          onScanByBottle={handleScanByBottle}
        />
      </IonContent>
      <ScanningView
        disableHeaderAction={!currentScanned?.length}
        listScannedAction={handleShowListBottle}
        totalScanned={currentScanned?.length}
        showHeader
        currentLot={currentLot}
        activeScan={openScan}
        scannedStatus={scannedStatus}
        saveAction={() => setIsShowPopupSave(true)}
        onExit={handleStopScan}
        isRangeScan={isRangeScan}
      >
        <ModalConfirm
          textConfirm={popupConfirmProps().okText}
          title={popupConfirmProps().title}
          onCancel={popupConfirmProps().cancelAction}
          isOpen={isPopupConfirmOpen}
          onConfirm={handleConfirmPopupScanningView}
        />
        <ModalConfirm
          textConfirm={t("understand")}
          title={titleScanInfoModal}
          isOpen={showScanInfoModal}
          onConfirm={handleAcceptScanInfoModal}
        />
        <PopupCancelScan
          onCancel={() => setShowPopupCancel(false)}
          textConfirm={t("CancelScan")}
          textCancel={t("NoCancelScan")}
          title={t("areYouCancelScan", {
            certNumber: currentLot?.certNumber,
            certType: currentLot?.certType,
            lotId: currentLot?.lotId,
          })}
          isOpen={showPopupCancel}
          onConfirm={handleCancelScan}
        />
      </ScanningView>
      {isShowPopupSave && (
        <PopupSave
          isOpen={isShowPopupSave}
          onLeave={() => setIsShowPopupSave(false)}
          onSave={() => saveOrApproveBottles(false)}
          onApprove={handleReviewAndApprove}
          onRangeScanReset={() => setConfirmRescanLot(true)}
          isRangeScan={isRangeScan}
          subTitle={`${t("rangeScanPopupReviewSubTitle", {
            range: `${sortableScannedRange[0]?.sequentialNumber}-${
              sortableScannedRange[sortableScannedRange.length - 1]
                ?.sequentialNumber
            }`,
            certNumber: currentLot?.certNumber,
            certType: currentLot?.certType,
            lotId: currentLot?.lotId,
          })}`}
        />
      )}
      {isShowPopupListBottle && (
        <PopupListBottle
          title={generateTitleAndSubTitle(listBottleType).title}
          subTitle={generateTitleAndSubTitle(listBottleType).subTitle}
          data={{
            bottles: currentScanned || [],
            lotId: currentLot?.lotId || "",
            certId: currentLot?.certId || "",
          }}
          isOpen={isShowPopupListBottle}
          onLeave={handleLeaveListBottle}
          onDelete={handleDelete}
          onApprove={() => saveOrApproveBottles(true)}
          type={listBottleType}
        />
      )}
      <ModalConfirm
        textConfirm={t("confirmed")}
        title={t("listBottlesUnavailable", { lotId: `${currentLot?.lotId}` })}
        isOpen={showErrorModal}
        onConfirm={() => setShowErrorModal(false)}
      />
    </IonPage>
  );
};

export { CertProductView };

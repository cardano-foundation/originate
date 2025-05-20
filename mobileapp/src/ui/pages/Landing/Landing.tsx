import {
  BarcodeScanner,
  SupportedFormat,
} from "@capacitor-community/barcode-scanner";
import { Preferences } from "@capacitor/preferences";
import {
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonPage,
  IonRow,
  IonText,
  IonToolbar,
} from "@ionic/react";
import { AxiosResponse } from "axios";
import { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ToastMessageContext } from "../../../context";
import { i18n } from "../../../i18n";
import { BackendAPI } from "../../../services";
import { SCANTRUST_SCAN_URL, convertName } from "../../../utils";
import {
  CertProductDetails,
  CertificateCardView,
  ModalConfirm,
  PopupInfoBottle,
  PopupListBottle,
  PopupSave,
  PopupUser,
  ScanButton,
  ScanningView,
  Segments,
} from "../../components";
import { PopupCancelScan } from "../../components/PopupCancelScan";
import {
  CertsByCategory,
  Certificate,
  DataSaveBottle,
  CertificateProduct,
  ScanRange,
  BottleInfo,
} from "../../common/responses";
import {
  BottleType,
  CertificateType,
  LanguageType,
  ListBottleType,
  ModalViewed,
  ToastMessageType,
  TypePreferences,
  TypeScan,
} from "../../common/types";
import { useScanner } from "../../hooks/useScanner";
import "./style.scss";
export const parseJwt = async () => {
  const langLocalPreference = (
    await Preferences.get({ key: TypePreferences.LANGUAGE })
  ).value;
  const token = (await Preferences.get({ key: TypePreferences.ID_TOKEN }))
    .value;
  if (token && !langLocalPreference) {
    const base64Url = token.split(".")[1] || "";
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split("")
        .map(function (c) {
          return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join("")
    );

    const locale = JSON.parse(jsonPayload)?.locale || "";
    const lang = locale ? locale : LanguageType.English;
    if (lang) {
      await Preferences.set({ key: "language", value: lang });
    }
    i18n.changeLanguage(lang);
  }

  return null;
};
const Landing = () => {
  const { t } = useTranslation();
  const { showToast } = useContext(ToastMessageContext);
  const [activeScan, setActiveScan] = useState(false);
  const [showPopupInfo, setShowPopupInfo] = useState<boolean>(false);
  const [showModal, setShowModal] = useState<boolean>(false);
  const [title, setTitle] = useState<string>("");
  const [textConfirm, setTextConfirm] = useState<string>(`${t("yesLetScan")}`);
  const {
    stopScan,
    checkPermission,
    setCurrentLot,
    startScanning,
    setShowModalError,
    setCurrentScanned,
    handleModalError,
    modalErrorConfirmText,
    errorMessage,
    showModalError,
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
  const [typeBottle, setTypeBottle] = useState("");
  const [showPopupUser, setShowPopupUser] = useState<boolean>(false);
  const [isShowPopupListBottle, setIsShowPopupListBottle] =
    useState<boolean>(false);
  const [isShowPopupSave, setIsShowPopupSave] = useState<boolean>(false);
  const [isCheckBottle, setIsCheckBottle] = useState<boolean>(true);
  const [showPopupCancel, setShowPopupCancel] = useState<boolean>(false);
  const history = useHistory();
  const username = winery?.wineryName || "";
  const [listBottleType, setListBottleType] = useState<ListBottleType>(
    ListBottleType.SCANNING
  );

  const [segment, setSegment] = useState<
    CertificateType.REQUIRED | CertificateType.COMPLETED
  >(CertificateType.REQUIRED);

  const [cert, setCert] = useState<CertsByCategory>({
    listRequired: [],
    listCompleted: [],
  });
  const [infoBottle, setInfoBottle] = useState<BottleInfo>();
  const [showErrorModal, setShowErrorModal] = useState<boolean>(false);
  const [showScanInfoModal, setShowScanInfoModal] = useState<boolean>(false);
  const [titleScanInfoModal, setTitleScanInfoModal] = useState<string>("");
  const [typeScan, setTypeScan] = useState<string>("");

  const removePrefix = (inputString: string | undefined, prefix: string) => {
    if (inputString?.startsWith(prefix)) {
      return inputString?.substring(prefix.length);
    }
    setTypeBottle(BottleType.NOT_SCAN);
    return undefined;
  };
  const [refresh, setRefresh] = useState<boolean>(false);

  useEffect(() => {
    if (
      currentScanned &&
      currentScanned.length >= 2 &&
      activeScan &&
      isRangeScan
    ) {
      setIsShowPopupSave(true);
    }
  }, [currentScanned, activeScan, isRangeScan]);

  const getInfoBottle = async (wineryId: string, bottleId: string) => {
    try {
      const response = await BackendAPI.getInfoBottle(wineryId, bottleId);
      if (response.data) {
        await BarcodeScanner.pauseScanning();
        setInfoBottle(response.data);
        setShowPopupInfo(true);
        setTypeBottle(response.data.scanningStatus);
      }
    } catch (err: any) {
      if (handleModalError)
        handleModalError("scanNotRecognized", "tryScanAgain");
    }
  };

  useEffect(() => {
    if (winery?.wineryId) {
      getCerts(winery.wineryId);
    }
  }, [winery?.wineryId, history?.location?.pathname, refresh]);

  useEffect(() => {
    const pauseScan = async () => {
      try {
        await BarcodeScanner.pauseScanning();
      } catch (err) {
        //
      }
    };
    if (isShowPopupListBottle) {
      pauseScan();
    }
  }, [isShowPopupListBottle]);

  const handleStopScan = () => {
    if (isCheckBottle) {
      setActiveScan(false);
      stopScan();
    } else {
      handleShowPopupCancel();
    }
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

  const handleCloseBottleInfoPopup = () => {
    setShowPopupInfo(false);
    stopScan();
    setActiveScan(false);
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
    setActiveScan(true);
    await startScanning();
  };

  useEffect(() => {
    parseJwt();
  }, []);

  const handleCancelScan = async () => {
    setShowPopupCancel(false);
    setActiveScan(false);
    setCurrentScanned?.([]);
    currentScannedRef.current = [];
    listRemoveRef.current = [];
    setCurrentLot(undefined);
    await stopScan();
  };
  const handleLeaveListBottle = async () => {
    setIsShowPopupListBottle(false);
    await BarcodeScanner.resumeScanning();
  };

  const handleCheckBottleScan = async () => {
    setShowModalError(false);
    setIsCheckBottle(true);
    setShowPopupInfo(false);
    if (!(await checkPermission())) {
      return;
    }
    setActiveScan(true);
    await BarcodeScanner.hideBackground();
    await BarcodeScanner.startScanning(
      {
        targetedFormats: [SupportedFormat.QR_CODE],
      },
      async (result) => {
        const bottleId = removePrefix(result?.content, `${SCANTRUST_SCAN_URL}`);
        if (!bottleId) {
          return setTypeBottle(BottleType.NOT_SCAN);
        }
        if (result.hasContent) {
          if (winery?.wineryId && bottleId) {
            await getInfoBottle(winery?.wineryId, bottleId);
          }
        }
      }
    );
  };

  const handleClickAvatar = () => {
    setShowPopupUser(true);
  };

  const getCerts = async (wineryId: string) => {
    try {
      const response: AxiosResponse = await BackendAPI.getCert(wineryId);
      const scanningCompleted = response?.data?.filter((el: Certificate) =>
        el.lotEntries.every(
          (e: CertificateProduct) => e.scanningStatus === "APPROVED"
        )
      );

      const scanningRequired = response?.data?.filter(
        (el: Certificate) =>
          !scanningCompleted.some((e: Certificate) => el.id === e.id)
      );

      setCert({
        listCompleted: scanningCompleted,
        listRequired: scanningRequired,
      });
    } catch (err) {
      showToast(t("somethingWentWrongPleaseTryAgain"), ToastMessageType.ERROR);
    }
  };

  //handleScanByRange
  const handleScanByRange = async () => {
    if (!(await checkPermission())) {
      return;
    }
    currentScannedRef.current = [];
    if (isGetLotError) {
      setShowModal(false);
      setShowErrorModal(true);
      return;
    }
    setShowModal(false);
    setIsRangeScan(true);
    setCurrentScanned?.([]);
    setTypeScan(TypeScan.SCAN_BY_RANGE);
    setTitleScanInfoModal(`${t("titleScanRangeModal")}`);
    const { value } = await Preferences.get({
      key: ModalViewed.SCAN_RANGE_INFO,
    });
    if (value) {
      setActiveScan(true);
      await startScanning();
    } else {
      setShowScanInfoModal(true);
    }
  };

  //handleScanByBottle
  const handleScanByBottle = async () => {
    if (!(await checkPermission())) {
      return;
    }
    if (isGetLotError) {
      setShowModal(false);
      setShowErrorModal(true);
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
      setActiveScan(true);
      await startScanning();
    } else {
      setShowScanInfoModal(true);
    }
  };

  const handleClickSingleLot = (
    id: string,
    certProduct: CertProductDetails,
    isScanning: boolean
  ) => {
    setIsCheckBottle(false);
    setShowModal(true);
    setTitle(() => {
      if (isScanning) {
        return `${t("areYouReadyToContinueScan", {
          certNumber: certProduct.certNumber,
          lotId: certProduct.lotId,
          certType: certProduct.certType,
        })}`;
      }
      return `${t("areYouReadyToScan", {
        certNumber: certProduct.certNumber,
        lotId: certProduct.lotId,
        certType: certProduct.certType,
      })}`;
    });
    setTextConfirm(() => {
      return `${t("scanByRange")}`;
    });
    setCurrentLot({
      lotId: certProduct.lotId,
      certId: id,
      certType: certProduct.certType,
      certNumber: certProduct.certNumber,
    });
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

  const handleShowListBottle = () => {
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
      setActiveScan(false);
      setIsShowPopupListBottle(false);
      listRemoveRef.current = [];
      setCurrentScanned?.([]);
      currentScannedRef.current = [];
      setCurrentLot(undefined);
      setRefresh(!refresh);
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

  const onResumeScanBottle = async () => {
    setShowPopupInfo(false);
    setShowModalError(false);
    await BarcodeScanner.resumeScanning();
  };

  const onResetRangeScan = async () => {
    setCurrentScanned?.([]);
    currentScannedRef.current = [];
    setIsShowPopupSave(false);
    await BarcodeScanner.resumeScanning();
  };

  return (
    <IonPage>
      {!activeScan && (
        <IonHeader
          className="ion-no-border"
          hidden={activeScan}
        >
          <IonToolbar className="px-20 header">
            <IonText slot="start">
              <h2 className="title">{t("certificate")}</h2>
            </IonText>
            <div
              slot="end"
              className="avatar"
              data-testid="avatar-user"
              onClick={handleClickAvatar}
            >
              {convertName(username)}
            </div>
          </IonToolbar>
        </IonHeader>
      )}
      <IonContent
        fullscreen
        data-testid="landing-content"
        className={["main-content", activeScan ? "hideBg" : ""].join(" ")}
      >
        <IonGrid
          className={["ion-no-padding", activeScan ? "hideBg" : ""].join(" ")}
        >
          <IonRow className="px-20 wrapS">
            <IonCol>
              <Segments
                value={segment}
                setValue={setSegment}
              />
            </IonCol>
          </IonRow>
          <IonRow className="wrap-list">
            <IonCol>
              <CertificateCardView
                typeScan={segment}
                cert={cert}
                handleClickSingleLot={handleClickSingleLot}
              />
            </IonCol>
          </IonRow>
          <IonRow className="wrap-button">
            <IonCol>
              <ScanButton onClick={handleCheckBottleScan} />
            </IonCol>
          </IonRow>
        </IonGrid>
        <PopupUser
          isOpen={showPopupUser}
          onLeave={() => setShowPopupUser(false)}
          username={username}
        />
      </IonContent>
      {isCheckBottle ? (
        <ScanningView
          activeScan={activeScan}
          onExit={handleStopScan}
        >
          <ModalConfirm
            textConfirm={t(modalErrorConfirmText)}
            title={t(errorMessage)}
            isOpen={showModalError}
            onConfirm={onResumeScanBottle}
          />
          {showPopupInfo && (
            <PopupInfoBottle
              isOpen={showPopupInfo}
              onLeave={handleCloseBottleInfoPopup}
              onScan={onResumeScanBottle}
              bottleData={infoBottle}
            />
          )}
        </ScanningView>
      ) : (
        <ScanningView
          totalScanned={currentScanned?.length}
          showHeader
          currentLot={currentLot}
          activeScan={activeScan}
          scannedStatus={scannedStatus}
          disableHeaderAction={!currentScanned?.length}
          listScannedAction={handleShowListBottle}
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
        </ScanningView>
      )}
      <ModalConfirm
        textConfirm={textConfirm}
        title={title}
        isOpen={showModal}
        onConfirm={handleScanByRange}
        onCancel={() => setShowModal(false)}
        onScanByBottle={handleScanByBottle}
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

export { Landing };

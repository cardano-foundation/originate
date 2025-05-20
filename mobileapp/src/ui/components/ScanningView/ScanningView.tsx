import { IonButton, IonChip, IonThumbnail } from "@ionic/react";
import { ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { backIcon, boxScan, check, listBullet, save } from "../../assets/icons";
import "./style.scss";
import { CertProductDetails } from "../CertOrProductCard";

type ScanningViewProps = {
  activeScan: boolean;
  onExit: () => void;
  listScannedAction?: () => void;
  saveAction?: () => void;
  showHeader?: boolean;
  disableHeaderAction?: boolean;
  disableBottomAction?: boolean;
  currentLot?: CertProductDetails;
  totalScanned?: number;
  children?: ReactNode;
  scannedStatus?: boolean;
  isRangeScan?: boolean;
};

const ScanningView = ({
  activeScan,
  totalScanned = 0,
  currentLot,
  showHeader = false,
  disableHeaderAction = true,
  disableBottomAction = false,
  children,
  scannedStatus = false,
  isRangeScan,
  onExit,
  listScannedAction,
  saveAction,
}: ScanningViewProps) => {
  const { t } = useTranslation();

  return (
    <div
      className="container-scan"
      hidden={!activeScan}
      data-testid="scan-view"
    >
      <div className={["scanning-header", showHeader || "btn-back"].join(" ")}>
        <IonButton
          slot="start"
          data-testid="btn-back-scanning-view"
          onClick={onExit}
        >
          <IonThumbnail>
            <img
              alt="backIcon"
              src={backIcon}
            />
          </IonThumbnail>
        </IonButton>
        {showHeader && (
          <>
            <div className="text-box">
              <p>{currentLot?.certNumber}</p>
              <p>{currentLot?.lotId}</p>
              <p>{currentLot?.certType}</p>
            </div>
            <div className="wrap-button-list">
              {!isRangeScan && (
                <IonButton
                  slot="start"
                  disabled={disableHeaderAction}
                  onClick={listScannedAction}
                  className={disableHeaderAction ? "btn-disabled" : ""}
                  data-testid="button-list"
                >
                  <IonThumbnail>
                    <img
                      alt="listBullet"
                      src={listBullet}
                    />
                  </IonThumbnail>
                </IonButton>
              )}
            </div>
          </>
        )}
      </div>
      <div className="scan-box">
        <img
          alt="boxScan"
          src={boxScan}
        />
      </div>
      <div className="scanning-bottom">
        {showHeader ? (
          <>
            {scannedStatus && (
              <div className="wrap-scan-status">
                <div className="scan-status">{t("scannedStatus")}</div>
                <IonChip>
                  <IonThumbnail>
                    <img
                      alt="check"
                      src={check}
                    />
                  </IonThumbnail>
                </IonChip>
              </div>
            )}

            {isRangeScan ? (
              <div className="count-box">
                {totalScanned === 0 ? t("firstRangeScan") : t("lastRangeScan")}
              </div>
            ) : (
              <>
                <IonButton
                  slot="start"
                  disabled={disableBottomAction}
                  className={disableBottomAction ? "btn-disabled" : ""}
                  onClick={saveAction}
                  data-testid="button-save-scan"
                >
                  <IonThumbnail>
                    <img
                      alt="save"
                      src={save}
                    />
                  </IonThumbnail>
                </IonButton>
              </>
            )}
          </>
        ) : (
          <div className="text-box">{t("checkBottleStatus")}</div>
        )}
      </div>
      {children}
    </div>
  );
};

export { ScanningView };

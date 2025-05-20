import {
  IonButton,
  IonButtons,
  IonHeader,
  IonModal,
  IonTitle,
  IonToolbar,
} from "@ionic/react";
import { useTranslation } from "react-i18next";
import { BottleType } from "../../common/types";
import { dropDown } from "../../assets/icons";
import "./style.scss";
import { BottleInfo } from "../../common/responses";

type PopupInfoBottleProps = {
  isOpen: boolean;
  bottleData?: BottleInfo;
  onLeave: () => void;
  onScan: () => void;
};

const PopupInfoBottle = ({
  isOpen,
  onLeave,
  onScan,
  bottleData,
}: PopupInfoBottleProps) => {
  const { t } = useTranslation();
  const RenderTypeMessage = (props: { bottle?: BottleInfo }) => {
    const { bottle } = props;
    const type = bottle?.scanningStatus;
    switch (type) {
      case BottleType.NOT_SCAN:
        return (
          <div className="type-bottle not-scan">
            <p>{t("notassociatedyet")}</p>
          </div>
        );
      case BottleType.SCANNED:
        return (
          <div className="type-bottle scanned">
            <p>
              {t("savebottlenotapproved", {
                certNum: bottle?.certNumber,
              })}
            </p>
          </div>
        );
      case BottleType.SCANNED_APPROVED:
        return (
          <div className="type-bottle approved">
            <p>
              {t("savebottleapproved", {
                certNum: bottle?.certNumber,
              })}
            </p>
          </div>
        );
      default:
        return (
          <div className="type-bottle not-scan">
            <p>{t("notassociatedyet")}</p>
          </div>
        );
    }
  };

  return (
    <IonModal
      isOpen={isOpen}
      className="info-bottle"
      data-testid="info-bottle"
      onDidDismiss={onScan}
    >
      <IonHeader mode="ios">
        <IonToolbar className="title-toolbar">
          <IonTitle className="title">{t("bottleInformation")}</IonTitle>
          <IonButtons slot="start">
            <IonButton
              data-testid="info-bottle-closeicon"
              onClick={onScan}
            >
              <img
                alt="closeButton"
                src={dropDown}
                className="icon-drop"
              />
            </IonButton>
          </IonButtons>
        </IonToolbar>
      </IonHeader>
      <div className="ion-padding">
        <RenderTypeMessage bottle={bottleData} />
        {(bottleData?.scanningStatus === BottleType.SCANNED ||
          bottleData?.scanningStatus === BottleType.SCANNED_APPROVED) && (
          <>
            <div className="info border-bottom">
              <p className="key">{t("infoCertificate")}</p>
              <p className="value">{bottleData?.certNumber}</p>
            </div>
            <div className="info border-bottom">
              <p className="key">{t("infoCertificateType")}</p>
              <p className="value">{bottleData?.certType}</p>
            </div>
          </>
        )}

        <div className="info border-bottom">
          <p className="key">{t("lot")}</p>
          <p className="value">{bottleData?.lotId}</p>
        </div>
        <div className="info ">
          <p className="key">{t("infoBottle")}</p>
          <p className="value">{bottleData?.sequentialNumber}</p>
        </div>
        <IonButton
          shape="round"
          expand="full"
          onClick={onScan}
          data-testid="info-scan-button"
        >
          {t("scanAnotherCode")}
        </IonButton>
        <IonButton
          shape="round"
          fill="outline"
          class="button-outline"
          expand="full"
          data-testid="info-leave-button"
          onClick={onLeave}
        >
          {t("leave")}
        </IonButton>
      </div>
    </IonModal>
  );
};

export { PopupInfoBottle };

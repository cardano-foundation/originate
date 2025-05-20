import { IonButton, IonLabel, IonThumbnail } from "@ionic/react";
import { scan } from "../../assets/icons";
import "./style.scss";
import { useTranslation } from "react-i18next";

type IProps = {
  onClick: () => void;
};

const ScanButton = ({ onClick }: IProps) => {
  const { t } = useTranslation();
  return (
    <div
      className="custom-button"
      data-testid="box-btn-scan"
    >
      <IonButton
        className="ion-no-margin"
        shape="round"
        onClick={onClick}
        data-testid="button-scan"
      >
        <IonThumbnail>
          <img
            alt="crop"
            src={scan}
          />
        </IonThumbnail>
      </IonButton>
      <IonLabel>{t("checkBottle")}</IonLabel>
    </div>
  );
};

export { ScanButton };

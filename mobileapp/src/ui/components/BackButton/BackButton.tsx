import { IonButton, IonThumbnail } from "@ionic/react";

import { backIcon } from "../../assets/icons";
import "./style.scss";

interface BackButtonProps {
  handleBack: () => void;
}

const BackButton = ({ handleBack }: BackButtonProps) => {
  return (
    <div className="button-back">
      <IonButton
        className="ion-no-margin"
        shape="round"
        onClick={handleBack}
        data-testid="back-btn"
      >
        <IonThumbnail>
          <img
            alt="backIcon"
            src={backIcon}
          />
        </IonThumbnail>
      </IonButton>
    </div>
  );
};

export { BackButton };

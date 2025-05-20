import { IonButton, IonLabel, IonModal } from "@ionic/react";
import "./style.scss";

type IProps = {
  title: string;
  textConfirm: string;
  textCancel: string;
  isOpen: boolean;
  onConfirm: () => void;
  onCancel?: () => void;
};

const PopupCancelScan = ({
  textConfirm,
  title,
  isOpen,
  textCancel,
  onCancel,
  onConfirm,
}: IProps) => {
  return (
    <IonModal
      isOpen={isOpen}
      className="popup-cancel"
      backdropDismiss={false}
      data-testid="popup-cancel-scan"
    >
      <IonLabel className="popup-title">{title}</IonLabel>
      <IonButton
        shape="round"
        onClick={onConfirm}
        data-testid="confirm-button"
      >
        {textConfirm}
      </IonButton>
      {onCancel && (
        <IonButton
          shape="round"
          fill="outline"
          class="button-outline"
          onClick={onCancel}
          data-testid="cancel-button"
        >
          {textCancel}
        </IonButton>
      )}
    </IonModal>
  );
};

export { PopupCancelScan };

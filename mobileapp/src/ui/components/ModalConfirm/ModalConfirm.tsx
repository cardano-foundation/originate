import { IonButton, IonLabel, IonModal } from "@ionic/react";
import { useTranslation } from "react-i18next";
import "./style.scss";

type IProps = {
  title: string;
  textConfirm: string;
  isOpen: boolean;
  onConfirm: () => void;
  onCancel?: () => void;
  onScanByBottle?: () => void;
};

const ModalConfirm = ({
  textConfirm,
  title,
  isOpen,
  onCancel,
  onConfirm,
  onScanByBottle,
}: IProps) => {
  const { t } = useTranslation();

  return (
    <IonModal
      isOpen={isOpen}
      className="modal-confirm"
      backdropDismiss={false}
      data-testid="modal-confirm-scan"
    >
      <IonLabel className="modal-title">{title}</IonLabel>

      <IonButton
        shape="round"
        onClick={onConfirm}
        data-testid="confirm-button"
      >
        {textConfirm}
      </IonButton>
      {onScanByBottle && (
        <IonButton
          shape="round"
          class="button-outline"
          onClick={onScanByBottle}
          data-testid="scan-by-bottle-button"
        >
          {t("scanByBottle")}
        </IonButton>
      )}
      {onCancel && (
        <IonButton
          shape="round"
          fill="outline"
          class="button-outline"
          onClick={onCancel}
          data-testid="cancel-button"
        >
          {t("cancel")}
        </IonButton>
      )}
    </IonModal>
  );
};

export { ModalConfirm };

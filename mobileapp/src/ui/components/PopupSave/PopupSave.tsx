import { IonButton, IonModal } from "@ionic/react";
import { useTranslation } from "react-i18next";

import "./style.scss";

type TProps = {
  isOpen?: boolean;
  onLeave?: () => void;
  onSave?: () => void;
  onApprove?: () => void;
  onRangeScanReset?: () => void;
  isRangeScan?: boolean;
  subTitle?: string;
};

export const PopupSave = ({
  isOpen,
  isRangeScan,
  subTitle,
  onLeave,
  onSave,
  onApprove,
  onRangeScanReset,
}: TProps) => {
  const { t } = useTranslation();
  return (
    <IonModal
      isOpen={isOpen}
      initialBreakpoint={isRangeScan ? 1 : 0.25}
      breakpoints={[0, isRangeScan ? 1 : 0.25]}
      onDidDismiss={onLeave}
      canDismiss={!isRangeScan}
      className={`modal-save ${isRangeScan ? "modal-save-range-scan" : ""}`}
      data-testid="popup-save-and-review"
    >
      <div className="group-button">
        {isRangeScan && (
          <>
            <div className="title">{t("bottleListTitleApprove")}</div>
            <div className="subtitle">{subTitle}</div>
            <IonButton
              shape="round"
              expand="full"
              className="button-review"
              onClick={onApprove}
              data-testid="button-approve-range-scan"
            >
              {t("approveLot")}
            </IonButton>
          </>
        )}
        <IonButton
          shape="round"
          expand="full"
          fill="outline"
          className="button-save-later"
          onClick={onSave}
          data-testid="button-save-later"
        >
          {t("saveAndContinueLater")}
        </IonButton>
        {isRangeScan ? (
          <IonButton
            shape="round"
            expand="full"
            fill="outline"
            className="button-save-later"
            onClick={onRangeScanReset}
            data-testid="button-rescan-range"
          >
            {t("rescanRange")}
          </IonButton>
        ) : (
          <IonButton
            shape="round"
            expand="full"
            className="button-review"
            onClick={onApprove}
            data-testid="button-review-and-approve"
          >
            {t("reviewAndApprove")}
          </IonButton>
        )}
      </div>
    </IonModal>
  );
};

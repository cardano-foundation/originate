import { FC } from "react";
import { IonLabel, IonSegment, IonSegmentButton } from "@ionic/react";
import "./style.scss";
import { useTranslation } from "react-i18next";
import { CertificateType } from "../../common/types";

type TProps = {
  value: string;
  // eslint-disable-next-line no-unused-vars
  setValue: (value: any) => void;
};
const Segments: FC<TProps> = ({ value, setValue }) => {
  const { t } = useTranslation();
  return (
    <div className="custom-segment">
      <IonSegment
        onIonChange={(event) => setValue(event.detail.value)}
        value={value}
      >
        <IonSegmentButton
          data-testid="segment-scanning-required"
          value={CertificateType.REQUIRED}
        >
          <IonLabel>
            <strong>{t("required")}</strong>
          </IonLabel>
        </IonSegmentButton>
        <IonSegmentButton
          data-testid="segment-scanning-completed"
          value={CertificateType.COMPLETED}
        >
          <IonLabel>
            <strong>{t("completed")}</strong>
          </IonLabel>
        </IonSegmentButton>
      </IonSegment>
    </div>
  );
};

export { Segments };

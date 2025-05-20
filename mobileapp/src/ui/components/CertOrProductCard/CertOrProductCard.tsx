import {
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonChip,
  IonText,
  IonThumbnail,
} from "@ionic/react";
import { useTranslation } from "react-i18next";
import { check, qrcode, qrcode2 } from "../../assets/icons";
import { QRType } from "../../common/types";
import "./style.scss";

type CertOrProductCardProps = {
  type: QRType;
  certId: string;
  certNum: string;
  certType: string;
  numLots?: number;
  lotId: string;
  isScanning: boolean;
  onClick: (data: any, isScanning: boolean) => void;
  multiLotCert?: boolean;
};

export type CertProductDetails = {
  certId: string;
  lotId: string;
  certType: string;
  certNumber: string;
};

const getIcon = (type: string) => {
  switch (type) {
    case QRType.COMPLETED:
      return { src: check, alt: "completed" };
    case QRType.ONE_LOT:
      return { src: qrcode2, alt: "one-lot" };
    default:
      return { src: qrcode, alt: "many-lots" };
  }
};

const CertOrProductCard = ({
  type,
  certId,
  certNum,
  certType,
  numLots,
  onClick,
  isScanning,
  multiLotCert,
  lotId,
}: CertOrProductCardProps) => {
  const data: CertProductDetails = { certId, lotId, certNumber: certNum, certType };
  const icon = getIcon(type);
  const { t } = useTranslation();

  return (
    <div className="card-item">
      <IonCard
        button={true}
        onClick={() => onClick(data, isScanning)}
        data-testid="card-item"
      >
        <IonCardHeader>
          <div className="card-icon">
            <IonChip className={icon.alt}>
              <IonThumbnail>
                <img
                  data-testid="qr-icon"
                  alt={icon.alt}
                  src={icon.src}
                />
              </IonThumbnail>
            </IonChip>
            {isScanning && (
              <div
                data-testid="dot-active"
                className="dot-active"
              />
            )}
          </div>

          <IonCardTitle>{multiLotCert ? lotId : certNum}</IonCardTitle>
        </IonCardHeader>
        <IonCardContent>
          <IonText>
            {multiLotCert ? (
              <p>{certNum}</p>
            ) : (
              <p>
                {numLots && numLots > 1
                  ? t("numOfLots", { number: `${numLots}` })
                  : t("numOfLot", { number: `${numLots}` })}
              </p>
            )}
          </IonText>
          <IonText className="cert-type">{certType}</IonText>
        </IonCardContent>
      </IonCard>
    </div>
  );
};

export { CertOrProductCard };

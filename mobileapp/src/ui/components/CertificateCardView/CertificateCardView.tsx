import { IonCol, IonGrid, IonList, IonRow } from "@ionic/react";
import { useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { CertsByCategory, Certificate } from "../../common/responses";
import { CertificateType, LotStatus, QRType } from "../../common/types";
import { CertProductDetails, CertOrProductCard } from "../CertOrProductCard";
import "./style.scss";

const isEveryLotStatus = (el: Certificate, status: LotStatus) =>
  el.lotEntries.every((item) => item.scanningStatus === status);

type CertificateCardViewProps = {
  cert: CertsByCategory;
  typeScan: CertificateType.COMPLETED | CertificateType.REQUIRED;
  handleClickSingleLot?: (
    id: string,
    data: CertProductDetails,
    isScanning: boolean
  ) => void;
};

const CertificateCardView = ({
  cert,
  typeScan,
  handleClickSingleLot,
}: CertificateCardViewProps) => {
  const [litsData, setListData] = useState(cert.listRequired || []);
  const history = useHistory();
  const { t } = useTranslation();

  useEffect(() => {
    if (typeScan === CertificateType.REQUIRED) {
      setListData(cert.listRequired);
    } else {
      setListData(cert.listCompleted);
    }
  }, [typeScan, cert]);

  const renderType = (typeScan: string, countLot: number) => {
    if (typeScan === CertificateType.COMPLETED) {
      return QRType.COMPLETED;
    } else if (countLot === 1) {
      return QRType.ONE_LOT;
    } else {
      return QRType.MANY_LOTS;
    }
  };

  const handlePushUrl = (el: Certificate) => {
    history.push(`/detail-lot/${el.id}`, el);
  };

  return (
    <IonList className="list-card-item">
      <IonGrid className="ion-no-padding">
        <IonRow>
          {litsData.length > 0 ? (
            litsData.map((el: Certificate) => {
              const isSingleLot = el.lotEntries.length === 1;
              const allApproved = isEveryLotStatus(el, LotStatus.APPROVED);
              const allNotStarted = isEveryLotStatus(el, LotStatus.NOT_STARTED);
              const checkIsScanning =
                (isSingleLot &&
                  el.lotEntries[0].scanningStatus === LotStatus.SCANNING) ||
                (!isSingleLot && !allApproved && !allNotStarted);
              return (
                <IonCol key={el.id}>
                  <CertOrProductCard
                    type={renderType(typeScan, el.lotEntries.length)}
                    certId={el.id}
                    certNum={el.certificateNumber}
                    certType={el.certificateType}
                    numLots={el.lotEntries.length}
                    isScanning={checkIsScanning}
                    lotId={el.lotEntries[0].lotId}
                    onClick={(
                      data: CertProductDetails,
                      isScanning: boolean
                    ) => {
                      if (typeScan === CertificateType.COMPLETED && isSingleLot) {
                        return;
                      }

                      if (isSingleLot && handleClickSingleLot) {
                        handleClickSingleLot(el.id, data, isScanning);
                        return;
                      }

                      handlePushUrl(el);
                    }}
                  />
                </IonCol>
              );
            })
          ) : (
            <div style={{ width: "100%", textAlign: "center" }}>
              {typeScan === CertificateType.REQUIRED
                ? t("noCertificateAvailable")
                : t("noCertificateFully")}
            </div>
          )}
        </IonRow>
      </IonGrid>
    </IonList>
  );
};

export { CertificateCardView };

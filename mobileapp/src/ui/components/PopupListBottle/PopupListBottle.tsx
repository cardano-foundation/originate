import {
  IonButton,
  IonButtons,
  IonHeader,
  IonModal,
  IonTitle,
  IonToolbar,
} from "@ionic/react";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { BottleListResponse } from "../../common/responses";
import { TitleConfirm } from "../../common/types";
import { ActionBottle, ListBottleType } from "../../common/types";
import { ModalConfirm } from "../ModalConfirm";
import { BottleItem } from "./BottleItem";
import { dropDown } from "../../assets/icons";
import "./style.scss";

interface PopupListBottleProps {
  title: string;
  subTitle: string;
  isOpen: boolean;
  onLeave: () => void;
  onDelete: (bottleId: string) => void;
  onApprove: () => void;
  data: BottleListResponse;
  type?: ListBottleType;
  disableAction?: boolean;
}

const PopupListBottle = ({
  isOpen,
  onLeave,
  data,
  onDelete,
  onApprove,
  title,
  subTitle,
  type = ListBottleType.SCANNING,
  disableAction = false,
}: PopupListBottleProps) => {
  const { t } = useTranslation();
  const [showModalConfirm, setShowModalConfirm] = useState<boolean>(false);
  const [bottleIdDelete, setBottleIdDelete] = useState<string>("");
  const [titleConfirm, setTitleConfirm] = useState<TitleConfirm>({
    title: "",
    cancelText: "",
    okText: "",
  });
  const [action, setAction] = useState<ActionBottle>(
    ActionBottle.DELETE_BOTTLE
  );

  const handleDelete = (bottleId: string) => {
    setAction(ActionBottle.DELETE_BOTTLE);
    setShowModalConfirm(true);
    setTitleConfirm({
      title: t("confirmDeleteQr").toString(),
      okText: t("yesDelete").toString(),
    });
    setBottleIdDelete(bottleId);
  };

  const handleConfirm = () => {
    setShowModalConfirm(false);
    if (action === ActionBottle.APPROVE) {
      onApprove();
      return;
    }
    if (action === ActionBottle.DELETE_BOTTLE) {
      onDelete(bottleIdDelete);
    }
  };

  const handleConfirmApprove = () => {
    setAction(ActionBottle.APPROVE);
    setTitleConfirm((prev) => {
      return {
        ...prev,
        title: t("titleApproveLot").toString(),
        okText: t("yesApprove").toString(),
      };
    });
    setShowModalConfirm(true);
  };

  return (
    <IonModal
      isOpen={isOpen}
      className="bottle-list-modal"
      onDidDismiss={onLeave}
      data-testid="modal-list"
    >
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          height: "100%",
        }}
      >
        <IonHeader mode="ios">
          <IonToolbar className="title-toolbar">
            <IonTitle className="title">{title}</IonTitle>
            <IonButtons slot="start">
              <IonButton
                data-testid="bottle-list-closeicon"
                onClick={onLeave}
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
        <div className="content">
          <div className="list-item-container">
            <div className="sub-title">{subTitle}</div>
            {data.bottles.map(({ id, sequentialNumber }, index) => {
              return (
                <BottleItem
                  key={index}
                  bottleId={id}
                  sequentialNumber={sequentialNumber}
                  onDelete={handleDelete}
                />
              );
            })}
          </div>
        </div>
        {type !== ListBottleType.SCANNING && (
          <div className="approve-container">
            <IonButton
              disabled={disableAction}
              shape="round"
              expand="full"
              onClick={handleConfirmApprove}
              data-testid="button-approve-lot"
            >
              {t("approveLot")}
            </IonButton>
          </div>
        )}
      </div>
      <ModalConfirm
        textConfirm={titleConfirm?.okText || ""}
        title={titleConfirm?.title || ""}
        isOpen={showModalConfirm}
        onConfirm={handleConfirm}
        onCancel={() => setShowModalConfirm(false)}
      />
    </IonModal>
  );
};

export { PopupListBottle };

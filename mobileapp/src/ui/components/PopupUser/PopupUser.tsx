import {
  IonModal,
  IonButton,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
} from "@ionic/react";
import { useTranslation } from "react-i18next";
import "./style.scss";
import { Browser } from "@capacitor/browser";
import { Preferences } from "@capacitor/preferences";
import { SelectLanguage } from "../SelectLanguage";
import { useAuth } from "../../../services/AuthContext";
import { chevronRight, dropDown } from "../../assets/icons";

type TProps = {
  isOpen?: boolean;
  onLeave?: () => void;
  username: string;
};

const PopupUser = ({ isOpen, onLeave, username }: TProps) => {
  const { t } = useTranslation();
  const { logout } = useAuth();
  const openTermAndCondition = async () => {
    const lang = (await Preferences.get({ key: "language" })).value || "";
    await Browser.open({
      url: lang
        ? `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/terms/mobile?kc_locale=${lang}`
        : `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/terms/mobile`,
    });
  };

  const openPrivacyPolicy = async () => {
    const lang = (await Preferences.get({ key: "language" })).value || "";
    await Browser.open({
      url: lang
        ? `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/privacy?kc_locale=${lang}`
        : `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/privacy`,
    });
  };

  return (
    <IonModal
      isOpen={isOpen}
      onDidDismiss={onLeave}
      className="popup-container"
      data-testid="popup-user-setting"
    >
      <IonHeader mode="ios">
        <IonToolbar className="title-toolbar">
          <IonTitle className="title">{username}</IonTitle>
          <IonButtons slot="start">
            <IonButton
              data-testid="popup-user-closeicon"
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
      <div className="ion-padding">
        <SelectLanguage />

        <div
          onClick={openTermAndCondition}
          className="box"
          data-testid="button-select"
        >
          <div className="select">
            <p className="selected">{t("termAndCondition")}</p>
            <img
              alt="chevronRight"
              src={chevronRight}
              className="icon-chevronright"
            />
          </div>
        </div>

        <div
          onClick={openPrivacyPolicy}
          className="box"
          data-testid="button-select"
        >
          <div className="select">
            <p className="selected">{t("privacyPolicy")}</p>
            <img
              alt="chevronRight"
              src={chevronRight}
              className="icon-chevronright"
            />
          </div>
        </div>

        <IonButton
          shape="round"
          expand="full"
          className="btn-sign-out"
          data-testid="btn-sign-out"
          onClick={logout}
        >
          {t("signOut")}
        </IonButton>
      </div>
    </IonModal>
  );
};

export { PopupUser };

import { IonButton, IonImg, IonPage } from "@ionic/react";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../../services/AuthContext";
import cardanoLogo from "../../assets/images/cardano-logo.svg";
import "./style.scss";
import { SelectLanguageButton } from "../../components";

const Login = () => {
  const { t } = useTranslation();
  const { login, lang, setLang } = useAuth();
  return (
    <IonPage className="container">
      <div className="select-language">
        <SelectLanguageButton
          lang={lang}
          setLang={setLang}
        />
      </div>
      <IonImg
        alt="cardano-logo"
        src={cardanoLogo}
      />
      <IonButton onClick={login}>{t("login")}</IonButton>
    </IonPage>
  );
};

export { Login };

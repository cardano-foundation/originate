import {
  IonButton,
  IonCheckbox,
  IonContent,
  IonLabel,
  IonListHeader,
  IonPage,
  IonTitle,
} from "@ionic/react";
import "./style.scss";
import { useContext, useState } from "react";
import { Trans, useTranslation } from "react-i18next";

import { Browser } from "@capacitor/browser";
import { instance, useAuth } from "../../../services/AuthContext";
import { SelectLanguageButton } from "../../components";
import { BackendAPI } from "../../../services";
import { ToastMessageType } from "../../common/types";
import { ToastMessageContext } from "../../../context";

const TermsAndConditions = () => {
  const { t } = useTranslation();
  const { showToast } = useContext(ToastMessageContext);
  const { lang, setLang } = useAuth();
  const [isChecked, setIsChecked] = useState<boolean>(false);
  const { logout } = useAuth();

  const agreeTerm = async () => {
    try {
      const response = await BackendAPI.agreeTerm();

      if (response) {
        await instance.refreshToken();
      }
    } catch (err: any) {
      showToast(t("somethingWentWrongPleaseTryAgain"), ToastMessageType.ERROR);
    }
  };

  const openPrivacyPolicy = async () => {
    await Browser.open({
      url: lang
        ? `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/privacy?kc_locale=${lang}`
        : `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/privacy`,
    });
  };

  const handleButtonContinue = () => {
    if (isChecked) {
      agreeTerm();
    }
  };

  return (
    <IonPage className="page">
      <IonListHeader className="header">
        <IonLabel
          className="title"
          data-testid="title"
        >
          <IonTitle
            className="title"
            size="large"
          >
            {t("termAndCondition")}
          </IonTitle>
        </IonLabel>
        <div className="button-language">
          <SelectLanguageButton
            lang={lang}
            setLang={setLang}
          />
        </div>
      </IonListHeader>

      <IonContent
        className="wrap-content"
        data-testid="content"
      >
        <div className="content">
          <p className="header-text-terms">
            <strong>{t("termsTopText")}</strong>
          </p>

          <p className="number-term">1. {t("terms1")}</p>
          <ol type="a">
            <li>{<Trans i18nKey="terms1.1" />}</li>
            <li>{<Trans i18nKey="terms1.2" />}</li>
            <li>{<Trans i18nKey="terms1.3" />}</li>
            <li>{<Trans i18nKey="terms1.4" />}</li>
            <li>
              {
                <Trans
                  i18nKey="terms1.5"
                  components={[
                    <span
                      data-testid="privacy-policy-link"
                      className="txt-link-privacy"
                      onClick={openPrivacyPolicy}
                    />,
                  ]}
                />
              }
            </li>
            <li>{<Trans i18nKey="terms1.6" />}</li>
            <li>{<Trans i18nKey="terms1.7" />}</li>
          </ol>

          <p className="number-term">2. {t("terms2")}</p>
          <ol type="a">
            <li>{<Trans i18nKey="terms2.1" />}</li>
            <li>{<Trans i18nKey="terms2.2" />}</li>
            <li>{<Trans i18nKey="terms2.3" />}</li>
            <li>{<Trans i18nKey="terms2.4" />}</li>
            <ul>
              <li>{t("terms2.4.1")}</li>
              <li>{t("terms2.4.2")}</li>
              <li>{t("terms2.4.3")}</li>
            </ul>
            <li>{<Trans i18nKey="terms2.5" />}</li>
          </ol>

          <p className="number-term">3. {t("terms3")}</p>
          <ol type="a">
            <li>{<Trans i18nKey="terms3.1" />}</li>
            <li>{<Trans i18nKey="terms3.2" />}</li>
            <li>{<Trans i18nKey="terms3.3" />}</li>
            <li>{<Trans i18nKey="terms3.4" />}</li>
          </ol>

          <p className="number-term">4. {t("terms4")}</p>
          <p>{t("terms4Header")}</p>
          <ol type="a">
            <li>{<Trans i18nKey="terms4.1" />}</li>
            <li>{<Trans i18nKey="terms4.2" />}</li>
            <li>{<Trans i18nKey="terms4.3" />}</li>
            <li>{<Trans i18nKey="terms4.4" />}</li>
          </ol>

          <p className="number-term">5. {t("terms5")}</p>
          <ol type="a">
            <li>{t("terms5.1")}</li>
            <li>{t("terms5.2")}</li>
          </ol>

          <p className="number-term">6. {t("terms6")}</p>
          <ol type="a">
            <li>{<Trans i18nKey="terms6.1" />}</li>
            <li>{<Trans i18nKey="terms6.2" />}</li>
            <li>{<Trans i18nKey="terms6.3" />}</li>
            <li>{<Trans i18nKey="terms6.4" />}</li>
            <li>{<Trans i18nKey="terms6.5" />}</li>
            <li>{<Trans i18nKey="terms6.6" />}</li>
            <li>{<Trans i18nKey="terms6.7" />}</li>
            <li>{<Trans i18nKey="terms6.8" />}</li>
            <li>
              {<Trans i18nKey="terms6.9" />}
              <br />
              <br />
              Cardano Foundation,
              <br />
              Dammstrasse 16,
              <br />
              6300, Zug,
              <br />
              Switzerland
              <br />
              Attn: Legal
            </li>
          </ol>
        </div>
      </IonContent>

      <div className={"footer"}>
        <div className="wrap-checkbox">
          <IonCheckbox
            slot="start"
            onIonChange={() => setIsChecked(!isChecked)}
            data-testid="checkbox"
          />
          <IonLabel className="text-agree">{t("agreeTermCondition")}</IonLabel>
        </div>

        {isChecked ? (
          <IonButton
            onClick={handleButtonContinue}
            shape="round"
            data-testid="confirm-button"
            className="button-continue"
          >
            {t("continue")}
          </IonButton>
        ) : (
          <IonButton
            disabled
            shape="round"
            fill="outline"
            data-testid="confirm-button"
            className="button-continue-outline"
          >
            {t("continue")}
          </IonButton>
        )}

        <IonButton
          fill="clear"
          className="button-signout"
          onClick={logout}
        >
          {t("signOutTerm")}
        </IonButton>
      </div>
    </IonPage>
  );
};
export { TermsAndConditions };

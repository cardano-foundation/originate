import {
  IonGrid,
  IonModal,
  IonList,
  IonLabel,
  IonCheckbox,
} from "@ionic/react";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import "./style.scss";
import { Preferences } from "@capacitor/preferences";
import { flagEnglish, dropDown, flagGeorgian } from "../../assets/icons";
import { LanguageType, TypePreferences } from "../../common/types";
import { i18n } from "../../../i18n";

const SelectLanguage = () => {
  const { t } = useTranslation();
  const [showModal, setShowModal] = useState(false);
  const [lang, setLang] = useState("");

  const options = [
    {
      flag: flagEnglish,
      label: t("english"),
      value: LanguageType.English,
    },
    {
      flag: flagGeorgian,
      label: t("georgian"),
      value: LanguageType.Georgian,
    },
  ];
  const onChangeLang = async (selectedLang: string) => {
    await Preferences.set({ key: "language", value: selectedLang });
    i18n?.changeLanguage(selectedLang);
    setShowModal(false);
    setLang(selectedLang);
  };

  useEffect(() => {
    const fetchData = async () => {
      const langLocalPreference = await Preferences.get({
        key: TypePreferences.LANGUAGE,
      });
      const langLocalValue = langLocalPreference?.value || "ka";
      setLang(langLocalValue);
    };

    fetchData();
  }, []);

  return (
    <IonGrid>
      <div
        onClick={() => setShowModal(true)}
        className="box-language"
        data-testid="select-language"
      >
        <p className="placeholder">{t("language")}</p>
        <div className="select">
          <div className="language">
            <img
              alt="flagEnglish"
              src={lang === LanguageType.English ? flagEnglish : flagGeorgian}
            />
            <p className="selected">
              {lang === LanguageType.English ? t("english") : t("georgian")}
            </p>
          </div>
          <img
            alt="dropDown"
            src={dropDown}
            className="icon-drop"
          />
        </div>
      </div>
      <IonModal
        className="modal"
        isOpen={showModal}
        trigger="open-custom-dialog"
        data-testid="select-language-modal"
        onDidDismiss={() => setShowModal(false)}
        animated={false}
      >
        <IonList
          className="list-item"
          lines="none"
        >
          {options?.map((el) => (
            <div
              key={el.value}
              className="item-lang"
              onClick={() => onChangeLang(el.value)}
              data-testid={`item-change-language-${el.value}`}
            >
              <div className="flag">
                <img
                  alt={el.label}
                  src={el.flag}
                />
                <IonLabel className="label">{el.label}</IonLabel>
              </div>
              <IonCheckbox
                checked={lang === el.value}
                color="success"
                className="checkbox"
              />
            </div>
          ))}
        </IonList>
      </IonModal>
    </IonGrid>
  );
};

export { SelectLanguage };

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
import { flagEnglish, flagGeorgian, up, down } from "../../assets/icons";
import { LanguageType, TypePreferences } from "../../common/types";
import { Preferences } from "@capacitor/preferences";

const SelectLanguageButton = ({ lang, setLang }: any) => {
  const { t } = useTranslation();
  const [showModal, setShowModal] = useState(false);
  const [langLocal, setLangLocal] = useState("");
  useEffect(() => {
    const fetchData = async () => {
      const langLocalPreference = await Preferences.get({
        key: TypePreferences.LANGUAGE,
      });
      const langLocalValue = langLocalPreference?.value;
      if (langLocalValue) {
        setLangLocal(langLocalValue);
      }
    };

    fetchData();
  }, [langLocal, lang]);

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

  const onChangeLang = async (lang: string) => {
    await Preferences.set({ key: "language", value: lang });
    setLang(lang);
  };

  return (
    <IonGrid>
      <div
        onClick={() => setShowModal(true)}
        className="box-select-language"
        data-testid="select-language-button"
      >
        <div className="select">
          <div className="language">
            <img
              alt="flagEnglish"
              src={
                langLocal
                  ? langLocal === LanguageType.English
                    ? flagEnglish
                    : flagGeorgian
                  : lang === LanguageType.English
                  ? flagEnglish
                  : flagGeorgian
              }
            />
          </div>
          {showModal ? (
            <img
              alt="dropDown"
              src={up}
              className="icon-up"
            />
          ) : (
            <img
              alt="dropDown"
              src={down}
              className="icon-drop"
            />
          )}
        </div>
      </div>
      <IonModal
        className="modal-select"
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
              data-testid="item-change-language"
            >
              <div className="flag">
                <img
                  alt={el.label}
                  src={el.flag}
                />
                <IonLabel className="label">{el.label}</IonLabel>
              </div>
              <IonCheckbox
                checked={langLocal ? langLocal === el.value : lang === el.value}
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

export { SelectLanguageButton };

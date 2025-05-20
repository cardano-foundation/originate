import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import { Preferences } from "@capacitor/preferences";
import enTranslation from "./ui/locales/en.json";
import kaTranslation from "./ui/locales/ka.json";
import { TypePreferences } from "./ui/common/types";

const initI18n = async () => {
  const languagePreference = await Preferences.get({
    key: TypePreferences.LANGUAGE,
  });
  const language = languagePreference?.value || "ka";

  i18n.use(initReactI18next).init({
    resources: {
      en: { translation: enTranslation },
      ka: { translation: kaTranslation },
    },
    lng: language,
    fallbackLng: "ka",
    interpolation: {
      escapeValue: false,
    },
  });
};

initI18n(); // Call the initialization function

export { i18n };

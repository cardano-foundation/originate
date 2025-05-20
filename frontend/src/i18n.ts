import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import enTranslation from "./ui/locales/en.json";
import kaTranslation from "./ui/locales/ka.json";
import { LanguageType } from "./ui/components/SelectLanguage/types";

const language = window.localStorage.getItem("language");

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: enTranslation },
    ka: { translation: kaTranslation },
  },
  lng:
    language === LanguageType.English ||
    (!language && navigator.language.startsWith(LanguageType.English))
      ? LanguageType.English
      : LanguageType.Georgian,
  fallbackLng: LanguageType.Georgian,
  interpolation: {
    escapeValue: false,
  },
});

export { i18n };

import { Preferences } from "@capacitor/preferences";
import { ROLE_SYSTEM } from "./constant";
import { TypePreferences } from "../ui/common/types";

// get language preference
export const languagePreference = async () => {
  const languagePreference = await Preferences.get({
    key: TypePreferences.LANGUAGE,
  });
  if (languagePreference && languagePreference.value) {
    return languagePreference.value;
  }
  return null;
};

// get idToken preference
export const getIdToken = async () => {
  const token = await Preferences.get({ key: TypePreferences.ID_TOKEN });
  if (token && token.value) {
    return token.value;
  }
  return null;
};

export const parseJwt = (jwtString: string) => {
  if (jwtString) {
    const base64Url = jwtString.split(".")[1] || "";
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split("")
        .map(function (c) {
          return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join("")
    );

    return JSON.parse(jsonPayload);
  }
  return null;
};

export const filterUnknownRoles = (arrayRoles: string[]) => {
  return arrayRoles.filter((role) => {
    return Object.values(ROLE_SYSTEM).includes(role);
  });
};

export const sleep = (ms: number) => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

export const convertName = (userName: string) => {
  if (!userName) return null;
  const textConvert = userName.split(" ");
  if (textConvert.length > 1) {
    return (
      textConvert[0].substring(0, 1) + textConvert[1].substring(0, 1)
    ).toUpperCase();
  }
  return userName?.substring(0, 1).toUpperCase();
};

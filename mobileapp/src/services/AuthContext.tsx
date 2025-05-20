/* eslint-disable @typescript-eslint/no-empty-function */
import { App } from "@capacitor/app";
import { Device } from "@capacitor/device";
import { Preferences } from "@capacitor/preferences";
import { isPlatform, useIonViewDidLeave } from "@ionic/react";
import { LocalStorageBackend } from "@openid/appauth";
import { AuthActions, AuthService } from "ionic-appauth";
import { CapacitorBrowser } from "ionic-appauth/lib/capacitor";
import {
  ReactNode,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Subscription } from "rxjs";
import { filter, switchMap, take } from "rxjs/operators";
import { ToastMessageContext } from "../context";
import { i18n } from "../i18n";
import {
  LanguageType,
  ToastMessageType,
  TypePreferences,
} from "../ui/common/types";
import { filterUnknownRoles, languagePreference, parseJwt } from "../utils";
import {
  KEYCLOAK_CLIENT_ID,
  KEYCLOAK_REDIRECT_URI,
  KEYCLOAK_SERVER,
  ROLE_SYSTEM,
} from "../utils/constant";
import { AuthContextProps, TValidToken } from "./AuthContext.types";
import { CapacitorRequestor } from "./capacitorRequestor";

export const instance = new AuthService(
  new CapacitorBrowser(),
  new LocalStorageBackend(),
  new CapacitorRequestor()
);

instance.authConfig = {
  client_id: `${KEYCLOAK_CLIENT_ID}`,
  server_host: `${KEYCLOAK_SERVER}`,
  redirect_url: `${KEYCLOAK_REDIRECT_URI}`,
  end_session_redirect_url: `${KEYCLOAK_REDIRECT_URI}`,
  scopes: "openid",
  pkce: true,
};

const initialAuthContext: AuthContextProps = {
  isAuthenticated: false,
  validToken: undefined,
  login: () => {},
  logout: () => {},
  lang: "",
  setLang: () => {},
  appTerm: false,
};

export const AuthContext = createContext<AuthContextProps>(initialAuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [appTerm, setAppTerm] = useState(false);
  const [authService, setAuthService] = useState<AuthService | undefined>();
  const [validToken, setValidToken] = useState<TValidToken | undefined>();
  const { showToast } = useContext(ToastMessageContext);
  const { t } = useTranslation();
  const [user, setUser] = useState();
  const [lang, setLang] = useState<string | null>("ka");
  const subs: Subscription[] = [];
  const login = async () => {
    if (authService) {
      await authService.signIn();
    }
  };

  const logout = async () => {
    try {
      if (authService) {
        await authService.signOut();
        await authService.revokeTokens();
        authService.endSessionCallback();
      }
    } catch (error) {
      showToast(t("signOutFailed"), ToastMessageType.ERROR);
    }
  };

  useEffect(() => {
    if (lang) {
      i18n.changeLanguage(lang);
    }
  }, [lang]);

  useEffect(() => {
    const setupInitialLanguage = async () => {
      const language = await languagePreference();
      if (language) {
        setLang(language);
      } else {
        try {
          const deviceLanguage = (await Device.getLanguageCode()).value;
          // @TODO post-pilot - foconnor: Ideally wouldn't store this now so change in device lang would be reflected
          // but currently have issues with Keycloak language detection so right now this is easiest for pilot.
          const langToSet = deviceLanguage.startsWith(LanguageType.English)
            ? LanguageType.English
            : LanguageType.Georgian;
          await Preferences.set({
            key: TypePreferences.LANGUAGE,
            value: langToSet,
          });
          setLang(langToSet);
        } catch (error) {}
      }
    };

    setupInitialLanguage();
  }, []);

  useEffect(() => {
    if (authService) {
      const queryParams = new URLSearchParams(location.search);
      if (
        queryParams.has("state") &&
        queryParams.has("session_state") &&
        queryParams.has("code")
      ) {
        authService.authorizationCallback(window.location.href);
      }
      authService.loadUserInfo();
    }
  }, [authService]);

  useEffect(() => {
    if (lang) {
      instance.authConfig = {
        client_id: `${KEYCLOAK_CLIENT_ID}`,
        server_host: `${KEYCLOAK_SERVER}`,
        redirect_url: `${KEYCLOAK_REDIRECT_URI}&app_locales=${lang}`,
        end_session_redirect_url: `${KEYCLOAK_REDIRECT_URI}`,
        scopes: "openid",
        pkce: true,
      };
    }
  }, [lang]);

  useEffect(() => {
    const setup = async () => {
      if (isPlatform("capacitor")) {
        App.addListener("appUrlOpen", (data: any) => {
          if (data.url.indexOf(instance.authConfig.redirect_url) === 0) {
            instance.authorizationCallback(data.url);
          } else {
            instance.endSessionCallback();
          }
        });
      }

      subs.push(
        instance.events$.subscribe(async (action) => {
          if (
            action.action === AuthActions.LoadTokenFromStorageSuccess ||
            action.action === AuthActions.SignInSuccess ||
            action.action === AuthActions.RefreshSuccess
          ) {
            setValidToken({
              accessToken: action.tokenResponse?.accessToken || "",
              expiresIn: action.tokenResponse?.expiresIn || 0,
              idToken: action.tokenResponse?.idToken || "",
              issuedAt: action.tokenResponse?.issuedAt || 0,
              refreshToken: action.tokenResponse?.refreshToken || "",
              scope: action.tokenResponse?.scope || "",
              tokenType: action.tokenResponse?.tokenType || "",
            });
            await Preferences.set({
              key: TypePreferences.ID_TOKEN,
              value: action?.tokenResponse?.idToken || "",
            });
            const parsed = parseJwt(action.tokenResponse?.accessToken || "");
            setAppTerm(parsed.app_terms);
            const roles = parsed?.realm_access?.roles;
            const filteredRoles = filterUnknownRoles(roles);
            if (
              filteredRoles.length === 1 &&
              filteredRoles.includes(ROLE_SYSTEM.WINERY)
            ) {
              setIsAuthenticated(true);
            } else {
              setIsAuthenticated(false);
              await instance.revokeTokens();
              showToast(t("messageLoginErr"), ToastMessageType.ERROR);
            }
          } else if (
            action.action === AuthActions.SignInFailed ||
            action.action === AuthActions.SignOutSuccess ||
            action.action === AuthActions.RefreshFailed
          ) {
            if (action.action === AuthActions.SignInFailed) {
              showToast(t("loginErrorMsg"), ToastMessageType.ERROR);
            }
            setIsAuthenticated(false);
          } else if (action.action === AuthActions.SignOutFailed) {
            showToast(t("signOutFailed"), ToastMessageType.ERROR);
          }
        }),
        instance.user$.subscribe((user) => {
          setUser(user);
        })
      );

      await instance.init();
      instance.initComplete$
        .pipe(
          filter((complete) => complete),
          switchMap(() => instance.isAuthenticated$),
          take(1)
        )
        .subscribe((isAuthenticated) => {
          setIsAuthenticated(isAuthenticated);
        });

      setAuthService(instance);
    };
    setup();
  }, []);

  useIonViewDidLeave(() => {
    subs.forEach((sub) => sub.unsubscribe());
  });
  const authContextValue: AuthContextProps = {
    isAuthenticated,
    validToken,
    login,
    logout,
    lang,
    setLang,
    appTerm,
  };

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

/* eslint-disable @typescript-eslint/no-empty-function */
import {
  ReactNode,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import {
  AuthContextProps,
  TUserNameInfo,
  TValidToken,
} from "./AuthContext.types";
import { keyCloakClient } from "../services/Instances/KeyCloakServices";
import { PKCE_METHOD, ROLE_SYSTEM } from "../ui/constants";
import ROUTES from "../routes/contants/Routes";
import { filterUnknownRoles } from "../ui/pages/Login/convertRolesLogin";

export const initialAuthContext: AuthContextProps = {
  isAuthenticated: false,
  validToken: undefined,
  isLoadingKeyCloak: true,
  userNameInfo: undefined,
  isShowTermsConditions: false,
  setValidToken: () => {},
  setIsAuthenticated: () => {},
  setUserNameInfo: () => {},
  setIsShowTermsConditions: () => {},
};

export const AuthContext = createContext<AuthContextProps>(initialAuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isLoadingKeyCloak, setIsLoadingKeyCloak] = useState<boolean>(true);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [validToken, setValidToken] = useState<TValidToken | undefined>(
    undefined
  );
  const [userNameInfo, setUserNameInfo] = useState<TUserNameInfo | undefined>(
    undefined
  );
  const [isShowTermsConditions, setIsShowTermsConditions] =
    useState<boolean>(false);

  useEffect(() => {
    if (window.location.pathname !== ROUTES.LOGIN) {
      keyCloakClient
        .init({
          checkLoginIframe: true,
          pkceMethod: PKCE_METHOD,
          onLoad: "check-sso",
        })
        .then((value) => {
          setIsLoadingKeyCloak(false);
          const data = filterUnknownRoles(
            keyCloakClient?.realmAccess?.roles ?? []
          );
          if (data.length === 1 && data[0] !== ROLE_SYSTEM.NWA) {
            setUserNameInfo({
              userName: keyCloakClient?.tokenParsed?.full_name
                ? keyCloakClient?.tokenParsed?.full_name
                : keyCloakClient?.tokenParsed?.email,
              isEmail: !keyCloakClient?.tokenParsed?.full_name,
            });
            setIsShowTermsConditions(!!keyCloakClient?.tokenParsed?.web_terms);
            setIsAuthenticated(value);
            setValidToken({
              accessToken: keyCloakClient?.token || "",
              role: data[0] || "",
              refreshToken: keyCloakClient?.refreshToken || "",
            });
          }
        })
        .catch(() => {
          setIsLoadingKeyCloak(false);
        });
    }
  }, []);

  const authContextValue: AuthContextProps = {
    isAuthenticated,
    isLoadingKeyCloak,
    validToken,
    userNameInfo,
    isShowTermsConditions,
    setValidToken,
    setIsAuthenticated,
    setUserNameInfo,
    setIsShowTermsConditions,
  };

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

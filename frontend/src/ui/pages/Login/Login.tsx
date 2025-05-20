import { useEffect, useState } from "react";
import Container from "@mui/material/Container";
import { Button } from "@mui/material";
import Box from "@mui/material/Box";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAuth } from "../../../contexts/AuthContext";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import ROUTES from "../../../routes/contants/Routes";
import { CustomAlert } from "../../layout/CustomAlert";
import { ALERT_TYPE } from "../../layout/CustomAlert/types";
import { SelectLanguage } from "../../components/SelectLanguage";
import { LanguageType } from "../../components/SelectLanguage/types";
import { i18n } from "../../../i18n";
import { UNAUTHORIZED, PKCE_METHOD, ROLE_SYSTEM } from "../../constants";
import { filterUnknownRoles } from "./convertRolesLogin";
import { Logo } from "../../assets/icons/Logo";
import "./style.scss";

/* global process */

function Login() {
  const history = useHistory();
  const { t } = useTranslation();
  const queryParams = new URLSearchParams(history?.location?.search);
  const logoutCode = queryParams.get("logoutCode");

  const {
    setIsAuthenticated,
    setValidToken,
    setUserNameInfo,
    setIsShowTermsConditions,
  } = useAuth();
  const [messageAlert, setMessageAlert] = useState({
    type: "",
    message: "",
  });

  useEffect(() => {
    keyCloakClient
      .init({
        checkLoginIframe: false,
        pkceMethod: PKCE_METHOD,
      })
      .then(() => {
        if (keyCloakClient.token) {
          if (!keyCloakClient.realmAccess) {
            setMessageAlert({
              type: ALERT_TYPE.error,
              message: t("failRole"),
            });
            return;
          } else {
            const data = filterUnknownRoles(keyCloakClient.realmAccess.roles);
            if (data.length === 0 || data.length > 1) {
              setMessageAlert({
                type: ALERT_TYPE.error,
                message: t("failRole"),
              });
              return;
            } else {
              if (data[0] === ROLE_SYSTEM.NWA) {
                setMessageAlert({
                  type: ALERT_TYPE.error,
                  message: t("failRole"),
                });
                return;
              } else {
                if (keyCloakClient?.tokenParsed?.locale) {
                  window.localStorage.setItem(
                    "language",
                    keyCloakClient.tokenParsed.locale
                  );
                  i18n.changeLanguage(keyCloakClient.tokenParsed.locale);
                }
                setIsShowTermsConditions(
                  !!keyCloakClient?.tokenParsed?.web_terms
                );
                setIsAuthenticated(true);
                setUserNameInfo({
                  userName: keyCloakClient?.tokenParsed?.full_name
                    ? keyCloakClient?.tokenParsed?.full_name
                    : keyCloakClient?.tokenParsed?.email,
                  isEmail: !keyCloakClient?.tokenParsed?.full_name,
                });
                setValidToken({
                  accessToken: keyCloakClient?.token || "",
                  role: data[0] || "",
                  refreshToken: keyCloakClient?.refreshToken || "",
                });
                return history.push({
                  pathname: ROUTES.HOME,
                  state: { role: data[0] },
                });
              }
            }
          }
        }
      })
      .catch(() => {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("failKeycloak"),
        });
      });
  }, [history, setIsAuthenticated, setValidToken]);

  useEffect(() => {
    if (logoutCode === UNAUTHORIZED) {
      setMessageAlert({
        type: ALERT_TYPE.error,
        message: t("authorize"),
      });
    }
  }, [history?.location?.search]);

  const handleLogin = () => {
    keyCloakClient
      .init({
        redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}`,
      })
      .then(() => {
        let setLanguage = window.localStorage.getItem("language");
        if (setLanguage) {
          setLanguage =
            setLanguage === LanguageType.English
              ? LanguageType.English
              : LanguageType.Georgian;
          keyCloakClient.login({ locale: setLanguage });
        } else {
          keyCloakClient.login();
        }
      })
      .catch(() => {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("failKeycloak"),
        });
        keyCloakClient.logout();
      });
  };

  const handleClearMessage = () => {
    setMessageAlert({
      type: "",
      message: "",
    });
  };

  return (
    <Container maxWidth="sm">
      <CustomAlert
        router={ROUTES.LOGIN}
        isOpen={!!messageAlert.message}
        severity={
          messageAlert.type === ALERT_TYPE.success ? "success" : "error"
        }
        message={messageAlert.message}
        onClearMessage={handleClearMessage}
      />
      <Box className="select-language-login">
        <SelectLanguage isOnlySelect={true} />
      </Box>
      <Box
        sx={{
          height: "calc(100vh - 104px)",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Logo />
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            marginTop: "90px",
          }}
        >
          <Button
            variant="contained"
            sx={{
              width: "350px",
              padding: "15px 25px",
              borderRadius: "30px",
              backgroundColor: "#1D439B",
              color: "#FFFFFF",
              fontSize: "16px",
              textTransform: "unset",
            }}
            onClick={handleLogin}
            data-testid="btn-login"
          >
            {t("login")}
          </Button>
        </Box>
      </Box>
    </Container>
  );
}

export { Login };

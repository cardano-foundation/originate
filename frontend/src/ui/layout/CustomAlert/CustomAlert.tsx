import React, { useEffect } from "react";
import { Alert, AlertColor } from "@mui/material";
import "./style.scss";
import { ALERT_TYPE } from "./types";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import ROUTES from "../../../routes/contants/Routes";

type Props = {
  severity: AlertColor;
  router?: string;
  message: string;
  isOpen: boolean;
  isfailLots?: boolean;
  autoCloseDelay?: number;
  onClearMessage?: () => void;
};

const CustomAlert: React.FC<React.PropsWithChildren<Props>> = ({
  severity,
  message,
  router,
  autoCloseDelay = 5000,
  isOpen,
  isfailLots,
  onClearMessage,
}) => {
  useEffect(() => {
    if (isOpen) {
      const timer = setTimeout(() => {
        onClearMessage && onClearMessage();
        if (router === ROUTES.LOGIN && keyCloakClient?.token) {
          keyCloakClient.logout();
        }
      }, autoCloseDelay);
      return () => {
        clearTimeout(timer);
      };
    }
  }, [autoCloseDelay, isOpen, onClearMessage, router]);

  if (!isOpen) {
    return null;
  }

  return (
    <Alert
      severity={severity}
      className={`custom-alert ${
        severity === ALERT_TYPE.success ? "alert-success" : "alert-error"
      }`}
      sx={{
        top: isfailLots ? "130px" : "",
      }}
    >
      {message}
    </Alert>
  );
};

export { CustomAlert };

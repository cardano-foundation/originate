import { setupIonicReact, IonApp, isPlatform } from "@ionic/react";
import React, { useEffect } from "react";
import { StatusBar, Style } from "@capacitor/status-bar";
import Routes from "../routes";
import "./styles/ionic.scss";
import "./style.scss";
import "../theme/variables.css";
import { ToastMessageProvider } from "../context";
import { AuthProvider } from "../services/AuthContext";

setupIonicReact();

const App = () => {
  useEffect(() => {
    if (!isPlatform("desktop")) {
      setStatusBarStyleDark();
    }
  }, []);

  const setStatusBarStyleDark = async () => {
    await StatusBar.setStyle({ style: Style.Light });
  };

  return (
    <ToastMessageProvider>
      <AuthProvider>
        <React.StrictMode>
          <IonApp>
            <Routes />
          </IonApp>
        </React.StrictMode>
      </AuthProvider>
    </ToastMessageProvider>
  );
};

export default App;

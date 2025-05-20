import React from "react";
import { BrowserRouter as Router } from "react-router-dom";
import Routes from "../routes";
import { AuthProvider } from "../contexts/AuthContext";
import { AxiosInterceptor } from "../services/AxiosInterceptor";
import "./style.scss";
import { Footer } from "./pages/Footer";

function App() {
  return (
    <AuthProvider>
      <React.StrictMode>
        <Router>
          <AxiosInterceptor>
            <Routes />
            <Footer />
          </AxiosInterceptor>
        </Router>
      </React.StrictMode>
    </AuthProvider>
  );
}

export { App };

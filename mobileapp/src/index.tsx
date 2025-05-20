import ReactDOM from "react-dom/client";
import { I18nextProvider } from "react-i18next";

import App from "./ui/App";
import reportWebVitals from "./reportWebVitals";
import { i18n } from "./i18n";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <I18nextProvider i18n={i18n}>
    <App />
  </I18nextProvider>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

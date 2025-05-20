import { IonReactRouter } from "@ionic/react-router";
import { IonRouterOutlet } from "@ionic/react";
import { Route } from "react-router-dom";
import { Landing } from "../ui/pages/Landing";
import { CertProductView } from "../ui/pages/CertProductView";
import { Login } from "../ui/pages/Login";
import { useAuth } from "../services/AuthContext";
import { TermsAndConditions } from "../ui/pages/TermsAndConditions";

const Routes = () => {
  const { isAuthenticated, appTerm } = useAuth();

  if (!isAuthenticated) {
    return <Login />;
  }

  if (!appTerm) {
    return <TermsAndConditions />;
  }

  return (
    <IonReactRouter>
      <IonRouterOutlet>
        <Route
          path="/"
          exact
          component={Landing}
        />
        <Route
          path="/detail-lot/:id"
          exact
          component={CertProductView}
        />
      </IonRouterOutlet>
    </IonReactRouter>
  );
};

export default Routes;

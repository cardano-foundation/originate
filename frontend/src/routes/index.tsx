import { Switch, Route } from "react-router-dom";
import ROUTES from "./contants/Routes";
import { Home } from "../ui/pages/Home";
import { Login } from "../ui/pages/Login";
import { Page404 } from "../ui/pages/Error404";
import { PrivateRoute } from "./PrivateRoute";
import { ViewBottleMappingFile } from "../ui/pages/ViewBottleMapping";
import { TermsConditionsFrontend } from "../ui/pages/TermsConditionsFrontend";
import { PrivacyPolicy } from "../ui/pages/PrivacyPolicy";
import { TermsConditionsMobile } from "../ui/pages/TermsConditionsMobile";
import { TermsConditionsAPI } from "../ui/pages/TermsConditionsApi";

const Routes = () => {
  return (
    <Switch>
      <Route
        path={ROUTES.LOGIN}
        exact
        component={Login}
      />
      <PrivateRoute
        path={ROUTES.HOME}
        exact
        Component={Home}
      />
      <PrivateRoute
        path={ROUTES.VIEW_BOTTLE}
        exact
        Component={ViewBottleMappingFile}
      />
      <Route
        path={ROUTES.VIEW_TERMS_CONDITIONS_FRONTEND}
        exact
        component={TermsConditionsFrontend}
      />
      <Route
        path={ROUTES.VIEW_TERMS_CONDITIONS_MOBILE}
        exact
        component={TermsConditionsMobile}
      />
      <Route
        path={ROUTES.VIEW_TERMS_CONDITIONS_API}
        exact
        component={TermsConditionsAPI}
      />
      <Route
        path={ROUTES.VIEW_PRIVACY_POLICY}
        exact
        component={PrivacyPolicy}
      />
      <Route
        path={ROUTES.PAGE_404}
        exact
        component={Page404}
      />
    </Switch>
  );
};

export default Routes;

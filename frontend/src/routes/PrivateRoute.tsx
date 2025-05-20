import React from "react";
import { Redirect, Route, RouteProps } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import ROUTES from "./contants/Routes";
import { ModalTermsOfServices } from "../ui/components/ModalTermsConditions";

interface RouteComponent {
  path: string;
  exact?: boolean | undefined;
  Component: React.FC<RouteProps>;
}

const PrivateRoute: React.FC<RouteComponent> = ({ Component, ...props }) => {
  const { isAuthenticated, isLoadingKeyCloak, isShowTermsConditions } =
    useAuth();

  if (isLoadingKeyCloak && !isAuthenticated) {
    return null;
  }
  if (!isShowTermsConditions && isAuthenticated) {
    return <ModalTermsOfServices isModal={!isShowTermsConditions} />;
  }

  return (
    <Route
      {...props}
      render={(routeProps) =>
        isAuthenticated ? (
          <Component {...routeProps} />
        ) : (
          <Redirect to={ROUTES.LOGIN} />
        )
      }
    />
  );
};

export { PrivateRoute };

import React, { Dispatch } from "react";

interface AuthContextProps {
  isLoadingKeyCloak: boolean;
  isAuthenticated: boolean;
  validToken: TValidToken | undefined;
  userNameInfo: TUserNameInfo | undefined;
  isShowTermsConditions: boolean;
  setIsAuthenticated: Dispatch<React.SetStateAction<boolean>>;
  setValidToken: Dispatch<React.SetStateAction<TValidToken | undefined>>;
  setUserNameInfo: Dispatch<React.SetStateAction<TUserNameInfo | undefined>>;
  setIsShowTermsConditions: Dispatch<React.SetStateAction<boolean>>;
}

interface TValidToken {
  accessToken: string;
  refreshToken: string;
  role: string;
}

interface TUserNameInfo {
  userName: string;
  isEmail: boolean;
}

export type { AuthContextProps, TValidToken, TUserNameInfo };

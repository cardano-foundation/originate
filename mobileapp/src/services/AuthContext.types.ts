interface AuthContextProps {
  isAuthenticated: boolean;
  validToken?: TValidToken;
  lang: string | null;
  setLang: (lang: string) => void;
  login: () => void;
  logout: () => void;
  appTerm: boolean;
}

interface TValidToken {
  accessToken: string;
  expiresIn: number;
  idToken: string;
  issuedAt: number;
  refreshToken: string;
  scope: string;
  tokenType: string;
}

export type { AuthContextProps, TValidToken };

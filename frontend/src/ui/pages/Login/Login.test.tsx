import {
  fireEvent,
  render,
  renderHook,
  screen,
  waitFor,
  act,
} from "@testing-library/react";
import { createMemoryHistory } from "history";
import { Router } from "react-router-dom";
import { Login } from "./Login";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import ROUTES from "../../../routes/contants/Routes";
import { AuthProvider, useAuth } from "../../../contexts/AuthContext";

jest.useFakeTimers();

jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
  changeLanguage: jest.fn(),
}));

jest.mock("react-i18next", () => ({
  iniReactI18next: jest.fn(),
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
}));

jest.mock("../../../services/Instances/KeyCloakServices", () => ({
  keyCloakClient: {
    init: jest.fn().mockResolvedValue(true),
    login: jest.fn(),
    logout: jest.fn(),
    token: "token",
    refreshToken: "refreshToken",
    realmAccess: {
      roles: ["ADMIN"],
    },
  },
}));

describe("Login", () => {
  test("handle test click login button success", async () => {
    render(<Login />);
    const loginButton = screen.getByTestId("btn-login");
    expect(loginButton).toBeInTheDocument();
    fireEvent.click(loginButton);
    await waitFor(() => {
      expect(keyCloakClient.login).toHaveBeenCalled();
    });
  });

  test("handle test click login button error", async () => {
    keyCloakClient.login = jest.fn(() => {
      throw new Error("login error");
    });
    act(() => {
      render(<Login />);
    });
    const loginButton = screen.getByTestId("btn-login");
    expect(loginButton).toBeInTheDocument();
    fireEvent.click(loginButton);
    jest.advanceTimersByTime(5000);
    await waitFor(() => {
      expect(keyCloakClient.logout).toHaveBeenCalled();
    });
  });

  test("login success and handle with ADMIN role", async () => {
    const history = createMemoryHistory();
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    act(() => {
      render(
        <Router history={history}>
          <Login />
        </Router>
      );
    });
    await waitFor(() => {
      expect(keyCloakClient.token).not.toBeUndefined();
      expect(result.current.isAuthenticated).toEqual(true);
      expect(result.current.validToken?.accessToken).toEqual(
        keyCloakClient.token
      );
      expect(result.current.validToken?.role).toEqual(
        keyCloakClient?.realmAccess?.roles[0]
      );
      expect(result.current.validToken?.refreshToken).toEqual(
        keyCloakClient.refreshToken
      );
      expect(history.location.pathname).toBe(ROUTES.HOME);
    });
  });

  test("login success and handle with NWA role", async () => {
    keyCloakClient.realmAccess = {
      roles: ["NWA"],
    };

    await act(async () => {
      render(<Login />);
    });

    const errorMessage = screen.getByText("failRole");
    expect(errorMessage).toBeInTheDocument();
    jest.advanceTimersByTime(5000);
    await waitFor(() => {
      expect(keyCloakClient.logout).toHaveBeenCalled();
    });
  });

  test("login success and handle with WINERY role", async () => {
    const history = createMemoryHistory();
    keyCloakClient.realmAccess = {
      roles: ["WINERY"],
    };
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    act(() => {
      render(
        <Router history={history}>
          <Login />
        </Router>
      );
    });
    await waitFor(() => {
      expect(keyCloakClient.token).not.toBeUndefined();
      expect(result.current.isAuthenticated).toEqual(true);
      expect(result.current.validToken?.accessToken).toEqual(
        keyCloakClient.token
      );
      expect(result.current.validToken?.role).toEqual(
        keyCloakClient?.realmAccess?.roles[0]
      );
      expect(result.current.validToken?.refreshToken).toEqual(
        keyCloakClient.refreshToken
      );
      expect(history.location.pathname).toBe(ROUTES.HOME);
    });
  });

  test("login component and handle login with not assign role", async () => {
    keyCloakClient.realmAccess = undefined;

    await act(async () => {
      render(<Login />);
    });

    const errorMessage = screen.getByText("failRole");
    expect(errorMessage).toBeInTheDocument();
    jest.advanceTimersByTime(5000);
    await waitFor(() => {
      expect(keyCloakClient.logout).toHaveBeenCalled();
    });
  });

  test("login component and handle login with multiple role", async () => {
    keyCloakClient.realmAccess = {
      roles: ["NWA", "ADMIN"],
    };

    await act(async () => {
      render(<Login />);
    });

    const errorMessage = screen.getByText("failRole");
    expect(errorMessage).toBeInTheDocument();
    jest.advanceTimersByTime(5000);
    await waitFor(() => {
      expect(keyCloakClient.logout).toHaveBeenCalled();
    });
  });

  test("login component and handle login with multiple role and role default ", async () => {
    const history = createMemoryHistory();
    keyCloakClient.realmAccess = {
      roles: ["ADMIN", "default_role_keycloak"],
    };
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    act(() => {
      render(
        <Router history={history}>
          <Login />
        </Router>
      );
    });
    await waitFor(() => {
      expect(keyCloakClient.token).not.toBeUndefined();
      expect(result.current.isAuthenticated).toEqual(true);
      expect(result.current.validToken?.accessToken).toEqual(
        keyCloakClient.token
      );
      expect(result.current.validToken?.role).toEqual(
        keyCloakClient?.realmAccess?.roles[0]
      );
      expect(result.current.validToken?.refreshToken).toEqual(
        keyCloakClient.refreshToken
      );
      expect(history.location.pathname).toBe(ROUTES.HOME);
    });
  });

  test("should show message and handle logout for logoutCode=401", async () => {
    const history = createMemoryHistory();
    const queryString = "?logoutCode=401";
    history.push(queryString);
    act(() => {
      render(
        <Router history={history}>
          <Login />
        </Router>
      );
    });
    const errorMessage = screen.getByText("authorize");
    expect(errorMessage).toBeInTheDocument();
  });

  test("init keycloak error", async () => {
    keyCloakClient.init = jest.fn().mockRejectedValue({});
    await act(async () => {
      render(<Login />);
    });

    const errorMessage = screen.getByText("failKeycloak");
    expect(errorMessage).toBeInTheDocument();
    jest.advanceTimersByTime(5000);
    await waitFor(() => {
      expect(keyCloakClient.logout).toHaveBeenCalled();
    });
  });
});

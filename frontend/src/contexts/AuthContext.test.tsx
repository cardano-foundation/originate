import { act, render, renderHook, screen } from "@testing-library/react";
import { AuthProvider, initialAuthContext, useAuth } from "./AuthContext";
import { keyCloakClient } from "../services/Instances/KeyCloakServices";

jest.mock("../services/Instances/KeyCloakServices", () => ({
  keyCloakClient: {
    init: jest.fn().mockResolvedValue(true),
    token: "token",
    refreshToken: "refreshToken",
    realmAccess: {
      roles: ["ADMIN"],
    },
    tokenParsed: {
      full_name: "userName",
      email: "email",
      web_terms: true,
    },
  },
}));

describe("AuthContext", () => {
  test("AuthProvider renders children", async () => {
    act(() => {
      render(
        <AuthProvider>
          <div>Child component</div>
        </AuthProvider>
      );
    });

    const childComponent = screen.getByText("Child component");
    expect(childComponent).toBeInTheDocument();
  });

  test("useAuth returns the correct context values keyCloak", async () => {
    const TestComponent = () => {
      const authContext = useAuth();
      return (
        <div>
          <span data-testid="isAuthenticated">
            {authContext.isAuthenticated.toString()}
          </span>
          <span data-testid="token">{authContext.validToken?.accessToken}</span>
          <span data-testid="role">{authContext.validToken?.role}</span>
          <span data-testid="refreshToken">
            {authContext.validToken?.refreshToken}
          </span>
          <span data-testid="userName">
            {authContext.userNameInfo?.userName}
          </span>
          <span data-testid="webTerms">
            {authContext.isShowTermsConditions.toString()}
          </span>
        </div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    const isAuthenticatedElement = screen.getByTestId("isAuthenticated");
    expect(isAuthenticatedElement.textContent).toBe("true");

    const isTermsElement = screen.getByTestId("webTerms");
    expect(isTermsElement.textContent).toBe("true");

    const token = screen.getByTestId("token");
    expect(token.textContent).toBe("token");
    const role = screen.getByTestId("role");
    expect(role.textContent).toBe("ADMIN");
    const refreshToken = screen.getByTestId("refreshToken");
    expect(refreshToken.textContent).toBe("refreshToken");
    const userName = screen.getByTestId("userName");
    expect(userName.textContent).toBe("userName");
  });

  test("useAuth returns the correct context values keyCloak with not value full_name and role ADMIN", async () => {
    keyCloakClient.tokenParsed = {
      email: "email",
      web_terms: true,
    };
    const TestComponent = () => {
      const authContext = useAuth();
      return (
        <div>
          <span data-testid="isAuthenticated">
            {authContext.isAuthenticated.toString()}
          </span>
          <span data-testid="role">{authContext.validToken?.role}</span>
          <span data-testid="userName">
            {authContext.userNameInfo?.userName}
          </span>
          <span data-testid="webTerms">
            {authContext.isShowTermsConditions.toString()}
          </span>
        </div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    const isAuthenticatedElement = screen.getByTestId("isAuthenticated");
    expect(isAuthenticatedElement.textContent).toBe("true");

    const isTermsElement = screen.getByTestId("webTerms");
    expect(isTermsElement.textContent).toBe("true");

    const role = screen.getByTestId("role");
    expect(role.textContent).toBe("ADMIN");
    const userName = screen.getByTestId("userName");
    expect(userName.textContent).toBe("email");
  });

  test("useAuth returns the fail role system", async () => {
    keyCloakClient.realmAccess = {
      roles: ["NWA"],
    };
    const TestComponent = () => {
      const authContext = useAuth();
      return (
        <div>
          <span data-testid="isAuthenticated">
            {authContext.isAuthenticated.toString()}
          </span>
          <span data-testid="isLoadingKeyCloak">
            {authContext.isLoadingKeyCloak.toString()}
          </span>
        </div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });
    const isAuthenticatedElement = screen.getByTestId("isAuthenticated");
    expect(isAuthenticatedElement.textContent).toBe("false");
    const isLoadingKeyCloak = screen.getByTestId("isLoadingKeyCloak");
    expect(isLoadingKeyCloak.textContent).toBe("false");
  });

  test("useAuth error get value keyCloak", async () => {
    keyCloakClient.init = jest.fn().mockRejectedValue({});
    const TestComponent = () => {
      const authContext = useAuth();
      return (
        <div>
          <span data-testid="isAuthenticated">
            {authContext.isAuthenticated.toString()}
          </span>
          <span data-testid="isLoadingKeyCloak">
            {authContext.isLoadingKeyCloak.toString()}
          </span>
        </div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    const isAuthenticatedElement = screen.getByTestId("isAuthenticated");
    expect(isAuthenticatedElement.textContent).toBe("false");
    const isLoadingKeyCloak = screen.getByTestId("isLoadingKeyCloak");
    expect(isLoadingKeyCloak.textContent).toBe("false");
  });

  test("useAuth returns context initial values", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    expect(result.current.isLoadingKeyCloak).toBe(true);
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.validToken).toBe(undefined);
    expect(result.current.userNameInfo).toBe(undefined);
    expect(result.current.isShowTermsConditions).toBe(false);
    expect(result.current.setValidToken).toBeInstanceOf(Function);
    expect(result.current.setIsAuthenticated).toBeInstanceOf(Function);
    expect(result.current.setUserNameInfo).toBeInstanceOf(Function);
    expect(result.current.setIsShowTermsConditions).toBeInstanceOf(Function);
    expect(() => initialAuthContext.setValidToken(undefined)).not.toThrow();
    expect(() => initialAuthContext.setIsAuthenticated(true)).not.toThrow();
    expect(() => initialAuthContext.setUserNameInfo(undefined)).not.toThrow();
    expect(() =>
      initialAuthContext.setIsShowTermsConditions(true)
    ).not.toThrow();
  });
});

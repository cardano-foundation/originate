import { Subject, Subscriber } from "rxjs";
const events$ = new Subject();
const user$ = new Subject();
const isAuthenticated$ = new Subject();
const initComplete$ = new Subject();
const authServiceMock = {
  signIn: jest.fn(),
  signOut: jest.fn(),
  endSessionCallback: jest.fn(),
  authorizationCallback: jest.fn(),
  revokeTokens: jest.fn(),
  authConfig: {},
  events$: {
    subscribe: jest.fn((callback) => events$.subscribe(callback)),
  },
  user$: {
    subscribe: jest.fn((callback) => user$.subscribe(callback)),
  },
  isAuthenticated$: {
    pipe: jest.fn(() => ({
      take: jest.fn(() => ({
        subscribe: jest.fn((callback) => isAuthenticated$.subscribe(callback)),
      })),
    })),
  },
  init: jest.fn(),
  initComplete$: {
    pipe: jest.fn(() => ({
      subscribe: jest.fn((callback) => initComplete$.subscribe(callback)),
    })),
  },
  loadUserInfo: jest.fn(),
  mockEvents: (action: any) => events$.next(action),
  mockUser: (user: any) => user$.next(user),
  mockIsAuthenticated: (isAuthenticated: any) =>
    isAuthenticated$.next(isAuthenticated),
  mockInitComplete: () => initComplete$.next(true),
};
import { act, render, screen, waitFor } from "@testing-library/react";
import { useTranslation } from "react-i18next";
import { Device } from "@capacitor/device";
import { AuthProvider, useAuth } from "./AuthContext";
import { ToastMessageContext } from "../context";
import { sleep } from "../utils";
import { ToastMessageType } from "../ui/common/types";
import { i18n } from "../i18n";
(useTranslation as jest.Mock).mockReturnValue({
  t: jest.fn((key) => key),
});

jest.mock("@ionic/react", () => ({
  useIonViewDidLeave: jest.fn(),
  isPlatform: jest.fn(),
}));
jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
}));
jest.mock("react-i18next", () => ({
  useTranslation: jest.fn().mockImplementation(() => ({
    t: jest.fn(),
  })),
}));
jest.mock("ionic-appauth", () => ({
  AuthService: jest.fn().mockImplementation(() => authServiceMock),
  AuthActions: {
    SignInSuccess: "Sign In Success",
    SignInFailed: "Sign In Failed",
    SignOutSuccess: "Sign Out Success",
    SignOutFailed: "Sign Out Failed",
    RefreshSuccess: "Refresh Success",
    RefreshFailed: "Refesh Failed",
  },
}));
jest.mock("@capacitor/device", () => ({
  Device: {
    getLanguageCode: jest.fn().mockResolvedValue({ value: "ka" }),
  },
}));
// Mock function changeLanguage  i18n
i18n.changeLanguage = jest.fn();
describe("AuthProvider", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test("changes language and sets initial language correctly", async () => {
    const mockDeviceLanguage = "en";
    jest
      .spyOn(Device, "getLanguageCode")
      .mockResolvedValue({ value: mockDeviceLanguage });
    const TestComponent = () => {
      const { lang } = useAuth();
      return <div data-testid="lang">{lang}</div>;
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId("lang")).toHaveTextContent(mockDeviceLanguage);
    act(() => {
      window.localStorage.setItem("language", "en");
    });
    expect(screen.getByTestId("lang")).toHaveTextContent("en");
    expect(i18n.changeLanguage).toHaveBeenCalledWith("en");
  });

  test("renders children and provides the correct context values", () => {
    const ChildComponent = () => {
      const auth = useAuth();
      return (
        <div>
          {auth.isAuthenticated ? "Authenticated" : "Not Authenticated"}
        </div>
      );
    };
    const { getByText } = render(
      <AuthProvider>
        <ChildComponent />
      </AuthProvider>
    );
    expect(getByText("Not Authenticated")).toBeInTheDocument();
  });
  test("calls authService.signIn when login function is invoked", async () => {
    const TestComponent = () => {
      const auth = useAuth();
      return (
        <button
          data-testid="mock-login-button"
          onClick={auth.login}
        >
          login
        </button>
      );
    };
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    const loginButton = screen.getByTestId("mock-login-button");
    expect(loginButton).toBeInTheDocument();
    await sleep(100);
    loginButton.click();
    await waitFor(() => {
      expect(authServiceMock.signIn).toHaveBeenCalledTimes(1);
    });
  });
  test("calls authService.signOut when logout function is invoked", async () => {
    const TestComponent = () => {
      const auth = useAuth();
      return (
        <button
          data-testid="mock-logout-button"
          onClick={auth.logout}
        >
          logout
        </button>
      );
    };
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    const logout = screen.getByTestId("mock-logout-button");
    expect(logout).toBeInTheDocument();
    await sleep(100);
    logout.click();
    await waitFor(() => {
      expect(authServiceMock.signOut).toHaveBeenCalledTimes(1);
      expect(authServiceMock.revokeTokens).toHaveBeenCalledTimes(1);
      expect(authServiceMock.endSessionCallback).toHaveBeenCalledTimes(1);
    });
  });
  test("should show an error toast on logout failure", async () => {
    authServiceMock.signOut = jest
      .fn()
      .mockRejectedValue(new Error("mock-error"));

    const TestComponent = () => {
      const auth = useAuth();
      return (
        <button
          data-testid="mock-logout-button"
          onClick={auth.logout}
        >
          logout
        </button>
      );
    };
    const showToastMock = jest.fn();
    render(
      <ToastMessageContext.Provider value={{ showToast: showToastMock }}>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </ToastMessageContext.Provider>
    );
    const logout = screen.getByTestId("mock-logout-button");
    expect(logout).toBeInTheDocument();
    await sleep(100);
    logout.click();
    await waitFor(() => {
      expect(showToastMock).toBeCalledWith(
        "signOutFailed",
        ToastMessageType.ERROR
      );
    });
  });

  test("should show an error toast on login failure", async () => {
    authServiceMock.events$ = {
      subscribe: jest.fn((callback) => {
        // Simulate login failure
        callback({ action: "Sign In Failed" });
        return Subscriber.EMPTY;
      }),
    };
    const TestComponent = () => {
      const auth = useAuth();
      return (
        <button
          data-testid="mock-login-button"
          onClick={auth.login}
        >
          log in
        </button>
      );
    };
    const showToastMock = jest.fn();
    render(
      <ToastMessageContext.Provider value={{ showToast: showToastMock }}>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </ToastMessageContext.Provider>
    );
    const logout = screen.getByTestId("mock-login-button");
    expect(logout).toBeInTheDocument();
    await sleep(100);
    logout.click();
    await waitFor(() => {
      expect(showToastMock).toBeCalledWith(
        "loginErrorMsg",
        ToastMessageType.ERROR
      );
    });
  });

  test("should show an error toast on login without role winery", async () => {
    authServiceMock.events$ = {
      subscribe: jest.fn((callback) => {
        // Simulate login failure
        callback({
          action: "Sign In Success",
          tokenResponse: {
            accessToken:
              "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrZng2SzdRa1ctdjA2UzR5LThUTUZ6MmJBSGZka3UxOU1iLUtBcVJuVTFRIn0.eyJleHAiOjE2OTA4Nzc3NzgsImlhdCI6MTY5MDg3NTk3OCwiYXV0aF90aW1lIjoxNjkwODc1OTc3LCJqdGkiOiJjYzhkMTY1NS1iZWQxLTQ4YjUtYWFhZS1hNjRmZDY0NzhmOTQiLCJpc3MiOiJodHRwczovL3Bvby1rZXljbG9hay1kZXYuc290YXRlay53b3Jrcy9yZWFsbXMvQm9sbmlzaVBpbG90QXBwbGljYXRpb24iLCJzdWIiOiJjNWU2NWMzNC1mM2Y2LTQxZTUtYjcxYS1lYWVlY2Q2OTM3YTUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtb2JpbGUtc2Nhbi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiNDA3NjBhODQtYzcwOC00MmIyLTg0YWEtZTU5YmU0MjBhYjRjIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJzaWQiOiI0MDc2MGE4NC1jNzA4LTQyYjItODRhYS1lNTliZTQyMGFiNGMiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6Im5nb2MucGhhbTNAc290YXRlay5jb20iLCJsb2NhbGUiOiJlbiIsImdpdmVuX25hbWUiOiIiLCJmYW1pbHlfbmFtZSI6IiIsImVtYWlsIjoibmdvYy5waGFtM0Bzb3RhdGVrLmNvbSJ9.XDQdFJkR28A3vUTNm_cmg0hOJXdOjnjiuR-JThwx2nAZmd5AvYtsCvNDLynjNEc4r8bIVdz4uTMm7HmP2lR3vazWlUD_ivAHMyc9zbtJF6mieyJpDujOKelz4_-lKh3O5Lwe77-1m1rS1JGvgHrGmGWM13ZI2ECm4N0cEwUAAypd-VF9V0AItwa4E81R9q-bt07gqa7M714E_Fm6zb8uzjkRjhkYPnLi8z1hk-C1FsE0tvvRzxMqBdEpcdxMeBmzBikZOe6mHOzzLN83n5kTaZuThRpdubi8wZd7NunVJuMVYFWnMt7zbzgPWytxCHi6_SnO2jnoxZZ5uhNHXUlmwA",
          },
        });
        return Subscriber.EMPTY;
      }),
    };
    const TestComponent = () => {
      const auth = useAuth();
      return (
        <button
          data-testid="mock-login-button"
          onClick={auth.login}
        >
          log in
        </button>
      );
    };
    const showToastMock = jest.fn();
    render(
      <ToastMessageContext.Provider value={{ showToast: showToastMock }}>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </ToastMessageContext.Provider>
    );
    const logout = screen.getByTestId("mock-login-button");
    expect(logout).toBeInTheDocument();
    await sleep(100);
    logout.click();
    await waitFor(() => {
      expect(showToastMock).toBeCalledWith(
        "messageLoginErr",
        ToastMessageType.ERROR
      );
    });
  });
});

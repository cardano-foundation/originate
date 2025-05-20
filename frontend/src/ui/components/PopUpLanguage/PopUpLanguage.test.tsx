import { fireEvent, render, screen } from "@testing-library/react";
import { PopUpLanguage } from "./PopUpLanguage";
import { SelectLanguage } from "../SelectLanguage";
import { LanguageType } from "../SelectLanguage/types";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";

/* global process */

jest.mock("../../../services/Instances/KeyCloakServices", () => ({
  keyCloakClient: {
    logout: jest.fn(),
  },
}));

jest.mock("../../../contexts/AuthContext", () => ({
  useAuth: () => ({
    userNameInfo: {
      userName: "test abcd",
      isEmail: false,
    },
  }),
}));

const onCancel = jest.fn();
const setMessageAlert = jest.fn();

describe("PopUpLanguage test", () => {
  beforeEach(() => {
    Object.defineProperty(navigator, "language", {
      value: "ka",
      configurable: true,
    });
    window.localStorage.removeItem("language");
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("should render content modal language", async () => {
    render(
      <PopUpLanguage
        isModal={true}
        onCancel={onCancel}
        setMessageAlert={setMessageAlert}
      />
    );
    const avatarName = screen.getByText("TA");
    const userNameElement = screen.getByText("test abcd");
    const contentUser = screen.getByTestId("content-user");

    expect(avatarName).toBeInTheDocument();
    expect(userNameElement).toBeInTheDocument();
    expect(contentUser).toBeInTheDocument();

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    const optionLanguageEn = screen.getByTestId(LanguageType.English);
    expect(optionLanguageEn).toBeInTheDocument();
    fireEvent.click(optionLanguageEn);
    expect(window.localStorage.getItem("language")).toBe("en");
    const signOutButton = screen.getByText("Sign out");
    expect(signOutButton).toBeInTheDocument();
    expect(screen.getByText("Language")).toBeInTheDocument();
  });

  test("should render the select language only icon", async () => {
    render(<SelectLanguage isOnlySelect={true} />);

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    expect(screen.getAllByText("English").length).toEqual(1);
    expect(screen.getAllByText("Georgian").length).toEqual(1);
  });

  test("handle test function logout success", async () => {
    const callLogoutService = keyCloakClient.logout;
    render(
      <PopUpLanguage
        isModal={true}
        onCancel={onCancel}
        setMessageAlert={setMessageAlert}
      />
    );
    const btnLogout = screen.getByTestId("btn-logout");
    fireEvent.click(btnLogout);
    expect(callLogoutService).toHaveBeenCalledWith({
      redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}`,
    });
  });

  test("handle test function logout error", async () => {
    keyCloakClient.logout = jest.fn(() => {
      throw new Error("Logout error");
    });
    render(
      <PopUpLanguage
        isModal={true}
        onCancel={onCancel}
        setMessageAlert={setMessageAlert}
      />
    );
    const btnLogout = screen.getByTestId("btn-logout");
    fireEvent.click(btnLogout);
    expect(setMessageAlert).toHaveBeenCalled();
  });

  test("handle test close popup language when click outside", async () => {
    render(
      <PopUpLanguage
        isModal={true}
        onCancel={onCancel}
        setMessageAlert={setMessageAlert}
      />
    );
    const backdropElement = document.querySelector(".MuiBackdrop-root");
    if (backdropElement) {
      fireEvent.click(backdropElement);
      expect(onCancel).toHaveBeenCalledTimes(1);
    }
  });
});

import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { LanguageType } from "../SelectLanguage/types";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import { SelectLanguage } from "../SelectLanguage";
import { ModalTermsOfServices } from "./ModalTermsOfServices";
import { TermsAcceptService, RefreshTokenService } from "../../../services";
import { MemoryRouter } from "react-router-dom";

/* global process */
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
  Trans: ({
    i18nKey,
    components,
  }: {
    i18nKey: string;
    components: React.ReactElement[];
  }) => (
    <>
      ${i18nKey} ${components}
    </>
  ),
}));

jest.mock("../../../services/Instances/KeyCloakServices", () => ({
  keyCloakClient: {
    logout: jest.fn(),
  },
}));

jest.mock("../../../contexts/AuthContext", () => ({
  useAuth: () => ({
    validToken: {
      refreshToken: "refreshToken",
    },
  }),
}));

jest.mock("../../../services", () => ({
  TermsAcceptService: {
    termsAccept: jest.fn().mockResolvedValue({
      status: 204,
      data: "",
    }),
  },
  RefreshTokenService: {
    refreshToken: jest.fn().mockResolvedValue({
      status: 200,
      data: {
        access_token: "",
        refresh_token: {
          current: "",
        },
      },
    }),
  },
}));

describe("ModalTermConditions", () => {
  beforeEach(() => {
    Object.defineProperty(navigator, "language", {
      value: "ka",
      configurable: true,
    });
  });

  test("should render content ModalTermOfServices component", async () => {
    act(() => {
      render(
        <MemoryRouter>
          <ModalTermsOfServices isModal={true} />
        </MemoryRouter>
      );
    });
    expect(screen.getByTestId("icon-check")).toBeInTheDocument();
    expect(screen.getByTestId("label-check")).toBeInTheDocument();
    expect(screen.getByTestId("titleHeaderModalTerm")).toBeInTheDocument();
    expect(screen.getByTestId("button-continue-disable")).toBeInTheDocument();

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    const optionLanguageEn = screen.getByTestId(LanguageType.English);
    expect(optionLanguageEn).toBeInTheDocument();
    fireEvent.click(optionLanguageEn);
    expect(screen.getAllByText("english").length).toEqual(1);
    expect(screen.getAllByText("georgian").length).toEqual(1);
  });

  test("should render the select language", async () => {
    render(
      <MemoryRouter>
        <SelectLanguage />
      </MemoryRouter>
    );

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    const optionLanguageEn = screen.getByTestId(LanguageType.English);
    expect(optionLanguageEn).toBeInTheDocument();
    fireEvent.click(optionLanguageEn);
    expect(window.localStorage.getItem("language")).toBe("en");
    const optionLanguageGe = screen.getByTestId(LanguageType.Georgian);
    expect(optionLanguageGe).toBeInTheDocument();
    fireEvent.click(optionLanguageGe);
    expect(window.localStorage.getItem("language")).toBe("ka");
  });

  test("handle confirm function continue modal terms of services", async () => {
    act(() => {
      render(
        <MemoryRouter>
          <ModalTermsOfServices isModal={true} />
        </MemoryRouter>
      );
    });
    const buttonContinueDisable = screen.getByTestId("button-continue-disable");
    expect(buttonContinueDisable).toBeInTheDocument();
    const buttonCheckBox = screen.getByTestId("icon-check").children[0];
    fireEvent.click(buttonCheckBox);

    await waitFor(async () => {
      const buttonContinue = screen.getByTestId("button-continue");
      expect(buttonContinue).toBeInTheDocument();
      fireEvent.click(buttonContinue);
      expect(TermsAcceptService.termsAccept).toBeCalled();
      expect(RefreshTokenService.refreshToken).toBeCalled();
    });
  });

  test("handle test function logout success", async () => {
    act(() => {
      render(
        <MemoryRouter>
          <ModalTermsOfServices isModal={true} />
        </MemoryRouter>
      );
    });
    const callLogoutService = keyCloakClient.logout;
    const btnLogout = screen.getByTestId("btn-logout");
    fireEvent.click(btnLogout);
    expect(callLogoutService).toHaveBeenCalledWith({
      redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}`,
    });
  });

  test("handle test function logout error", async () => {
    act(() => {
      render(
        <MemoryRouter>
          <ModalTermsOfServices isModal={true} />
        </MemoryRouter>
      );
    });
    keyCloakClient.logout = jest.fn(() => {
      throw new Error("Logout error");
    });
    const btnLogout = screen.getByTestId("btn-logout");
    fireEvent.click(btnLogout);
  });

  test("should show message call function click button continue with error not found api", async () => {
    TermsAcceptService.termsAccept = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });

    act(() => {
      render(
        <MemoryRouter>
          <ModalTermsOfServices isModal={true} />
        </MemoryRouter>
      );
    });

    const buttonContinueDisable = screen.getByTestId("button-continue-disable");
    expect(buttonContinueDisable).toBeInTheDocument();
    const buttonCheckBox = screen.getByTestId("icon-check").children[0];
    fireEvent.click(buttonCheckBox);

    await waitFor(() => {
      const buttonContinue = screen.getByTestId("button-continue");
      expect(buttonContinue).toBeInTheDocument();
      fireEvent.click(buttonContinue);
      expect(TermsAcceptService.termsAccept).toBeCalled();
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
    });
  });
});

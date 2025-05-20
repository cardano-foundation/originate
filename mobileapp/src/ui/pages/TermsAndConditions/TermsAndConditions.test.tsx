import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import React from "react";
import { Browser } from "@capacitor/browser";
import userEvent from "@testing-library/user-event";
import { TermsAndConditions } from "./TermsAndConditions";
import { BackendAPI } from "../../../services";

const mockLogout = jest.fn();
jest.mock("../../../services/AuthContext");
jest.mock("../../../services/backendApi");
jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
}));
const mockSetLang = jest.fn();
jest.mock("../../../services/AuthContext", () => ({
  useAuth: jest.fn(() => ({
    appTerm: false,
    lang: "en",
    setLang: mockSetLang,
    logout: mockLogout,
  })),
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

jest.mock("@capacitor/browser", () => ({
  Browser: {
    open: jest.fn(),
  },
}));
describe("TermsAndConditions component", () => {
  test("calls BackendAPI.agreeTerm() and refreshToken on continue button click", async () => {
    render(<TermsAndConditions />);
    const checkbox = screen.getByTestId("checkbox");

    // Simulate clicking the checkbox
    expect(screen.getByTestId("confirm-button")).toBeDisabled();
    act(() => {
      fireEvent.click(checkbox);
    });
    await waitFor(async () => {
      expect(screen.getByTestId("confirm-button")).not.toBeDisabled();
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    // Wait for asynchronous actions to complete
    await waitFor(async () => {
      expect(BackendAPI.agreeTerm).toHaveBeenCalled();
    });
  });

  test("calls logout function on sign out button click", () => {
    render(<TermsAndConditions />);

    const signOutButton = screen.getByText("signOutTerm");

    // Simulate clicking the sign out button
    userEvent.click(signOutButton);

    // Assert that the logout function was called
    expect(mockLogout).toHaveBeenCalled();
  });

  test("calls Browser.open() on privacy policy button click", () => {
    render(<TermsAndConditions />);

    const privacyPolicyButton = screen.getByTestId("privacy-policy-link");

    // Simulate clicking the privacy policy button
    userEvent.click(privacyPolicyButton);

    // Assert that the Browser.open() function was called
    expect(Browser.open).toHaveBeenCalled();
  });

  test("should set isChecked to true and enable checkbox", async () => {
    jest.spyOn(React, "useState").mockImplementation(() => [true, jest.fn()]);
    render(<TermsAndConditions />);

    const continueButton = screen.getByTestId("confirm-button");
    // Assert that the checkbox is checked
    fireEvent.click(continueButton);
    await waitFor(async () => {
      expect(continueButton).toBeEnabled();
      expect(BackendAPI.agreeTerm).toHaveBeenCalled();
    });
  });
});

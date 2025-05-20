import { act, fireEvent, render, screen } from "@testing-library/react";
import { Footer } from "./Footer";
import ROUTES from "../../../routes/contants/Routes";

const openMock = jest.fn();
window.open = openMock;

jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
}));

jest.mock("../../../contexts/AuthContext", () => ({
  useAuth: () => ({
    isShowTermsConditions: true,
  }),
}));

jest.mock("react-i18next", () => ({
  iniReactI18next: jest.fn(),
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
}));

describe("ViewFooter test", () => {
  test("should render page footer", async () => {
    act(() => {
      render(<Footer />);
    });
    expect(screen.getByText("footerCopyright")).toBeInTheDocument();
    expect(screen.getByText("terms")).toBeInTheDocument();
    expect(screen.getByText("privacy")).toBeInTheDocument();
  });
  test("should render view page TermsConditions", async () => {
    act(() => {
      render(<Footer />);
    });
    const termsLink = screen.getByTestId("viewTerms");
    fireEvent.click(termsLink);
    expect(openMock).toHaveBeenCalledTimes(1);
    expect(openMock).toHaveBeenCalledWith(
      expect.stringContaining(ROUTES.VIEW_TERMS_CONDITIONS_FRONTEND),
      "_blank"
    );
  });

  test("should render view page PrivacyPolicy", async () => {
    act(() => {
      render(<Footer />);
    });
    const privacyLink = screen.getByTestId("viewPrivacy");
    fireEvent.click(privacyLink);
    expect(openMock).toHaveBeenCalledTimes(1);
    expect(openMock).toHaveBeenCalledWith(
      expect.stringContaining(ROUTES.VIEW_PRIVACY_POLICY),
      "_blank"
    );
  });
});

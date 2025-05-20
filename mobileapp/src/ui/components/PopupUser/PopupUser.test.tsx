import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import { PopupUser } from "./PopupUser";
import { Browser } from "@capacitor/browser";

process.env.FRONTEND_DOMAIN_PUBLIC_URL =
  "https://poo-frontend-dev.sotatek.works";

const mockLogout = jest.fn();
jest.mock("../../../services/AuthContext", () => ({
  useAuth: () => ({
    logout: mockLogout,
  }),
}));
jest.mock("@capacitor/browser", () => ({
  Browser: {
    open: jest.fn(),
  },
}));
jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
}));

jest.mock("react-i18next", () => ({
  iniReactI18next: jest.fn(),
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
}));
describe("PopupUser", () => {
  test("should render the popup when isOpen is true", async () => {
    const mockDataModal = {
      isOpen: true,
      onLeave: jest.fn(),
    };
    const { getByTestId } = render(
      <PopupUser
        isOpen={mockDataModal.isOpen}
        onLeave={mockDataModal.onLeave}
        username="test"
      />
    );
    await waitFor(() => {
      const infoElement = screen.getByTestId("popup-user-setting");
      fireEvent.click(getByTestId("popup-user-setting"));
      expect(screen.getByText("test")).toBeInTheDocument();
      expect(infoElement).toBeInTheDocument();
    });
    fireEvent.click(getByTestId("popup-user-closeicon"));
    expect(mockDataModal.onLeave).toBeCalled();
  });

  test("will be call signOut function when click button logout", async () => {
    act(() => {
      render(
        <PopupUser
          isOpen
          username="test"
        />
      );
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("btn-sign-out"));
      expect(mockLogout).toHaveBeenCalled();
      // checkbutton Term and Condition
      const termAndConditionButton = screen.getByText("termAndCondition");
      fireEvent.click(termAndConditionButton);
      const expectedTermURL = `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/terms/mobile`;
      expect(Browser.open).toHaveBeenCalledWith({
        url: expectedTermURL,
      });

      // check button Privacy Policy
      const privacyPolicyButton = screen.getByText("privacyPolicy");
      fireEvent.click(privacyPolicyButton);
      const expectedPrivacyURL = `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/privacy`;
      expect(Browser.open).toHaveBeenCalledWith({
        url: expectedPrivacyURL,
      });
    });
  });
});

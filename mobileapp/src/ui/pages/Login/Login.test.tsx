import {
  act,
  render,
  screen,
  fireEvent,
  waitFor,
} from "@testing-library/react";
import { Login } from "./Login";
import { i18n } from "../../../i18n";
import { SelectLanguageButton } from "../../components/SelectLanguageButton";
// Mock function changeLanguage i18n
i18n.changeLanguage = jest.fn();

const mockLogin = jest.fn();
jest.mock("../../../services/AuthContext", () => ({
  useAuth: () => ({
    login: mockLogin,
  }),
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
test("renders login component", () => {
  render(<Login />);

  const logo = screen.getByAltText("cardano-logo");
  const loginButton = screen.getByText("login");

  expect(logo).toBeInTheDocument();
  expect(loginButton).toBeInTheDocument();
});

test("calls login function on button click", async () => {
  act(() => {
    render(<Login />);
  });
  const loginButton = await screen.findByText("login");
  fireEvent.click(loginButton);
  await waitFor(() => expect(mockLogin).toHaveBeenCalled());
});

test("should render the Login component correctly", () => {
  const { getByTestId, getByAltText, getByText } = render(<Login />);
  const cardanoLogo = getByAltText("cardano-logo");
  const loginButton = getByText("login");
  const selectLanguageButton = getByTestId("select-language-button");

  expect(cardanoLogo).toBeInTheDocument();
  expect(loginButton).toBeInTheDocument();
  expect(selectLanguageButton).toBeInTheDocument();
});

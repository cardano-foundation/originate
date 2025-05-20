import { Preferences } from "@capacitor/preferences";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LanguageType } from "../../common/types";
import { SelectLanguageButton } from "./SelectLanguageButton";

jest.mock("@capacitor/preferences");
Preferences.get = jest.fn();
Preferences.set = jest.fn();

jest.mock("react-i18next", () => ({
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
}));
// Mock the Device.getLanguageCode function
jest.mock("@capacitor/device", () => ({
  Device: {
    getLanguageCode: jest.fn().mockResolvedValue({ value: "ka" }),
  },
}));
describe("SelectLanguageButton", () => {
  test("should show modal when button is clicked", () => {
    render(<SelectLanguageButton />);
    waitFor(() => {
      fireEvent.click(screen.getByTestId("select-language-button"));
      const modal = screen.getByTestId("select-language-modal");
      expect(modal).toBeInTheDocument();
    });
  });

  test("renders with the correct initial language", async () => {
    // Mock the initial language preference
    const initialLang = LanguageType.English;
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    require("@capacitor/preferences").Preferences.get.mockResolvedValue({
      value: initialLang,
    });

    render(
      <SelectLanguageButton
        lang={initialLang}
        setLang={() => {}}
      />
    );

    // Verify that the flag for the initial language is displayed
    const flagElement = screen.getByAltText("flagEnglish");
    expect(flagElement).toBeInTheDocument();
  });

  test("should render the SelectLanguageButton component correctly", () => {
    const mockSetLang = jest.fn();
    const { getByTestId } = render(
      <SelectLanguageButton
        lang="en"
        setLang={mockSetLang}
      />
    );

    const selectLanguageButtonElement = getByTestId("select-language-button");
    expect(selectLanguageButtonElement).toBeInTheDocument();
  });

  test("should show the correct language label on the checkbox", async () => {
    const mockSetLang = jest.fn();

    const { getByText } = await render(
      <SelectLanguageButton
        lang="en"
        setLang={mockSetLang}
      />
    );
    waitFor(() => {
      const englishLabelElement = getByText("english");
      expect(englishLabelElement).toBeInTheDocument();
    });
  });

  test("opens the language selection modal when clicked", async () => {
    Preferences.get = jest.fn().mockResolvedValue({ value: "ka" });
    render(
      <SelectLanguageButton
        lang={LanguageType.Georgian}
        setLang={() => {}}
      />
    );

    userEvent.click(screen.getByTestId("select-language-button"));
    // Use waitFor to wait for the component to update
    await waitFor(() => {
      expect(screen.getByText("georgian")).toBeInTheDocument();
      expect(Preferences.get).toHaveBeenCalledWith({ key: "language" });
      // Verify that the language selection modal is displayed
      const modalElement = screen.getByTestId("select-language-modal");
      expect(modalElement).toBeInTheDocument();
    });
  });

  test("changes the language when a new language is selected", async () => {
    const setLang = jest.fn(); // Mock the setLang function
    Preferences.get = jest.fn().mockResolvedValue({ value: "en" });
    render(
      <SelectLanguageButton
        lang={LanguageType.English}
        setLang={setLang}
      />
    );
    act(() => {
      userEvent.click(screen.getByTestId("select-language-button"));
    });
    // Wait for asynchronous actions to complete
    await waitFor(() => {
      userEvent.click(screen.getByText("georgian"));
      // Verify that setLang is called with the selected language
      expect(setLang).toHaveBeenCalledWith(LanguageType.Georgian);
      expect(Preferences.get).toHaveBeenCalledWith({ key: "language" });
      expect(Preferences.set).toHaveBeenCalledWith({
        key: "language",
        value: "ka",
      });
    });
  });

  test("should change language and save it in localStorage when a language is selected", () => {
    render(<SelectLanguageButton />);
    waitFor(() => {
      const georgianItem = screen.getByTestId("item-change-language-ka");
      fireEvent.click(georgianItem);
      expect(localStorage.getItem("language")).toBe("ka");
      const englishItem = screen.getByTestId("item-change-language-en");
      fireEvent.click(englishItem);
      expect(localStorage.getItem("language")).toBe("en");
    });
  });
});

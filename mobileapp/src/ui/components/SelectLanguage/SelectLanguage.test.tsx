import { Preferences } from "@capacitor/preferences"; // Import from mock
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { SelectLanguage } from "./SelectLanguage";
jest.mock("../../../i18n", () => ({
  changeLanguage: jest.fn(),
}));
jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    set: jest.fn(),
    get: jest.fn().mockResolvedValue({ value: "ka" }),
  },
}));
describe("SelectLanguage component", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders the SelectLanguage component", () => {
    render(<SelectLanguage />);
    const selectLanguageElement = screen.getByTestId("select-language");
    expect(selectLanguageElement).toBeInTheDocument();
  });

  test("opens the language selection modal when clicked", () => {
    render(<SelectLanguage />);
    const selectLanguageElement = screen.getByTestId("select-language");
    fireEvent.click(selectLanguageElement);
    const modalElement = screen.getByTestId("select-language-modal");
    expect(modalElement).toBeInTheDocument();
  });

  test("changes the language when an option is selected", async () => {
    render(<SelectLanguage />);
    const selectLanguageElement = screen.getByTestId("select-language");
    fireEvent.click(selectLanguageElement);

    const englishOptionElement = screen.getByTestId("item-change-language-en");

    fireEvent.click(englishOptionElement);

    // Use waitFor to wait for the component to update
    await waitFor(() => {
      const selectedLanguageText = screen.getByText("english");
      expect(selectedLanguageText).toBeInTheDocument();
      expect(Preferences.set).toHaveBeenCalledWith({
        key: "language",
        value: "en",
      });
    });
  });
});

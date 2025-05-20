import { fireEvent, render, screen } from "@testing-library/react";
import { SelectLanguage } from "./SelectLanguage";
import { LanguageType } from "./types";

describe("SelectLanguage", () => {
  beforeEach(() => {
    Object.defineProperty(navigator, "language", {
      value: "ka",
      configurable: true,
    });
    window.localStorage.removeItem("language");
  });
  test("should render the select language", async () => {
    render(<SelectLanguage />);

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

  test("should render the select language only icon", async () => {
    render(<SelectLanguage isOnlySelect={true} />);

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    const optionLanguageEn = screen.getByTestId(LanguageType.English);
    expect(optionLanguageEn).toBeInTheDocument();
    fireEvent.click(optionLanguageEn);
    expect(window.localStorage.getItem("language")).toBe("en");
    expect(screen.getAllByText("English").length).toEqual(1);
    expect(screen.getAllByText("Georgian").length).toEqual(1);
    const optionLanguageGe = screen.getByTestId(LanguageType.Georgian);
    expect(optionLanguageGe).toBeInTheDocument();
    fireEvent.click(optionLanguageGe);
    expect(window.localStorage.getItem("language")).toBe("ka");
  });
});

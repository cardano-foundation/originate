import { act, fireEvent, render, screen } from "@testing-library/react";
import { PrivacyPolicy } from "./PrivacyPolicy";
import { LanguageType } from "../../components/SelectLanguage/types";

describe("ViewPrivacyPolicy test", () => {
  beforeEach(() => {
    Object.defineProperty(navigator, "language", {
      value: "ka",
      configurable: true,
    });
  });
  test("should render PrivacyPolicy component", async () => {
    act(() => {
      render(<PrivacyPolicy />);
    });

    const selectLanguage = screen.getByTestId("select-language");
    expect(selectLanguage).toBeInTheDocument();
    fireEvent.mouseDown(selectLanguage.children[0]);
    fireEvent.click(selectLanguage);
    const optionLanguageEn = screen.getByTestId(LanguageType.English);
    expect(optionLanguageEn).toBeInTheDocument();
    fireEvent.click(optionLanguageEn);
    expect(screen.getAllByText("English").length).toEqual(1);
    expect(screen.getAllByText("Georgian").length).toEqual(1);
  });

  test("should render the select language", async () => {
    render(<PrivacyPolicy />);

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
});

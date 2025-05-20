import { act, fireEvent, render, screen } from "@testing-library/react";
import { TermsConditionsMobile } from "./TermsConditionsMobile";
import { LanguageType } from "../../components/SelectLanguage/types";
import { MemoryRouter } from "react-router-dom";

describe("ViewTermsConditions test", () => {
  beforeEach(() => {
    Object.defineProperty(navigator, "language", {
      value: "ka",
      configurable: true,
    });
  });
  test("should render TermsConditions component", async () => {
    act(() => {
      render(
        <MemoryRouter>
          <TermsConditionsMobile />
        </MemoryRouter>
      );
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
    render(
      <MemoryRouter>
        <TermsConditionsMobile />
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
});

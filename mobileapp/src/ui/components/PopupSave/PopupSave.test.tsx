import { act, fireEvent, render, screen } from "@testing-library/react";
import { PopupSave } from "./PopupSave";

const handleSaveAndContinue = jest.fn();
const handleApprove = jest.fn();
const setIsShowPopupSave = jest.fn();

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

describe("Popup Save", () => {
  test("handleSaveAndContinue should be called when click save and continue button", async () => {
    await act(async () => {
      render(
        <PopupSave
          isOpen
          onLeave={() => setIsShowPopupSave(false)}
          onSave={handleSaveAndContinue}
          onApprove={handleApprove}
        />
      );
    });

    const buttonSaveLater = screen.getByTestId("button-save-later");
    fireEvent.click(buttonSaveLater);
    expect(handleSaveAndContinue).toBeCalled();
  });

  test("handleApprove should be called when click review and approve button", async () => {
    render(
      <PopupSave
        isOpen
        onLeave={() => setIsShowPopupSave(false)}
        onSave={handleSaveAndContinue}
        onApprove={handleApprove}
      />
    );

    const buttonReviewAndApprove = screen.getByTestId(
      "button-review-and-approve"
    );
    fireEvent.click(buttonReviewAndApprove);
    expect(handleApprove).toBeCalled();
  });

  test("PopupSave renders correctly when in range scanning", () => {
    render(
      <PopupSave
        isOpen
        onLeave={() => setIsShowPopupSave(false)}
        onSave={handleSaveAndContinue}
        onApprove={handleApprove}
        subTitle="mockSubtitle"
        isRangeScan
      />
    );

    const buttonReviewAndApprove = screen.queryByTestId(
      "button-review-and-approve"
    );
    const buttonRescanRange = screen.getByTestId("button-rescan-range");
    const buttonApproveLot = screen.getByTestId("button-approve-range-scan");
    const title = screen.getByText("bottleListTitleApprove");
    const subTitle = screen.getByText("mockSubtitle");
    expect(buttonReviewAndApprove).toBeNull();
    expect(buttonRescanRange).toBeInTheDocument();
    expect(buttonApproveLot).toBeInTheDocument();
    expect(title).toBeInTheDocument();
    expect(subTitle).toBeInTheDocument();
  });
});

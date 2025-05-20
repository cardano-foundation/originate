import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { PopupCancelScan } from "./PopupCancelScan";

describe("PopupCancelScan", () => {
  test("should render the modal when isOpen is true", async () => {
    const mockDataModal = {
      isOpen: true,
      title: "Test Title",
      textConfirm: "Confirm",
      textCancel: "Cancel",
      onConfirm: jest.fn(),
      onCancel: jest.fn(),
    };

    render(
      <PopupCancelScan
        isOpen={mockDataModal.isOpen}
        title={mockDataModal.title}
        textConfirm={mockDataModal.textConfirm}
        textCancel={mockDataModal.textCancel}
        onConfirm={mockDataModal.onConfirm}
        onCancel={mockDataModal.onCancel}
      />
    );

    const modalElement = screen.getByTestId("popup-cancel-scan");
    expect(modalElement).toBeInTheDocument();
    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Confirm")).toBeInTheDocument();

    const confirmButton = screen.getByTestId("confirm-button");
    fireEvent.click(confirmButton);
    expect(mockDataModal.onConfirm).toHaveBeenCalled();

    const cancelButton = screen.getByTestId("cancel-button");
    fireEvent.click(cancelButton);
    expect(mockDataModal.onCancel).toHaveBeenCalled();
  });
});

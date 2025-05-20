import { fireEvent, render, screen, waitFor } from "@testing-library/react";

import { ModalConfirm } from "./ModalConfirm";

describe("ModalConfirm", () => {
  test("should render the modal when isOpen is true", async () => {
    const mockDataModal = {
      isOpen: true,
      title: "Carnado",
      textConfirm: "Scan Cert",
      onConfirm: jest.fn(),
      onCancel: jest.fn(),
    };

    render(
      <ModalConfirm
        isOpen={true}
        title={mockDataModal.title}
        textConfirm={mockDataModal.textConfirm}
        onCancel={mockDataModal.onCancel}
        onConfirm={mockDataModal.onConfirm}
      />
    );

    await waitFor(() => {
      const modalElement = screen.getByTestId("modal-confirm-scan");

      expect(modalElement).toBeInTheDocument();
      expect(screen.getByText("Carnado")).toBeInTheDocument();
      expect(screen.getByText("Scan Cert")).toBeInTheDocument();

      const confirmButton = screen.getByTestId("confirm-button");
      fireEvent.click(confirmButton);

      expect(mockDataModal.onConfirm).toHaveBeenCalled();

      const cancelButton = screen.getByTestId("cancel-button");
      fireEvent.click(cancelButton);
      expect(mockDataModal.onCancel).toHaveBeenCalled();
    });
  });
  test("renders Scan By Bottle button when onScanByBottle is provided", () => {
    const title = "Test Title";
    const textConfirm = "Confirm Text";
    const isOpen = true;
    const onConfirm = jest.fn();
    const onScanByBottle = jest.fn();

    render(
      <ModalConfirm
        title={title}
        textConfirm={textConfirm}
        isOpen={isOpen}
        onConfirm={onConfirm}
        onScanByBottle={onScanByBottle}
      />
    );

    expect(screen.queryByTestId("scan-by-bottle-button")).toBeInTheDocument();

    // Simulate button click
    fireEvent.click(screen.getByTestId("scan-by-bottle-button"));

    // Verify that the provided callback function is called
    expect(onScanByBottle).toHaveBeenCalled();
  });
});

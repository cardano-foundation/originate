import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { TYPE_MODAL } from "../../constants/business";
import { ModalComponent } from "../../layout/CustomModal";

describe("ModalComponent test", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  test("check show content modal", () => {
    const handleConfirm = jest.fn();
    const handleCancel = jest.fn();
    const onClose = jest.fn();

    render(
      <ModalComponent
        isModal={true}
        title={"titleApproveAllRows"}
        description={"descriptionApproveAllRows"}
        textConfirm={"confirmApproveAllRows"}
        type={TYPE_MODAL.APPROVE}
        onClose={onClose}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    );
    const titleModal = screen.getByText("titleApproveAllRows");
    const descriptionModal = screen.getByText("descriptionApproveAllRows");
    const textConfirmModal = screen.getByText("confirmApproveAllRows");
    expect(titleModal).toBeInTheDocument();
    expect(descriptionModal).toBeInTheDocument();
    expect(textConfirmModal).toBeInTheDocument();
    fireEvent.click(textConfirmModal);
    expect(handleConfirm).toHaveBeenCalled();
    const cancelModal = screen.getByText("cancel");
    fireEvent.click(cancelModal);
    expect(handleCancel).toHaveBeenCalled();
  });
});

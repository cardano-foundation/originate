import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { BulkComponent } from "./BulkComponent";
import { ROLE_SYSTEM } from "../../constants/auth";
import { TYPE_MODAL } from "../../constants/business";
import { ModalComponent } from "../../layout/CustomModal";

describe("BulkComponent test", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  test("check show content with role Admin", () => {
    const handleDelete = jest.fn();
    const handleFinalise = jest.fn();
    const handleApprove = jest.fn();

    render(
      <BulkComponent
        selected={["2"]}
        role={ROLE_SYSTEM.ADMIN}
        onDelete={handleDelete}
        onFinalise={handleFinalise}
        onApprove={handleApprove}
      />
    );
    const bulkAction = screen.getByTestId("bulk-action-menu");
    expect(bulkAction).not.toBeDisabled();
    fireEvent.click(bulkAction);
    const bulkFinaliseAll = screen.getByTestId("button-finalise-all");
    expect(bulkFinaliseAll).toBeInTheDocument();
    fireEvent.click(bulkFinaliseAll);
    expect(handleFinalise).toHaveBeenCalled();
  });

  test("check show content with role Winery", () => {
    const handleDelete = jest.fn();
    const handleFinalise = jest.fn();
    const handleApprove = jest.fn();
    const handleConfirm = jest.fn();
    const handleCancel = jest.fn();
    const onClose = jest.fn();

    render(
      <BulkComponent
        selected={["2"]}
        role={ROLE_SYSTEM.WINERY}
        onDelete={handleDelete}
        onFinalise={handleFinalise}
        onApprove={handleApprove}
      />
    );
    const bulkAction = screen.getByTestId("button-approve-all");
    expect(bulkAction).not.toBeDisabled();
    fireEvent.click(bulkAction);
    expect(handleApprove).toHaveBeenCalledWith();
    render(
      <ModalComponent
        isModal={true}
        title={"titleApproveOneRow"}
        description={"descriptionApproveOneRow"}
        textConfirm={"titleApproveOneRow"}
        type={TYPE_MODAL.APPROVE}
        onClose={onClose}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    );
    const iconClose = screen.getByTestId("icon-close");
    expect(iconClose).toBeInTheDocument();
  });
});

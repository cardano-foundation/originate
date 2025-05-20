import React from "react";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { ModalUpload } from "./ModalUpload";
import { UploadService } from "../../../services";
import { ALERT_TYPE } from "../../layout/CustomAlert/types";
import { convertErrorMessage } from "../../utils/MessageUtils";

const onShowMessage = jest.fn();
const onOk = jest.fn();
const onCancel = jest.fn();

jest.mock("../../../services", () => ({
  UploadService: {
    uploadWinerySCM: jest.fn().mockResolvedValue({}),
    uploadBottleMapping: jest.fn().mockResolvedValue({}),
  },
}));

describe("ModalUpload test", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test("should show message function be call when upload file successfully", async () => {
    render(
      <ModalUpload
        isOpen
        wineryId={"1234"}
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });

    await act(async () => {
      render(
        <ModalUpload
          wineryId={"1234"}
          isOpen
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });

    const backdropElement = document.querySelector(".MuiBackdrop-root");
    if (backdropElement) {
      fireEvent.click(backdropElement);
      expect(onCancel).not.toHaveBeenCalled();
    }

    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.success,
      "uploadSuccess"
    );
  });

  test("should show message function be call when drop file to upload file successfully", async () => {
    render(
      <ModalUpload
        isOpen
        wineryId={"1234"}
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const dropArea = screen.getByTestId("drop-area");
    fireEvent.drop(dropArea, {
      dataTransfer: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });
    await act(async () => {
      render(
        <ModalUpload
          isOpen
          wineryId={"1234"}
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });

    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.success,
      "uploadSuccess"
    );
  });

  test("should file name not display when drop multiple file", async () => {
    render(
      <ModalUpload
        isOpen
        wineryId={"1234"}
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const dropArea = screen.getByTestId("drop-area");
    fireEvent.drop(dropArea, {
      dataTransfer: {
        files: [
          new File(["(⌐□_□)"], "test.csv", { type: "text/csv" }),
          new File(["(⌐□_□)"], "1.csv", { type: "text/csv" }),
        ],
      },
    });

    const fileNameElement = screen.getByTestId("file-name");
    expect(fileNameElement.children[0]).not.toBeDefined();
    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.error,
      "uploadMultipleFile"
    );
  });

  test("should show message when drop file wrong format", async () => {
    render(
      <ModalUpload
        isOpen
        wineryId={"1234"}
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const dropArea = screen.getByTestId("drop-area");
    fireEvent.drop(dropArea, {
      dataTransfer: {
        files: [new File(["(⌐□_□)"], "test.txt", { type: "text/plain" })],
      },
    });

    expect(onShowMessage).toHaveBeenCalledWith(ALERT_TYPE.error, "wrongFormat");
  });

  test("should show message function be call when upload file error", async () => {
    const responseError = {
      data: {
        meta: {
          code: "2",
        },
      },
    };
    const callUploadService = UploadService.uploadWinerySCM as jest.Mock;
    callUploadService.mockRejectedValue(responseError);

    render(
      <ModalUpload
        isOpen
        title="scmDataTitle"
        wineryId="1234"
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });

    await act(async () => {
      render(
        <ModalUpload
          isOpen
          title="scmDataTitle"
          wineryId="1234"
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });
    expect(callUploadService).toBeCalled();
    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.error,
      convertErrorMessage("2")
    );
  });

  test("mock test call function drag", async () => {
    render(
      <ModalUpload
        isOpen
        wineryId={"1234"}
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const dropArea = screen.getByTestId("drop-area");
    fireEvent.dragEnter(dropArea);
  });

  test("should show message success when upload bottle mapping with valid file", async () => {
    render(
      <ModalUpload
        wineryId="1234"
        title={"bottleMappingTitle"}
        isOpen
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });

    await act(async () => {
      render(
        <ModalUpload
          wineryId="1234"
          title={"bottleMappingTitle"}
          isOpen
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });

    expect(UploadService.uploadBottleMapping).toHaveBeenCalled();
    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.success,
      "uploadSuccess"
    );
  });

  test("should show message error when upload bottle mapping with invalid file", async () => {
    UploadService.uploadBottleMapping = jest.fn().mockRejectedValue({
      data: {
        meta: {
          code: "3",
        },
      },
    });
    render(
      <ModalUpload
        wineryId="1234"
        title={"bottleMappingTitle"}
        isOpen
        onShowMessage={onShowMessage}
        onOk={onOk}
        onCancel={onCancel}
      />
    );
    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });

    await act(async () => {
      render(
        <ModalUpload
          wineryId="1234"
          title={"bottleMappingTitle"}
          isOpen
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });

    expect(UploadService.uploadBottleMapping).toHaveBeenCalled();
    expect(onShowMessage).toHaveBeenCalledWith(
      ALERT_TYPE.error,
      convertErrorMessage("3")
    );
  });

  test("should show message function be call when upload winery file with error not found api", async () => {
    const callUploadService = UploadService.uploadWinerySCM as jest.Mock;
    callUploadService.mockRejectedValue({
      status: 404,
      data: {},
    });

    act(() => {
      render(
        <ModalUpload
          isOpen
          title="scmDataTitle"
          wineryId="1234"
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });

    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });
    await waitFor(() => {
      expect(callUploadService).toBeCalled();
      expect(onShowMessage).toHaveBeenCalledWith(
        ALERT_TYPE.error,
        "somethingWentWrongPleaseTryAgain"
      );
    });
  });

  test("should show message success when upload bottle mapping with valid file", async () => {
    UploadService.uploadBottleMapping = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    act(() => {
      render(
        <ModalUpload
          wineryId="1234"
          title={"bottleMappingTitle"}
          isOpen
          onShowMessage={onShowMessage}
          onOk={onOk}
          onCancel={onCancel}
        />
      );
    });
    const inputElement = screen.getByTestId("input-upload-file");
    fireEvent.change(inputElement, {
      target: {
        files: [new File(["(⌐□_□)"], "test.csv", { type: "text/csv" })],
      },
    });
    await waitFor(() => {
      expect(UploadService.uploadBottleMapping).toHaveBeenCalled();
      expect(onShowMessage).toHaveBeenCalledWith(
        ALERT_TYPE.error,
        "somethingWentWrongPleaseTryAgain"
      );
    });
  });
});

import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { ModalToastBulk } from "./ModalToastBulk";

const dataMock = {
  textHeader: "5 rows successfully finalised",
  textError: "15 rows not finalised",
  dataError: [
    {
      id: "71a7f9d0",
      status: "Not found",
    },
  ],
  isError: false,
};

describe("ModalToastBulk test", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  test("check show content modal", () => {
    const onClose = jest.fn();

    render(
      <ModalToastBulk
        isModal={true}
        data={dataMock}
        onClose={onClose}
      />
    );
    const textHeader = screen.getByText(dataMock.textHeader);
    const textError = screen.getByText(dataMock.textError);
    const errorContent = screen.getByTestId("error-container");
    expect(textHeader).toBeInTheDocument();
    expect(textError).toBeInTheDocument();
    expect(errorContent).toBeInTheDocument();
    fireEvent.click(errorContent);
    const contentDetail = screen.getAllByTestId("content-detail");
    expect(contentDetail.length).toEqual(dataMock.dataError.length);
  });

  test("handle test call function close modal toast bulk when click button ok", async () => {
    const onClose = jest.fn();

    render(
      <ModalToastBulk
        isModal={true}
        data={dataMock}
        onClose={onClose}
      />
    );
    const button = screen.getByTestId("btn-close");
    expect(button).toBeInTheDocument();
    fireEvent.click(button);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  test("handle test close modal toast bulk when click outside", async () => {
    const onClose = jest.fn();

    render(
      <ModalToastBulk
        isModal={true}
        data={dataMock}
        onClose={onClose}
      />
    );
    const backdropElement = document.querySelector(".MuiBackdrop-root");
    if (backdropElement) {
      fireEvent.click(backdropElement);
      expect(onClose).toHaveBeenCalledTimes(1);
    }
  });
});

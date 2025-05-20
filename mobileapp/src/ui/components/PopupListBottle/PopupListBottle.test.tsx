import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { useTranslation } from "react-i18next";
import { BottleListResponse } from "../../common/responses";
import { ListBottleType } from "../../common/types";
import { PopupListBottle } from "./PopupListBottle";

const handleLeaveListBottle = jest.fn();
const handleApprove = jest.fn();
const handleDelete = jest.fn();

jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
}));

jest.mock("react-i18next", () => ({
  iniReactI18next: jest.fn(),
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string, other?: { [key: string]: string }) =>
      key + Object.values(other || {}),
  }),
}));

let dataBottle: BottleListResponse = {
  bottles: [
    {
      id: "1",
      reelNumber: 1,
      lotId: "12",
      sequentialNumber: 1,
      certificateId: "1",
    },
  ],
  lotId: "",
  certId: "",
};

describe("ScanningItem", () => {
  test("renders correct title", async () => {
    render(
      <PopupListBottle
        title={"Scanned so far"}
        subTitle={
          "Delete any entries you think you’ve gotten wrong and rescan a code."
        }
        data={dataBottle}
        isOpen
        onLeave={handleLeaveListBottle}
        onDelete={handleDelete}
        onApprove={handleApprove}
      />
    );
    const title: HTMLImageElement = screen.getByText("Scanned so far");
    expect(title).toBeInTheDocument();
    const subTitle: HTMLImageElement = screen.getByText(
      "Delete any entries you think you’ve gotten wrong and rescan a code."
    );
    expect(subTitle).toBeInTheDocument();
  });

  test("should popup confirm delete display when click icon trash", async () => {
    act(() =>
      render(
        <PopupListBottle
          title={"Scanned so far"}
          subTitle={
            "Delete any entries you think you’ve gotten wrong and rescan a code."
          }
          data={dataBottle}
          isOpen
          onLeave={handleLeaveListBottle}
          onDelete={handleDelete}
          onApprove={handleApprove}
        />
      )
    );
    const iconDelete: HTMLImageElement =
      screen.getByTestId("icon-delete-bottle");
    fireEvent.click(iconDelete);
    const popupConfirm: HTMLImageElement =
      screen.getByTestId("modal-confirm-scan");
    expect(popupConfirm).toBeInTheDocument();
  });

  test("should function delete called when click icon trash", async () => {
    act(() =>
      render(
        <PopupListBottle
          title={"Scanned so far"}
          subTitle={
            "Delete any entries you think you’ve gotten wrong and rescan a code."
          }
          data={dataBottle}
          isOpen
          onLeave={handleLeaveListBottle}
          onDelete={handleDelete}
          onApprove={handleApprove}
        />
      )
    );
    const iconDelete: HTMLImageElement =
      screen.getByTestId("icon-delete-bottle");
    fireEvent.click(iconDelete);
    fireEvent.click(screen.getByTestId("confirm-button"));
    expect(handleDelete).toBeCalled();
  });

  test("should confirm approve text", async () => {
    dataBottle = {
      bottles: [
        {
          id: "1",
          reelNumber: 1,
          lotId: "12",
          sequentialNumber: 1,
          certificateId: "1",
        },
      ],
      lotId: "",
      certId: "",
    };
    act(() =>
      render(
        <PopupListBottle
          title={"Scanned so far"}
          subTitle={
            "Delete any entries you think you’ve gotten wrong and rescan a code."
          }
          data={dataBottle}
          isOpen
          onLeave={handleLeaveListBottle}
          onDelete={handleDelete}
          onApprove={handleApprove}
          type={ListBottleType.APPROVE}
        />
      )
    );
    const buttonApprove = screen.getByTestId("button-approve-lot");
    fireEvent.click(buttonApprove);
    await waitFor(() => {
      expect(screen.getByTestId("modal-confirm-scan")).toBeInTheDocument();
      expect(screen.getByText("titleApproveLot")).toBeInTheDocument();
    });
  });

  test("should trigger onLeave callback when down icon button is clicked", async () => {
    render(
      <PopupListBottle
        title={"Scanned so far"}
        subTitle={
          "Delete any entries you think you’ve gotten wrong and rescan a code."
        }
        data={dataBottle}
        isOpen
        onLeave={handleLeaveListBottle}
        onDelete={handleDelete}
        onApprove={handleApprove}
      />
    );
    fireEvent.click(screen.getByTestId("bottle-list-closeicon"));
    await waitFor(() => {
      expect(handleLeaveListBottle).toHaveBeenCalled();
    });
  });
});

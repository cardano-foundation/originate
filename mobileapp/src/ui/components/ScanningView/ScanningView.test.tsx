import { act, render, fireEvent, screen } from "@testing-library/react";
import { ScanningView } from "./ScanningView";

describe("ScanningView", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });
  test("ScanningView renders correctly with header and actions", () => {
    const onExitMock = jest.fn();
    const listScannedActionMock = jest.fn();
    const saveActionMock = jest.fn();

    act(() => {
      render(
        <ScanningView
          activeScan={true}
          totalScanned={10}
          currentLot={{
            certId: "1",
            certNumber: "GE-123456",
            lotId: "1234567AB89",
            certType: "TYPE-001"
          }}
          showHeader={true}
          disableHeaderAction={false}
          onExit={onExitMock}
          listScannedAction={listScannedActionMock}
          saveAction={saveActionMock}
        />
      );
    });

    expect(screen.getByTestId("btn-back-scanning-view")).toBeInTheDocument();
    expect(screen.getByAltText("backIcon")).toBeInTheDocument();
    expect(screen.getByText("GE-123456")).toBeInTheDocument();
    expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
    expect(screen.getByAltText("listBullet")).toBeInTheDocument();
    expect(screen.getByAltText("boxScan")).toBeInTheDocument();
    expect(screen.getByAltText("save")).toBeInTheDocument();

    fireEvent.click(screen.getByTestId("btn-back-scanning-view"));
    expect(onExitMock).toHaveBeenCalled();

    fireEvent.click(screen.getByAltText("listBullet"));
    expect(listScannedActionMock).toHaveBeenCalled();

    fireEvent.click(screen.getByAltText("save"));
    expect(saveActionMock).toHaveBeenCalled();
  });

  test("ScanningView renders correctly without header and bottom actions", () => {
    act(() => {
      render(
        <ScanningView
          activeScan={true}
          onExit={jest.fn()}
        />
      );
    });

    expect(screen.queryByAltText("listBullet")).not.toBeInTheDocument();
    expect(screen.queryByAltText("save")).not.toBeInTheDocument();
    expect(screen.queryByAltText("check")).not.toBeInTheDocument();
    expect(screen.getByText("checkBottleStatus")).toBeInTheDocument();
    expect(screen.getByAltText("boxScan")).toBeInTheDocument();
  });

  test("ScanningView renders correctly with scanned icon", () => {
    act(() => {
      render(
        <ScanningView
          activeScan={true}
          showHeader={true}
          onExit={jest.fn()}
          scannedStatus={true}
        />
      );
    });

    expect(screen.queryByAltText("listBullet")).toBeInTheDocument();
    expect(screen.queryByAltText("save")).toBeInTheDocument();
    expect(screen.queryByAltText("check")).toBeInTheDocument();
  });

  test("ScanningView renders correctly when scanning range", () => {
    act(() => {
      render(
        <ScanningView
          activeScan={true}
          totalScanned={0}
          currentLot={{
            certId: "1",
            certNumber: "GE-123456",
            lotId: "1234567AB89",
            certType: "TYPE-001"
          }}
          showHeader={true}
          disableHeaderAction={false}
          onExit={jest.fn()}
          listScannedAction={jest.fn()}
          saveAction={jest.fn()}
          scannedStatus={true}
          isRangeScan={true}
        />
      );
    });

    expect(screen.getByText("scannedStatus")).toBeInTheDocument();
    expect(screen.getByAltText("check")).toBeInTheDocument();
    expect(screen.getByText("GE-123456")).toBeInTheDocument();
    expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
    expect(screen.getByText("firstRangeScan")).toBeInTheDocument();
    expect(screen.queryByTestId("button-save-scan")).not.toBeInTheDocument();
  });

  test("ScanningView renders correctly when scanning range with the first bottle scanned", () => {
    act(() => {
      render(
        <ScanningView
          activeScan={true}
          totalScanned={1}
          showHeader={true}
          currentLot={{
            certId: "1",
            certNumber: "GE-123456",
            lotId: "1234567AB89",
            certType: "TYPE-001"
          }}
          disableHeaderAction={false}
          onExit={jest.fn()}
          listScannedAction={jest.fn()}
          saveAction={jest.fn()}
          scannedStatus={true}
          isRangeScan={true}
        />
      );
    });

    expect(screen.getByText("scannedStatus")).toBeInTheDocument();
    expect(screen.getByAltText("check")).toBeInTheDocument();
    expect(screen.getByText("GE-123456")).toBeInTheDocument();
    expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
    expect(screen.queryByText("lastRangeScan")).toBeInTheDocument();
    expect(screen.queryByText("firstRangeScan")).not.toBeInTheDocument();
    expect(screen.queryByTestId("button-save-scan")).not.toBeInTheDocument();
  });
});

import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { BackendAPI } from "../../../services";
import { CertProductView } from "./CertProductView";
import { BottleByCertLot, RangeScanSaveStatus } from "../../hooks/types";
import { CertProductDetails } from "../../components";

jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    get: jest.fn().mockResolvedValue({ value: "mockedToken" }),
    set: jest.fn().mockResolvedValue(undefined),
  },
}));

JSON.parse = jest.fn().mockReturnValue({
  locale: "en",
});

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

const mockStopScan = jest.fn();
const mockStartScanning = jest.fn();
const mockCurrentScanned: BottleByCertLot[] = [
  {
    id: "bottle1",
    lotId: "1",
    sequentialNumber: 1,
    reelNumber: 1,
    certificateId: "GE-123456",
  },
  {
    id: "bottle2",
    lotId: "2",
    sequentialNumber: 1,
    reelNumber: 1,
    certificateId: "GE-123453",
  },
];

const mockCurrentLot: CertProductDetails = {
  certId: "certId",
  lotId: "1234567AB88",
  certType: "TYPE-002",
  certNumber: "GE-123459",
};
const mockSortableScannedRange: BottleByCertLot[] = [
  {
    id: "1",
    lotId: "1234567AB89",
    certificateId: "GE-123456",
    reelNumber: 1,
    sequentialNumber: 1,
  },
  {
    id: "2",
    lotId: "1234567AB89",
    certificateId: "GE-123456",
    reelNumber: 1,
    sequentialNumber: 2,
  },
];
const mockListRemoveRef = { current: [] };
let mockPopupConfirmProps = jest.fn().mockReturnValue({
  title: "confirmTitle",
  cancelAction: jest.fn(),
});
const mockSetScanningRangeIssue = jest.fn();
const mockSetUnableToSave = jest.fn();
const mockSetConfirmApproveRangeScan = jest.fn();
const mockSetConfirmRescanLot = jest.fn();
let mockIsPopupConfirmOpen = false;
let mockConfirmRescanLot = false;
let mockConfirmApproveRangeScan = false;
let mockScanningRangeIssue: RangeScanSaveStatus = { error: false };
jest.mock("../../hooks/useScanner", () => ({
  useScanner: jest.fn(() => ({
    checkPermission: jest.fn().mockResolvedValue(true),
    startScanning: mockStartScanning,
    stopScan: mockStopScan,
    setCurrentLot: jest.fn(),
    currentLot: mockCurrentLot,
    setShowModalError: jest.fn(),
    currentScanned: mockCurrentScanned,
    currentScannedRef: { current: mockCurrentScanned },
    winery: { wineryId: "1234" },
    listRemoveRef: mockListRemoveRef,
    isRangeScan: true,
    setUnableToSaveStatus: mockSetUnableToSave,
    popupConfirmProps: mockPopupConfirmProps,
    setIsRangeScan: jest.fn(),
    setRangeScanningStatus: mockSetScanningRangeIssue,
    sortableScannedRange: mockSortableScannedRange,
    setConfirmApproveRangeScan: mockSetConfirmApproveRangeScan,
    setConfirmRescanLot: mockSetConfirmRescanLot,
    isPopupConfirmOpen: mockIsPopupConfirmOpen,
    confirmRescanLot: mockConfirmRescanLot,
    confirmApproveRangeScan: mockConfirmApproveRangeScan,
    rangeScanningStatus: mockScanningRangeIssue,
  })),
}));

jest.mock("react-router-dom", () => ({
  useHistory: jest.fn().mockReturnValue({
    goBack: jest.fn(),
    location: {
      state: {
        id: "certNum",
        certificateNumber: "GE-123456",
        certificateType: "TYPE-001",
        lotEntries: [
          {
            lotId: "1234567AB89",
            scanningStatus: "SCANNING",
          },
          {
            lotId: "2345678CD90",
            scanningStatus: "APPROVED",
          },
        ],
      },
    },
  }),
}));

jest.mock("@capacitor-community/barcode-scanner", () => ({
  BarcodeScanner: {
    pauseScanning: jest.fn(),
    resumeScanning: jest.fn(),
    startScanning: jest.fn(),
    stopScan: jest.fn(),
    hideBackground: jest.fn(),
    checkPermission: jest.fn().mockResolvedValue({ granted: true }),
  },
  SupportedFormat: {
    QR_CODE: "QR_CODE",
  },
}));

jest.mock("../../../services", () => ({
  BackendAPI: {
    saveAndContinue: jest.fn().mockResolvedValue({}),
    saveScanRangeApi: jest.fn().mockResolvedValue({}),
  },
}));

describe("Cert Product View - Range Scan", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("should call saveScanRangeApi when save scanning range", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(BackendAPI.saveScanRangeApi).toBeCalledWith(
        "1234",
        "1234567AB88",
        "certId",
        {
          startRange: "1",
          endRange: "2",
          isSequentialNumber: false,
          finalise: false,
        }
      );
    });
  });

  test("modal confirm approve state should be set to true after click approve scanning range", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-approve-range-scan"));
    });
    await waitFor(() => {
      expect(mockSetConfirmApproveRangeScan).toBeCalledWith(true);
    });
    // @TODO - foconnor: We can't test the API call properly as there's state fragmented across
    // too many components so becomes an integration test.
  });

  test("scanning view should be reset after confirm reset scan", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-rescan-range"));
      expect(mockSetConfirmRescanLot).toBeCalledWith(true);
    });
    // Same here.
  });

  test("should show popup error when saveScanRangeApi return error 409", async () => {
    mockPopupConfirmProps = jest.fn().mockReturnValue({
      title: "scanningRangeIssue",
      cancelAction: jest.fn(),
    });
    BackendAPI.saveScanRangeApi = jest
      .fn()
      .mockRejectedValue({ response: { status: 409 } });
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
      expect(mockSetScanningRangeIssue).toBeCalledWith({ error: true, approve: false });
    });
    // Same here.
  });

  test("should show popup error when saveScanRangeApi return error 400", async () => {
    mockPopupConfirmProps = jest.fn().mockReturnValue({
      title: "scanningRangeIssue",
      cancelAction: jest.fn(),
    });
    BackendAPI.saveScanRangeApi = jest
      .fn()
      .mockRejectedValue({ response: { status: 400 } });
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
      expect(mockSetUnableToSave).toBeCalled();
    });
  });
});

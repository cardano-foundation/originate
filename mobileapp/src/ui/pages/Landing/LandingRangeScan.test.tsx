import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { BarcodeScanner } from "@capacitor-community/barcode-scanner";
import { i18n } from "../../../i18n";
import { BackendAPI } from "../../../services/backendApi";
import { Landing } from "./Landing";
import { BottleByCertLot, RangeScanSaveStatus } from "../../hooks/types";
import { CertProductDetails } from "../../components";
import { BottleType } from "../../common/types";

jest.mock("../../../utils", () => ({
  PREFIX_BOTTLE_ID: "https://nwxn.qr1.ch/",
  sleep: jest.fn(),
  convertName: jest.fn(),
}));

jest.mock("@capacitor-community/barcode-scanner", () => ({
  SupportedFormat: {
    QR_CODE: "qr_code",
  },
  BarcodeScanner: {
    checkPermission: jest.fn().mockResolvedValue({ granted: true }),
    prepare: jest.fn().mockResolvedValue(undefined),
    stopScan: jest.fn().mockResolvedValue(undefined),
    showbackground: jest.fn().mockResolvedValue(undefined),
    startScan: jest.fn(),
    startScanning: jest.fn().mockResolvedValue("scanId"),
    openAppSettings: jest.fn(),
    hideBackground: jest.fn(),
    showBackground: jest.fn(),
    resumeScanning: jest.fn(),
    pauseScanning: jest.fn(),
  },
}));

jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    get: jest.fn().mockResolvedValue({ value: "mockedToken" }),
    set: jest.fn().mockResolvedValue(undefined),
  },
}));

const mockCheckPermission = jest.fn().mockResolvedValue(true);
const mockStartScan = jest.fn().mockResolvedValue({
  hasContent: true,
  content: "https://nwxn.qr1.ch/Fza2DPUysEPW",
});
const mockStartScanning = jest.fn();
const mockStopScan = jest.fn();
const mockSetCurrentLot = jest.fn();
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

const mockWineryData: any = {
  wineryId: "1235",
};

const mockCurrentLot: CertProductDetails = {
  certId: "certId",
  lotId: "1234567AB89",
  certType: "TYPE-001",
  certNumber: "GE-123456",
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
let mockScanningRangeIssue: RangeScanSaveStatus = { error: false };;
jest.mock("../../hooks/useScanner", () => ({
  useScanner: jest.fn(() => ({
    checkPermission: mockCheckPermission,
    startScan: mockStartScan,
    stopScan: mockStopScan,
    startScanning: mockStartScanning,
    setCurrentLot: mockSetCurrentLot,
    currentLot: mockCurrentLot,
    currentScanned: mockCurrentScanned,
    winery: mockWineryData,
    setShowModalError: jest.fn(),
    currentScannedRef: { current: mockCurrentScanned },
    listRemoveRef: mockListRemoveRef,
    isRangeScan: true,
    unableToSaveStatus: { error: false },
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

JSON.parse = jest.fn().mockReturnValue({
  locale: "en",
});

const pushMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useHistory: () => ({
    push: pushMock,
    location: {
      pathname: "/",
    },
  }),
}));

const localStorageMock: { [key: string]: string } = {};

// Mock localStorage
const localStorageGetterMock = jest.fn((key: string) => localStorageMock[key]);
const localStorageSetterMock = jest.fn((key: string, value: string) => {
  localStorageMock[key] = value;
});

Object.defineProperty(window, "localStorage", {
  value: {
    getItem: localStorageGetterMock,
    setItem: localStorageSetterMock,
  },
  writable: true,
});

// Mock function changeLanguage  i18n
i18n.changeLanguage = jest.fn();

describe("Landing", () => {
  beforeEach(() => {
    // jest.clearAllMocks();
    BackendAPI.getCert = jest.fn().mockResolvedValue({
      data: [
        {
          id: "certId",
          certificateNumber: "GE-123456",
          certificateType: "TYPE-001",
          lotEntries: [
            {
              lotId: "1234567AB89",
              scanningStatus: BottleType.SCANNED,
            },
          ],
        },
        {
          id: "certId",
          certificateNumber: "GE-123457",
          certificateType: "TYPE-001",
          lotEntries: [
            {
              lotId: "1234567AB90",
              scanningStatus: BottleType.SCANNED,
            },
            {
              lotId: "1234567AB91",
              scanningStatus: BottleType.SCANNED,
            },
          ],
        },
      ],
    });
  });

  afterEach(() => {
    mockIsPopupConfirmOpen = false;
    mockConfirmRescanLot = false;
    mockConfirmApproveRangeScan = false;
    mockScanningRangeIssue = { error: false };
  });

  test("should call saveScanRangeApi when save scanning range", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    render(<Landing />);
    await waitFor(() => {
      expect(screen.getByText("GE-123456")).toBeInTheDocument();
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("confirm-button"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("rangeScanPopupReviewSubTitle")
      ).toBeInTheDocument();
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
      expect(BackendAPI.saveScanRangeApi).toBeCalledWith(
        "1235",
        "1234567AB89",
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

  test("should show popup error when saveScanRangeApi return error 409", async () => {
    mockPopupConfirmProps = jest.fn().mockReturnValue({
      title: "scanningRangeIssue",
      cancelAction: jest.fn(),
    });
    BackendAPI.saveScanRangeApi = jest
      .fn()
      .mockRejectedValue({ response: { status: 409 } });
    render(<Landing />);
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("confirm-button")[0]);
    });
    mockIsPopupConfirmOpen = true;
    mockScanningRangeIssue = { error: true };
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
    });
    await waitFor(() => {
      expect(mockSetScanningRangeIssue).toBeCalledWith({ error: true, approve: false });
      fireEvent.click(screen.getByTestId("confirm-button"));
    });
    await waitFor(() => {
      expect(mockSetScanningRangeIssue).toBeCalledWith({ error: false });
      expect(mockSetConfirmRescanLot).toBeCalledWith(false);
      expect(BarcodeScanner.resumeScanning).toBeCalled();
    });
  });

  test("modal confirm rescan state should be set to true after click rescan scanning range", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    render(<Landing />);
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("confirm-button")[0]);
    });
    mockIsPopupConfirmOpen = true;
    mockConfirmRescanLot = true;
    await waitFor(() => {
      expect(screen.getByTestId("button-rescan-range")).toBeInTheDocument();
      fireEvent.click(screen.getByTestId("button-rescan-range"));
      expect(mockSetConfirmRescanLot).toBeCalledWith(true);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("confirm-button"));
      expect(mockSetScanningRangeIssue).toBeCalledWith({ error: false });
      expect(mockSetConfirmRescanLot).toBeCalledWith(false);
      expect(BarcodeScanner.resumeScanning).toBeCalled();
    });
  });

  test("should show popup error when saveScanRangeApi return error 400", async () => {
    mockPopupConfirmProps = jest.fn().mockReturnValue({
      title: "scanningRangeIssue",
      cancelAction: jest.fn(),
    });
    BackendAPI.saveScanRangeApi = jest
      .fn()
      .mockRejectedValue({ response: { status: 400 } });
    render(<Landing />);
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("confirm-button")[0]);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
      expect(mockSetUnableToSave).toBeCalled();
    });
  });

  test("modal confirm approve state should be set to true after click approve scanning range", async () => {
    BackendAPI.saveScanRangeApi = jest.fn();
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("confirm-button")[0]);
    });
    mockIsPopupConfirmOpen = true;
    mockConfirmApproveRangeScan = true;
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-approve-range-scan"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("confirm-button"));
      expect(BackendAPI.saveScanRangeApi).toBeCalledWith(
        "1235",
        "1234567AB89",
        "certId",
        {
          startRange: "1",
          endRange: "2",
          isSequentialNumber: false,
          finalise: true,
        }
      );
    });
  });
});

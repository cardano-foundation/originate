import { BarcodeScanner } from "@capacitor-community/barcode-scanner";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { CertProductView } from "./CertProductView";

jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    set: jest.fn(),
    get: jest.fn().mockResolvedValue({ value: undefined }),
  },
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

jest.mock("react-router-dom", () => ({
  useHistory: jest.fn().mockReturnValue({
    goBack: jest.fn(),
    location: {
      state: {
        id: "hj43h3h",
        certificateNumber: "GE-12345",
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
  },
}));

jest.mock("../../../utils", () => ({
  SCANTRUST_SCAN_URL: "https://nwxn.qr1.ch/",
}));

describe("Cert Product View - Use Scanner", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("modal error function should call with scanNotRecognized param", async () => {
    BarcodeScanner.startScanning = jest
      .fn()
      .mockImplementation((_options, callback) => {
        callback({
          content: "invalidQrCode",
          hasContent: true,
        });
      });
    act(() => {
      render(<CertProductView />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("1234567AB89"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    await waitFor(() => {
      expect(screen.getByText("scanNotRecognized")).toBeInTheDocument();
    });
  });

  test("modal error function should call with scanExtendIdWrong param", async () => {
    BarcodeScanner.startScanning = jest
      .fn()
      .mockImplementation((_options, callback) => {
        callback({
          hasContent: true,
          content: "https://nwxn.qr1.ch/YGFeb8O0tAmH",
        });
      });
    act(() => {
      render(<CertProductView />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("1234567AB89"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    await waitFor(() => {
      expect(screen.getByText("scanExtendIdWrong")).toBeInTheDocument();
    });
  });
});

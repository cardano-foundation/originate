/* eslint-disable no-undef */
import {
  BarcodeScanner,
  SupportedFormat,
} from "@capacitor-community/barcode-scanner";
import { isPlatform } from "@ionic/react";
import { act, renderHook } from "@testing-library/react";
import { useScanner } from "./useScanner";

jest.mock("@capacitor-community/barcode-scanner", () => ({
  SupportedFormat: {
    QR_CODE: "qr_code",
  },
  BarcodeScanner: {
    checkPermission: jest.fn().mockResolvedValue({ granted: true }),
    prepare: jest.fn().mockResolvedValue(undefined),
    stopScan: jest.fn().mockResolvedValue(undefined),
    showBackground: jest.fn().mockResolvedValue(undefined),
    startScan: jest.fn(),
    startScanning: jest.fn().mockResolvedValue("scanId"),
    openAppSettings: jest.fn(),
    hideBackground: jest.fn(),
  },
}));

jest.mock("@ionic/react", () => ({
  isPlatform: jest.fn(),
}));

describe("useScanner", () => {
  beforeEach(() => {
    (isPlatform as jest.Mock).mockReturnValue(true);
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test("checkPermission returns true if permission is granted", async () => {
    const { result } = renderHook(() => useScanner());

    const permissionGranted = await result.current.checkPermission();
    expect(permissionGranted).toBe(true);
  });

  test("checkPermission opens app settings if permission is never asked", async () => {
    (BarcodeScanner.checkPermission as jest.MockedFn<any>).mockResolvedValue({
      neverAsked: true,
    });
    (global as any).confirm = jest.fn(() => true);

    const { result } = renderHook(() => useScanner());

    const permissionGranted = await result.current.checkPermission();
    expect(permissionGranted).toBe(false);
    expect((global as any).confirm).toBeCalled();
    expect(BarcodeScanner.openAppSettings).toBeCalled();
  });

  // Mock the BarcodeScanner
  jest.mock("@capacitor-community/barcode-scanner", () => ({
    BarcodeScanner: {
      checkPermission: jest.fn().mockResolvedValue({ neverAsked: true }),
      startScan: jest.fn().mockResolvedValue({
        content: "abcd1234",
        hasContent: true,
      }),
    },
  }));

  test("startScan starts scanning and returns a BarcodeScanResult", async () => {
    (BarcodeScanner.checkPermission as jest.MockedFn<any>).mockResolvedValue({
      granted: true,
      neverAsked: true,
    });
    // Mock BarcodeScanner.startScanning to resolve with a BarcodeScanResult
    const expectedBarcodeResult = {
      content: "YOUR_MOCKED_CONTENT",
      hasContent: true,
    };

    // Create a mock function for BarcodeScanner.startScanning
    const mockStartScanning = jest.fn((options, callback) => {
      callback(expectedBarcodeResult);
    });

    // Mock BarcodeScanner.startScanning with the custom mock function
    (BarcodeScanner.startScanning as jest.MockedFn<any>).mockImplementation(
      mockStartScanning
    );

    const { result } = renderHook(() => useScanner());

    // Call the startScan function
    const scanPromise = result.current.startScan();
    const scanResult = await scanPromise;
    expect(scanResult).toEqual(expectedBarcodeResult);

    // Check if BarcodeScanner.startScanning was called with the correct options
    expect(mockStartScanning).toHaveBeenCalledWith(
      {
        targetedFormats: [SupportedFormat.QR_CODE],
      },
      expect.any(Function) // We pass a mock function as the callback
    );
    expect(BarcodeScanner.hideBackground).toHaveBeenCalled();
    expect(BarcodeScanner.startScanning).toHaveBeenCalled();
  });

  test("stopScan calls BarcodeScanner.stopScan and BarcodeScanner.showBackground", async () => {
    (BarcodeScanner.checkPermission as jest.MockedFn<any>).mockResolvedValue({
      granted: true,
      neverAsked: true,
    });
    (BarcodeScanner.stopScan as jest.MockedFn<any>).mockResolvedValue(
      undefined
    );
    (BarcodeScanner.showBackground as jest.MockedFn<any>).mockResolvedValue(
      undefined
    );

    const { result } = renderHook(() => useScanner());
    await act(async () => {
      await result.current.stopScan();
    });
    expect(BarcodeScanner.stopScan).toBeCalled();
    expect(BarcodeScanner.showBackground).toBeCalled();
  });

  test("startScanning calls BarcodeScanner.startScanning with callback", async () => {
    const mockStartScanning = jest.fn().mockResolvedValue("scanId");
    (BarcodeScanner.checkPermission as jest.MockedFn<any>).mockResolvedValue({
      neverAsked: true,
    });
    (BarcodeScanner.hideBackground as jest.MockedFn<any>).mockResolvedValue(
      undefined
    );
    (BarcodeScanner.startScanning as jest.MockedFn<any>).mockImplementation(
      mockStartScanning
    );

    const { result } = renderHook(() => useScanner());

    await result.current.startScanning();
    expect(BarcodeScanner.hideBackground).toBeCalled();
    expect(mockStartScanning).toBeCalledWith(
      { targetedFormats: ["qr_code"] },
      expect.any(Function)
    );
  });

  //UT getWinery
  jest.mock("../../services", () => ({
    BackendAPI: {
      getWinery: jest.fn().mockResolvedValue({
        data: [
          {
            wineryId: "1235",
            wineryName: "1",
          },
        ],
      }),
    },
  }));
});

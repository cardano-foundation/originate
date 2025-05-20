import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { ToastMessageProvider } from "../../../context";
import { BackendAPI } from "../../../services/backendApi";
import { CertProductDetails, PopupInfoBottle } from "../../components";
import { PopupCancelScan } from "../../components/PopupCancelScan";
import { Landing } from "./Landing";
import { BottleByCertLot } from "../../hooks/types";
import { BottleInfo } from "../../common/responses";
import { BottleType } from "../../common/types";

jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    set: jest.fn(),
    get: jest.fn().mockResolvedValue({ value: undefined }),
  },
}));
jest.mock("../../../utils", () => ({
  PREFIX_BOTTLE_ID: "https://nwxn.qr1.ch/",
  sleep: jest.fn(),
  convertName: jest.fn(),
}));

jest.mock("react-i18next", () => ({
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
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

let mockCheckPermission = jest.fn().mockResolvedValue(true);
const mockStartScan = jest.fn().mockResolvedValue({
  hasContent: true,
  content: "https://nwxn.qr1.ch/Fza2DPUysEPW",
});
const mockStartScanning = jest.fn();
const mockStopScan = jest.fn();
const mockSetCurrentLot = jest.fn();
let mockCurrentScanned: BottleByCertLot[] = [
  {
    id: "bottle1",
    lotId: "1",
    sequentialNumber: 1,
    reelNumber: 1,
    certificateId: "2",
  },
];
let mockWineryData: any = {
  wineryId: "1235",
};
const mockCurrentLot: CertProductDetails = {
  certId: "GE-123456",
  lotId: "1234567AB89",
  certType: "TYPE-001",
  certNumber: "CERT-NUM"
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
const mockPopupConfirmProps = jest.fn().mockReturnValue({
  title: "confirmTitle",
  cancelAction: jest.fn(),
});
let mockIsRangeScan = false;
const mockSetIsRangeScan = jest.fn();
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
    isRangeScan: mockIsRangeScan,
    setUnableToSaveStatus: jest.fn(),
    popupConfirmProps: mockPopupConfirmProps,
    setIsRangeScan: mockSetIsRangeScan,
    setRangeScanningStatus: jest.fn(),
    sortableScannedRange: mockSortableScannedRange,
    setConfirmApproveRangeScan: jest.fn(),
    setConfirmRescanLot: false,
    isPopupConfirmOpen: false,
    confirmRescanLot: false,
    confirmApproveRangeScan: false,
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

jest.mock("../../../services/backendApi");

describe("Landing", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    BackendAPI.getBottleByLot = jest.fn().mockResolvedValue([
      {
        id: "bottle1",
        lotId: "1",
        sequentialNumber: 1,
        reelNumber: 1,
        certificateId: "2",
      },
    ]);
    BackendAPI.getWinery = jest.fn().mockResolvedValue({
      data: [
        {
          wineryId: "1235",
          wineryName: "1",
        },
      ],
    });

    BackendAPI.getCert = jest.fn().mockResolvedValue({
      data: [
        {
          id: "certId1",
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
          id: "certId2",
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
    BackendAPI.saveAndContinue = jest.fn();
    BackendAPI.getInfoBottle = jest.fn().mockResolvedValue([
      {
        certId: "GE-123456",
        lotId: "1234567AB89",
        scanningStatus: BottleType.SCANNED,
        sequentialNumber: "2",
        reelNumbe: "10",
      },
    ]);
  });

  jest.mock("@capacitor-community/barcode-scanner", () => ({
    BarcodeScanner: {
      checkPermission: jest.fn().mockResolvedValue(true),
      startScan: jest.fn().mockResolvedValue({
        hasContent: true,
        content: "BOTTLE123",
      }),
      resumeScanning: jest.fn(),
    },
  }));

  test("should render landing", async () => {
    act(() => {
      render(<Landing />);
    });

    await waitFor(() => {
      expect(screen.getByText("certificate")).toBeInTheDocument();
      expect(screen.getByText("GE-123456")).toBeInTheDocument();
    });
  });

  test("should handle the click avatar", async () => {
    act(() => {
      render(<Landing />);
    });
    fireEvent.click(screen.getByTestId("avatar-user"));
    await waitFor(() => {
      expect(screen.getByTestId("popup-user-setting")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByTestId("popup-user-closeicon"));
    expect(screen.queryByTestId("popup-user-setting")).not.toBeInTheDocument();
  });

  test("Should not attempt to load certificates if there is no winery ID", async () => {
    mockWineryData = {};
    BackendAPI.getCert = jest.fn();
    act(() => {
      render(<Landing />);
    });
    // Must wait to stop false positive as this text is also displayed while certs are loading
    await new Promise((resolve) => setTimeout(resolve, 250));
    await waitFor(() => {
      expect(screen.getByText("noCertificateAvailable")).toBeInTheDocument();
      expect(BackendAPI.getCert).not.toBeCalled();
    });
  });

  test("Should render 'No certificates available that require scanning'", async () => {
    BackendAPI.getCert = jest.fn().mockResolvedValue({
      data: [],
    });
    act(() => {
      render(<Landing />);
    });
    // Must wait to stop false positive as this text is also displayed while certs are loading
    await new Promise((resolve) => setTimeout(resolve, 250));
    await waitFor(() => {
      expect(screen.getByText("noCertificateAvailable")).toBeInTheDocument();
    });
  });

  test("Should render 'No certificates have been fully scanned yet.'", async () => {
    BackendAPI.getCert = jest.fn().mockResolvedValue({
      data: [],
    });
    act(() => {
      render(<Landing />);
    });
    // Must wait to stop false positive as this text is also displayed while certs are loading
    await new Promise((resolve) => setTimeout(resolve, 250));
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("segment-scanning-completed"));
      expect(screen.getByText("noCertificateFully")).toBeInTheDocument();
    });
  });

  test("handleCheckBottleScan starts scanning when allowed and shows the PopupInfoBottle", async () => {
    act(() => {
      render(<Landing />);
    });
    const buttonScan = screen.queryByTestId("button-scan");
    expect(buttonScan).toBeInTheDocument();
    buttonScan?.click();
    await waitFor(() => {
      expect(screen.getByText("checkBottleStatus")).toBeInTheDocument();
    });
  });

  test("should show popup PopupInfoBottle when click button check bottle", async () => {
    const mockData: BottleInfo = {
      certId: "41234fg",
      certNumber: "GE-12356",
      certType: "TYPE-001",
      lotId: "1234567AB89",
      scanningStatus: BottleType.NOT_SCAN,
      sequentialNumber: "2",
      reelNumber: "10",
    };

    render(
      <PopupInfoBottle
        isOpen={true}
        onLeave={jest.fn()}
        onScan={jest.fn()}
        bottleData={mockData}
      />
    );

    await waitFor(() => {
      expect(screen.getByText("notassociatedyet")).toBeInTheDocument();
      expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    });
  });

  test("renders ModalConfirm with Scan By Range correctly", async () => {
    // These tests are weak because we are mocking useScanner...
    // but we can at least check that mockSetIsRangeScan is called
    mockWineryData = {
      wineryId: "1235",
    };
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockIsRangeScan = true;

    render(<Landing />);
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });

    fireEvent.click(screen.getByText("scanByRange"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    expect(mockSetIsRangeScan).toBeCalledWith(true);

    await waitFor(() => {
      const modalElement = screen.getByText("lastRangeScan");
      expect(modalElement).toBeInTheDocument();
    });
  });

  test("renders ModalConfirm with Scan By Bottle correctly", async () => {
    mockWineryData = {
      wineryId: "1235",
    };
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockIsRangeScan = false;

    render(<Landing />);
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });

    fireEvent.click(screen.getByText("scanByBottle"));

    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    expect(mockSetIsRangeScan).toBeCalledWith(false);
    await waitFor(() => {
      expect(screen.getByTestId("button-save-scan")).toBeInTheDocument();
    });
  });

  test("should show popup cancel scan when stop scan", async () => {
    const mockCheckPermission = jest.fn().mockResolvedValue(true);
    const mockStopScan = jest.fn();
    jest.mock("../../hooks/useScanner", () => ({
      useScanner: () => ({
        checkPermission: mockCheckPermission,
        stopScan: mockStopScan,
      }),
    }));
    const mockDataModal = {
      isOpen: true,
      title: "Cancel Title",
      textConfirm: "Confirm",
      textCancel: "Cancel",
      onConfirm: () => {
        mockStopScan();
      },
      onCancel: jest.fn(),
    };

    render(
      <PopupCancelScan
        isOpen={mockDataModal.isOpen}
        title={mockDataModal.title}
        textConfirm={mockDataModal.textConfirm}
        textCancel={mockDataModal.textCancel}
        onConfirm={mockDataModal.onConfirm}
        onCancel={mockDataModal.onCancel}
      />
    );
    expect(screen.getByTestId("popup-cancel-scan")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      expect(mockStopScan).toHaveBeenCalled();
    });
  });

  test("should show modal confirm correct with click single slot", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      expect(screen.getByText("areYouReadyToContinueScan")).toBeInTheDocument();
      fireEvent.click(screen.getAllByTestId("confirm-button")[0]);
    });
    await waitFor(() => {
      expect(screen.getByTestId("landing-content")).toHaveClass(
        "main-content md content-ltr"
      );
    });
  });

  test("bottle item must be remove when click delete icon", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);

    BackendAPI.saveAndContinue = jest.fn().mockResolvedValue({});
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    fireEvent.click(screen.getByTestId("button-list"));
    const buttonSave = screen.getByTestId("button-save-scan");
    const iconDelete = screen.getByTestId("icon-delete-bottle");
    act(() => {
      fireEvent.click(iconDelete);
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(buttonSave);
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(popupSave).not.toBeInTheDocument();
      // expect(screen.getByTestId("bottle-item")).not.toBeInTheDocument();
      // expect(screen.queryAllByTestId("bottle-item").length).toEqual(0);
    });
    // expect(popupSave).not.toBeInTheDocument();
  });

  test("popup save must be display when click button approve", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    act(() => {
      render(<Landing />);
    });

    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("confirm-button"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-scan"));
    });
    await waitFor(() => {
      expect(screen.getByTestId("popup-save-and-review")).toBeInTheDocument();
    });
  });

  test("popup save must be hidden when click button save later and call api", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockCurrentScanned = [
      {
        id: "bottle1",
        lotId: "1",
        sequentialNumber: 1,
        reelNumber: 1,
        certificateId: "GE-123456",
      },
    ];

    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("confirm-button"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-scan"));
    });
    const popupSave = screen.getByTestId("popup-save-and-review");
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("button-save-later"));
    });
    await waitFor(() => {
      expect(popupSave).not.toBeInTheDocument();
    });
  });

  test("should navigate to CertProductView when click cert multi slot", async () => {
    mockWineryData = {
      wineryId: "1235",
    };
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123457"));
    });
    await waitFor(() => {
      expect(pushMock).toBeCalledWith("/detail-lot/certId2", {
        id: "certId2",
        certificateNumber: "GE-123457",
        certificateType: "TYPE-001",
        lotEntries: [
          { lotId: "1234567AB90", scanningStatus: BottleType.SCANNED },
          { lotId: "1234567AB91", scanningStatus: BottleType.SCANNED },
        ],
      });
    });
  });

  test("should save button disable when nothing has been scanned", async () => {
    mockCurrentScanned.length = 0;
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonSave = screen.getByTestId("button-save-scan");
    expect(buttonSave).toBeDisabled();
  });

  test("popup save must be hidden and call api when click button save later", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockCurrentScanned = [
      {
        id: "bottle1",
        lotId: "1",
        sequentialNumber: 1,
        reelNumber: 1,
        certificateId: "GE-123456",
      },
    ];

    BackendAPI.saveAndContinue = jest.fn();
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      expect(screen.getByText("GE-123456")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("GE-123456"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });

    fireEvent.click(screen.getByTestId("button-list"));
    fireEvent.click(screen.getByTestId("icon-delete-bottle"));
    fireEvent.click(screen.getByTestId("confirm-button"));

    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(popupSave).not.toBeInTheDocument();
      expect(BackendAPI.saveAndContinue).toBeCalledWith(
        "1235",
        "1234567AB89",
        "GE-123456",
        {
          add: [],
          remove: ["bottle1"],
          finalise: false,
        }
      );
    });
  });

  test("popup save must be hidden when click review and approve button", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    act(() => {
      render(<Landing />);
    });

    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-review-and-approve"));
    expect(popupSave).not.toBeInTheDocument();
    expect(screen.getByTestId("modal-list")).toBeInTheDocument();
  });

  test("button list should be disabled when current certificate-lot pair has no scanned", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockCurrentScanned.length = 0;
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonList = screen.getByTestId("button-list");
    expect(buttonList).toBeDisabled();
  });

  test("should call function approve when approve list bottle", async () => {
    mockCurrentScanned = [
      {
        id: "bottle1",
        lotId: "1",
        sequentialNumber: 1,
        reelNumber: 1,
        certificateId: null,
      },
    ];
    BackendAPI.saveAndContinue = jest.fn();
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-review-and-approve"));
    expect(popupSave).not.toBeInTheDocument();
    expect(screen.getByTestId("modal-list")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("button-approve-lot"));
    expect(screen.getByTestId("modal-confirm-scan")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      expect(BackendAPI.saveAndContinue).toBeCalledWith(
        "1235",
        "1234567AB89",
        "GE-123456",
        {
          add: ["bottle1"],
          remove: [],
          finalise: true,
        }
      );
    });
  });

  test("BW-47 should display current scanned bottle when call api", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockCurrentScanned = [
      {
        id: "bottle1",
        lotId: "1",
        sequentialNumber: 1,
        reelNumber: 1,
        certificateId: "GE-123456",
      },
    ];
    act(() => {
      render(<Landing />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });

    expect(screen.getByTestId("modal-confirm-scan")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("confirm-button"));
    expect(screen.getByText("CERT-NUM")).toBeInTheDocument();
    expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    expect(screen.getAllByText("TYPE-001").length).toBe(3);  // 2 cert cards + this top text
  });

  test("toast message success should be display 3 seconds when save and continue", async () => {
    mockCheckPermission = jest.fn().mockResolvedValue(true);

    act(() => {
      render(
        <ToastMessageProvider>
          <Landing />
        </ToastMessageProvider>
      );
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("GE-123456"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    fireEvent.click(screen.getByTestId("button-save-scan"));
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(screen.getByTestId("toast-message")).toBeInTheDocument();
      expect(screen.getByTestId("toast-message")).toHaveAttribute(
        "duration",
        "3000"
      );
    });
  });
});

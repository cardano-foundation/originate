import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { ToastMessageProvider } from "../../../context";
import { BackendAPI } from "../../../services";
import { PopupCancelScan } from "../../components/PopupCancelScan";
import { CertProductView } from "./CertProductView";
import { BottleByCertLot } from "../../hooks/types";
import { CertProductDetails } from "../../components";

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
let mockCurrentScanned: BottleByCertLot[] = [
  {
    id: "bottle1",
    lotId: "1",
    sequentialNumber: 1,
    reelNumber: 1,
    certificateId: "2",
  },
];

const mockCurrentLot: CertProductDetails = {
  certId: "GE-123459",
  lotId: "1234567AB88",
  certNumber: "certNumA",
  certType: "certTypeA",
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
    isRangeScan: mockIsRangeScan,
    setUnableToSaveStatus: jest.fn(),
    popupConfirmProps: mockPopupConfirmProps,
    setIsRangeScan: mockSetIsRangeScan,
    setRangeScanningStatus: jest.fn(),
    sortableScannedRange: mockSortableScannedRange,
    setConfirmApproveRangeScan: jest.fn(),
    setConfirmRescanLot: jest.fn(),
  })),
}));

jest.mock("react-router-dom", () => ({
  useHistory: jest.fn().mockReturnValue({
    goBack: jest.fn(),
    location: {
      state: {
        id: "GE-123456",
        certificateNumber: "certNum",
        certificateType: "certType",
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

describe("Cert Product View", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("should render the component correctly", async () => {
    act(() => {
      render(<CertProductView />);
    });
    await waitFor(() => {
      expect(screen.getAllByText("certNum").length).toBe(3);
      expect(screen.getAllByText("certType").length).toBe(3);
      expect(screen.getAllByText("1234567AB89").length).toBe(1);
      expect(screen.getByText("2345678CD90")).toBeInTheDocument();
    });
  });

  test("should handle scanning by bottle", async () => {
    const mockCheckPermission = jest.fn().mockResolvedValue(true);
    jest.mock("../../hooks/useScanner", () => ({
      useScanner: () => ({
        checkPermission: mockCheckPermission,
      }),
    }));
    render(<CertProductView />);

    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      const scanByRangeButton = screen.getByText("scanByBottle");
      fireEvent.click(scanByRangeButton);
    });
    await waitFor(() => {
      expect(screen.getByTestId("button-save-scan")).toBeInTheDocument();
    });

    // Shows contents of currentLot (differs from the clicked lot above but its mocked)
    await waitFor(() => {
      expect(screen.getByText("certNumA")).toBeInTheDocument();
      expect(screen.getByText("1234567AB88")).toBeInTheDocument();
      expect(screen.getByText("certTypeA")).toBeInTheDocument();
    }); 
  });

  test("should handle scanning by range", async () => {
    // These tests are weak because we are mocking useScanner...
    // but we can at least check that mockSetIsRangeScan is called
    const mockCheckPermission = jest.fn().mockResolvedValue(true);
    mockIsRangeScan = true;

    jest.mock("../../hooks/useScanner", () => ({
      useScanner: () => ({
        checkPermission: mockCheckPermission,
      }),
    }));
    render(<CertProductView />);

    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByRange"));
    });

    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });

    await waitFor(() => {
      expect(screen.getByText("lastRangeScan")).toBeInTheDocument();
    });

    // Shows contents of currentLot (differs from the clicked lot above but its mocked)
    await waitFor(() => {
      expect(screen.getByText("certNumA")).toBeInTheDocument();
      expect(screen.getByText("1234567AB88")).toBeInTheDocument();
      expect(screen.getByText("certTypeA")).toBeInTheDocument();
    }); 
  });

  test("should handle scanning by bottle", async () => {
    mockIsRangeScan = false;
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("scanByBottle"));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("understand"));
    });
    await waitFor(() => {
      expect(screen.getByTestId("button-save-scan")).toBeInTheDocument();
    });
  });

  test("should handle the card click", async () => {
    act(() => {
      render(<CertProductView />);
    });
    await waitFor(() => {
      fireEvent.click(screen.getByText("1234567AB89"));
    });
    expect(screen.getByTestId("modal-confirm-scan")).toBeInTheDocument();
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

    await waitFor(() => {
      expect(screen.getByTestId("popup-cancel-scan")).toBeInTheDocument();
      fireEvent.click(screen.getByTestId("confirm-button"));
    });
    expect(mockStopScan).toHaveBeenCalled();
    expect(mockStopScan).toHaveBeenCalled();
    expect(mockStopScan).toHaveBeenCalled();
  });

  test("should modal listBottle to be show", async () => {
    act(() => {
      render(<CertProductView />);
    });
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const btnList = screen.getByTestId("button-list");
    fireEvent.click(btnList);
    expect(screen.getByTestId("modal-list")).toBeInTheDocument();
  });

  test("should modal confirm close when click cancel button", async () => {
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    const confirmModal = screen.getByTestId("modal-confirm-scan");
    fireEvent.click(screen.getByTestId("cancel-button"));
    expect(confirmModal).not.toBeInTheDocument();
  });

  test("popup save must be display when click button approve", async () => {
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonSave = screen.getByTestId("button-save-scan");
    fireEvent.click(buttonSave);
    const popupSave = screen.getByTestId("popup-save-and-review");
    await waitFor(() => {
      expect(popupSave).toBeInTheDocument();
    });
  });

  test("popup save must be hidden when click button save later", async () => {
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonSave = screen.getByTestId("button-save-scan");
    fireEvent.click(buttonSave);
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(BackendAPI.saveAndContinue).toBeCalled();
      expect(popupSave).not.toBeInTheDocument();
    });
  });

  test("popup save must be hidden when click review and approve button", async () => {
    render(<CertProductView />);
    await waitFor(() => {
      fireEvent.click(screen.getByText("1234567AB89"));
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-review-and-approve"));
    expect(popupSave).not.toBeInTheDocument();
    expect(screen.getByTestId("modal-list")).toBeInTheDocument();
  });

  test("should button list enable when scan success", async () => {
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(screen.getByTestId("button-list"));
    expect(screen.getByTestId("button-list")).not.toHaveClass("btn-disabled");
  });

  test("bottle item must be removed when click delete icon", async () => {
    render(<CertProductView />);
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByText("scanByBottle"));
    fireEvent.click(screen.getByTestId("button-list"));
    fireEvent.click(screen.getByTestId("icon-delete-bottle"));
    fireEvent.click(screen.getByText("yesDelete"));
    await waitFor(() => {
      expect(screen.queryByText("bottle1")).toBeNull();
    });
  });

  test("button list should be disabled when current certificate-lot pair has no scanned", async () => {
    mockCurrentScanned.length = 0;
    render(<CertProductView />);

    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonList = screen.getByTestId("button-list");
    await waitFor(() => {
      expect(buttonList).toBeDisabled();
    });
  });

  test("should call function approve when approve list bottle", async () => {
    BackendAPI.saveAndContinue = jest.fn();
    render(<CertProductView />);

    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-review-and-approve"));
    await waitFor(() => {
      expect(popupSave).not.toBeInTheDocument();
      expect(screen.getByTestId("modal-list")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByTestId("button-approve-lot"));
    await waitFor(() => {
      expect(screen.getByTestId("modal-confirm-scan")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByTestId("confirm-button"));
    await waitFor(() => {
      expect(BackendAPI.saveAndContinue).toHaveBeenCalledWith(
        "1234",
        "1234567AB88",
        "GE-123459",
        {
          add: [],
          remove: ["bottle1"],
          finalise: true,
        }
      );
    });
  });

  test("should save button disable when nothing has been scanned", async () => {
    mockCurrentScanned.length = 0;
    render(<CertProductView />);
    await waitFor(() => {
      expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonSave = screen.getByTestId("button-save-scan");
    expect(buttonSave).toBeDisabled();
  });

  test("popup save must be hidden and call api when click button save later", async () => {
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
    render(<CertProductView />);
    await waitFor(() => {
      expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    fireEvent.click(screen.getByTestId("button-list"));
    fireEvent.click(screen.getByTestId("icon-delete-bottle"));
    fireEvent.click(screen.getByText("yesDelete"));

    fireEvent.click(screen.getByTestId("button-save-scan"));
    const popupSave = screen.getByTestId("popup-save-and-review");
    fireEvent.click(screen.getByTestId("button-save-later"));
    await waitFor(() => {
      expect(popupSave).not.toBeInTheDocument();
      expect(BackendAPI.saveAndContinue).toBeCalledWith(
        "1234",
        "1234567AB88",
        "GE-123459",
        {
          add: [],
          remove: ["bottle1"],
          finalise: false,
        }
      );
    });
  });

  test("button list shold be disabled when current certificate-lot pair has no scanned", async () => {
    mockCurrentScanned.length = 0;
    render(<CertProductView />);

    await waitFor(() => {
      expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonList = screen.getByTestId("button-list");
    expect(buttonList).toBeDisabled();
  });

  test("toast message success should be display 3 seconds when save and continue", async () => {
    render(
      <ToastMessageProvider>
        <CertProductView />
      </ToastMessageProvider>
    );

    await waitFor(() => {
      expect(screen.getByText("1234567AB89")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("1234567AB89"));
    fireEvent.click(screen.getByTestId("confirm-button"));
    const buttonSave = screen.getByTestId("button-save-scan");
    fireEvent.click(buttonSave);
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

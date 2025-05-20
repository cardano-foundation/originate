import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import { PopupInfoBottle } from "./PopupInfoBottle";
import { BottleType } from "../../common/types";
import { BottleInfo } from "../../common/responses";

const mockDataNotAssociated: BottleInfo = {
  certId: "CERT1234",
  certNumber: "GE-12345",
  certType: "TYPE-001",
  lotId: "LOT5678",
  scanningStatus: BottleType.NOT_SCAN,
  sequentialNumber: "123",
  reelNumber: "456",
};

const mockDataScanned: BottleInfo = {
  certId: "CERT1234",
  certNumber: "GE-12345",
  certType: "TYPE-001",
  lotId: "LOT5678",
  scanningStatus: BottleType.SCANNED,
  sequentialNumber: "123",
  reelNumber: "456",
};

const mockDataApproved: BottleInfo = {
  certId: "CERT1234",
  certNumber: "GE-12345",
  certType: "TYPE-001",
  lotId: "LOT5678",
  scanningStatus: BottleType.SCANNED_APPROVED,
  sequentialNumber: "123",
  reelNumber: "456",
};

describe("PopupInfoBottle", () => {
  test("should render the popup when isOpen is true", async () => {
    const mockDataModal = {
      isOpen: true,
      onLeave: jest.fn(),
      onScan: jest.fn(),
    };
    render(
      <PopupInfoBottle
        isOpen={mockDataModal.isOpen}
        onLeave={mockDataModal.onLeave}
        onScan={mockDataModal.onScan}
        bottleData={mockDataNotAssociated}
      />
    );
    await waitFor(() => {
      expect(screen.getByTestId("info-bottle")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByTestId("info-scan-button"));
    expect(mockDataModal.onScan).toHaveBeenCalled();
    fireEvent.click(screen.getByTestId("info-leave-button"));
    expect(mockDataModal.onLeave).toHaveBeenCalled();
  });

  test("should render not-scan bottle type correctly", () => {
    render(
      <PopupInfoBottle
        isOpen={true}
        onLeave={jest.fn()}
        onScan={jest.fn()}
        bottleData={mockDataNotAssociated}
      />
    );

    expect(screen.getByText("notassociatedyet")).toBeInTheDocument();
    expect(screen.getByText("LOT5678")).toBeInTheDocument();
    expect(screen.getByText("123")).toBeInTheDocument();
    expect(screen.queryByText("GE-12345")).not.toBeInTheDocument();
    expect(screen.queryByText("TYPE-001")).not.toBeInTheDocument();
  });

  test("should render scanned bottle type correctly", async () => {
    act(() => {
      render(
        <PopupInfoBottle
          isOpen={true}
          onLeave={jest.fn()}
          onScan={jest.fn()}
          bottleData={mockDataScanned}
        />
      );
    });
    await waitFor(() => {
      expect(screen.getByText("savebottlenotapproved")).toBeInTheDocument();
      expect(screen.getByText("LOT5678")).toBeInTheDocument();
      expect(screen.getByText("123")).toBeInTheDocument();
      expect(screen.getByText("GE-12345")).toBeInTheDocument();
      expect(screen.getByText("TYPE-001")).toBeInTheDocument();
    });
  });

  test("should render scanned approved bottle type correctly", async () => {
    act(() => {
      render(
        <PopupInfoBottle
          isOpen={true}
          onLeave={jest.fn()}
          onScan={jest.fn()}
          bottleData={mockDataApproved}
        />
      );
    });
    await waitFor(() => {
      expect(screen.getByText("savebottleapproved")).toBeInTheDocument();
      expect(screen.getByText("LOT5678")).toBeInTheDocument();
      expect(screen.getByText("123")).toBeInTheDocument();
      expect(screen.getByText("GE-12345")).toBeInTheDocument();
      expect(screen.getByText("TYPE-001")).toBeInTheDocument();
    });
  });

  test("should trigger onScan callback when 'Scan Another Code' button is clicked", () => {
    const mockOnScan = jest.fn();
    render(
      <PopupInfoBottle
        isOpen={true}
        onLeave={jest.fn()}
        onScan={mockOnScan}
        bottleData={mockDataScanned}
      />
    );

    fireEvent.click(screen.getByTestId("info-scan-button"));
    expect(mockOnScan).toHaveBeenCalled();
  });

  test("should trigger onLeave callback when 'Leave' button is clicked", async () => {
    const mockOnLeave = jest.fn();
    render(
      <PopupInfoBottle
        isOpen={true}
        onLeave={mockOnLeave}
        onScan={jest.fn()}
        bottleData={mockDataScanned}
      />
    );
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("info-leave-button"));
      expect(mockOnLeave).toHaveBeenCalled();
    });
  });

  test("should trigger onScan callback when down icon button is clicked", async () => {
    const mockOnScan = jest.fn();
    render(
      <PopupInfoBottle
        isOpen={true}
        onLeave={jest.fn()}
        onScan={mockOnScan}
        bottleData={mockDataScanned}
      />
    );
    fireEvent.click(screen.getByTestId("info-bottle-closeicon"));
    await waitFor(() => {
      expect(mockOnScan).toHaveBeenCalled();
    });
  });
});

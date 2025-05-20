import { render, fireEvent, screen } from "@testing-library/react";

import { CertOrProductCard } from "./CertOrProductCard";
import { QRType } from "../../common/types";

const props = {
  type: QRType.ONE_LOT,
  certId: "123456",
  certNum: "GE-12345",
  certType: "TYPE-001",
  lotId: "12340000001",
  numLots: 2,
  bottles: 10,
  onClick: jest.fn(),
};

describe("ScanningItem", () => {
  test("calls onClick when card is clicked", async () => {
    const { getByTestId } = render(
      <CertOrProductCard
        isScanning
        {...props}
      />
    );
    const card = getByTestId("card-item");
    fireEvent.click(card);
    expect(props.onClick).toHaveBeenCalled();
  });

  test("renders correct icon and color for QRType.COMPLETED", async () => {
    render(
      <CertOrProductCard
        isScanning={false}
        {...props}
        type={QRType.COMPLETED}
      />
    );

    const iconImage: HTMLImageElement = screen.getByAltText("completed");
    expect(iconImage).toBeInTheDocument();
  });

  test("renders correct icon and color for QRType.ONE_LOT", async () => {
    render(
      <CertOrProductCard
        isScanning
        {...props}
        numLots={1}
        type={QRType.ONE_LOT}
      />
    );

    const iconImage: HTMLImageElement = screen.getByAltText("one-lot");
    expect(iconImage).toBeInTheDocument();
    expect(screen.getByText("numOfLot")).toBeInTheDocument();
    expect(screen.getByText("GE-12345")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
  });

  test("renders correct icon and color for QRType values", async () => {
    render(
      <CertOrProductCard
        {...props}
        isScanning={false}
        type={QRType.MANY_LOTS}
      />
    );

    const iconImage: HTMLImageElement = screen.getByAltText("many-lots");
    expect(iconImage).toBeInTheDocument();
    expect(screen.queryByTestId("dot-active")).not.toBeInTheDocument();
    expect(screen.getByText("numOfLots")).toBeInTheDocument();
    expect(screen.getByText("GE-12345")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
  });

  test("renders dot active when have status scanning", async () => {
    render(
      <CertOrProductCard
        {...props}
        type={QRType.MANY_LOTS}
        isScanning={true}
      />
    );

    expect(screen.getByTestId("dot-active")).toBeInTheDocument();
  });

  test("renders individual lot from multi cert lot correctly", async () => {
    render(
      <CertOrProductCard
        {...props}
        isScanning={false}
        multiLotCert={true}
        lotId={"12340000001"}
      />
    );

    expect(screen.getByText("12340000001")).toBeInTheDocument();
    expect(screen.getByText("GE-12345")).toBeInTheDocument();
    expect(screen.getByText("TYPE-001")).toBeInTheDocument();
  });
});

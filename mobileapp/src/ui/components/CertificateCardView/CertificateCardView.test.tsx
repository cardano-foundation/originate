import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { CertificateCardView } from "./CertificateCardView";
import { BottleType, CertificateType } from "../../common/types";
import { CertsByCategory } from "../../common/responses";

const pushMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useHistory: () => ({
    push: pushMock,
  }),
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

describe("ListScanningItem", () => {
  const mockCert: CertsByCategory = {
    listRequired: [
      {
        id: "id1",
        certificateNumber: "GE-12345",
        certificateType: "TYPE-001",
        lotEntries: [
          { lotId: "mocklot1", scanningStatus: BottleType.SCANNED },
          { lotId: "mocklot2", scanningStatus: BottleType.SCANNED_APPROVED },
        ],
      },
    ],
    listCompleted: [
      {
        id: "id2",
        certificateNumber: "GE-12346",
        certificateType: "TYPE-002",
        lotEntries: [{ lotId: "mocklot3", scanningStatus: BottleType.SCANNED }],
      },
    ],
  };

  const mockEmptyCert: CertsByCategory = {
    listRequired: [],
    listCompleted: [],
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders required certificates", async () => {
    render(
      <CertificateCardView
        cert={mockCert}
        typeScan={CertificateType.REQUIRED}
      />,
      { wrapper: MemoryRouter }
    );
    const requiredCertificate = screen.getByAltText("many-lots");
    expect(requiredCertificate).toBeInTheDocument();
  });

  test("renders completed certificates", async () => {
    render(
      <CertificateCardView
        cert={mockCert}
        typeScan={CertificateType.COMPLETED}
      />,
      { wrapper: MemoryRouter }
    );

    const completedCertificate = screen.getByAltText("completed");
    expect(completedCertificate).toBeInTheDocument();
  });

  test("navigates to detail-lot on certificate click", async () => {
    render(
      <MemoryRouter>
        <CertificateCardView
          cert={mockCert}
          typeScan={CertificateType.REQUIRED}
        />
      </MemoryRouter>
    );

    const certificate = screen.getByTestId("card-item");
    fireEvent.click(certificate);
    expect(pushMock).toHaveBeenCalledWith(
      "/detail-lot/id1",
      mockCert.listRequired[0]
    );
  });

  test("renders completed empty certificate", async () => {
    render(
      <CertificateCardView
        cert={mockEmptyCert}
        typeScan={CertificateType.COMPLETED}
      />,
      { wrapper: MemoryRouter }
    );

    const textEmpty = screen.getByText("noCertificateFully");
    expect(textEmpty).toBeInTheDocument();
  });

  test("renders required  empty certificate", async () => {
    render(
      <CertificateCardView
        cert={mockEmptyCert}
        typeScan={CertificateType.REQUIRED}
      />,
      { wrapper: MemoryRouter }
    );

    const textEmpty = screen.getByText("noCertificateAvailable");
    expect(textEmpty).toBeInTheDocument();
  });
});

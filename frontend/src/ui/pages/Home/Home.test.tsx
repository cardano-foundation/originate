import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";
import { Home } from "./Home";
import { ROLE_SYSTEM } from "../../constants/auth";
import { tableData1, tableData2 } from "../../../__mocks__/tableMock";
import { GetDataSCMService } from "../../../services/GetDataSCMService";
import {
  DeleteDataSCMService,
  FinaliseDataSCMService,
  ApproveDataSCMService,
} from "../../../services";
import { WineryService } from "../../../services/WineryService";
import { AuthProvider } from "../../../contexts/AuthContext";

const openMock = jest.fn();
window.open = openMock;

jest.mock("i18next", () => ({
  use: jest.fn().mockReturnValue({
    init: jest.fn(),
  }),
  changeLanguage: jest.fn(),
}));

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: jest.fn(() => ({ id: "1234" })),
}));

jest.mock("react-i18next", () => ({
  iniReactI18next: jest.fn(),
  useTranslation: jest.fn().mockReturnValue({
    t: (key: string) => key,
  }),
}));

jest.mock("../../../services/Instances/KeyCloakServices", () => ({
  keyCloakClient: {
    init: jest.fn().mockResolvedValue(true),
  },
}));

jest.mock("../../../services", () => ({
  DeleteDataSCMService: {
    deleteDataSCM: jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    }),
  },
  FinaliseDataSCMService: {
    finaliseDataSCM: jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    }),
  },
  ApproveDataSCMService: {
    approveDataSCM: jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["72a7f9d1"],
        failLotsAlreadyApproved: [],
        failLotsNotFinalised: [],
        failLotsNotFound: [],
        failJobsNotScheduled: [],
      },
    }),
  },
}));

describe("Home test", () => {
  beforeEach(() => {
    WineryService.getUserWinery = jest.fn().mockResolvedValue({
      data: [
        {
          wineryId: "1234",
          wineryName: "Winery 1",
        },
        {
          wineryId: "1235",
          wineryName: "Winery 2",
        },
      ],
    });
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData1 });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("should show an alert message when the login is successful", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });

    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });

    const successAlert = screen.getByText("successLogin");
    expect(successAlert).toBeInTheDocument();
  });

  //List test check action Approve

  test("should show confirm popup when click approve button", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
      fireEvent.click(screen.getAllByTestId("button-approve")[0]);
    });

    await waitFor(() => {
      expect(screen.getByText("titleApproveOneRow")).toBeInTheDocument();
      expect(screen.getByText("descriptionApproveOneRow")).toBeInTheDocument();
      expect(screen.getByText("confirmApproveOneRow")).toBeInTheDocument();
      expect(screen.getByText("cancel")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and approve bulk one row success", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["72a7f9d1"],
        failLotsAlreadyApproved: [],
        failLotsNotFinalised: [],
        failLotsNotFound: [],
        failJobsNotScheduled: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("checkbox-select")[0]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveOneRow"));
    });
    await waitFor(() => {
      expect(screen.getByText("allSuccessApprovedOneRow")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and approve bulk these row with status succeed and failJobsNotScheduled", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["72a7f9d1", "72a7f9d2"],
        failLotsAlreadyApproved: [],
        failLotsNotFinalised: [],
        failLotsNotFound: [],
        failJobsNotScheduled: ["72a7f9d3"],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      expect(screen.getAllByTestId("checkbox-select").length).not.toBe(0);
      const checkboxSelect = screen.getAllByTestId("checkbox-select");
      fireEvent.click(checkboxSelect[0]);
      fireEvent.click(checkboxSelect[1]);
      fireEvent.click(checkboxSelect[2]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveTheseRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allSuccessApprovedTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and approve bulk these row with status succeed, failLotsAlreadyApproved, failLotsNotFinalised and failLotsNotFound", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["72a7f9d1"],
        failLotsAlreadyApproved: ["72a7f9d2"],
        failLotsNotFinalised: ["72a7f9d3"],
        failLotsNotFound: ["72a7f9d4"],
        failJobsNotScheduled: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      expect(screen.getAllByTestId("checkbox-select").length).not.toBe(0);
      const checkboxSelect = screen.getAllByTestId("checkbox-select");
      fireEvent.click(checkboxSelect[0]);
      fireEvent.click(checkboxSelect[1]);
      fireEvent.click(checkboxSelect[2]);
      fireEvent.click(checkboxSelect[3]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveTheseRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("successApprovedOneRow")).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessApprovedTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(3);
    });
  });

  test("should show confirm popup when click select checkbox and approve all bulk with status succeed, failJobsNotScheduled and failLotsAlreadyApproved", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["72a7f9d1", "72a7f9d2"],
        failLotsAlreadyApproved: ["72a7f9d3", "72a7f9d4", "72a7f9d5"],
        failLotsNotFinalised: [],
        failLotsNotFound: [],
        failJobsNotScheduled: ["72a7f9d6"],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveAllRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("successApprovedTheseRows")).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessApprovedTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(3);
    });
  });

  test("should show confirm popup when click select approve all bulk with error failLotsAlreadyApproved and failLotsNotFinalised", async () => {
    const history = createMemoryHistory();
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        failLotsAlreadyApproved: ["72a7f9d1", "72a7f9d2"],
        failLotsNotFinalised: ["72a7f9d3", "72a7f9d4", "72a7f9d5", "72a7f9d6"],
        failLotsNotFound: [],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("unSuccessApprovedTheseRows")
      ).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessApprovedTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(6);
    });
  });

  test("should show confirm popup when click select approve all bulk with only error failLotsAlreadyApproved", async () => {
    const history = createMemoryHistory();
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    ApproveDataSCMService.approveDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        failLotsAlreadyApproved: ["72a7f9d1", "72a7f9d2", "72a7f9d3"],
        failLotsNotFinalised: [],
        failLotsNotFound: [],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      fireEvent.click(screen.getByTestId("button-approve-all"));
      fireEvent.click(screen.getByText("confirmApproveAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allUnSuccessApprovedTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show message call function click button approve one row error failLotsNotFinalised", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    const history = createMemoryHistory();
    ApproveDataSCMService.approveDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        failLotsAlreadyApproved: [],
        failLotsNotFinalised: ["71a7f961"],
        failLotsNotFound: [],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("button-approve")[0]);
      fireEvent.click(screen.getByText("confirmApproveOneRow"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allUnSuccessApprovedOneRow")
      ).toBeInTheDocument();
    });
  });

  test("should show message call function click button approve with error not found api", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    const history = createMemoryHistory();
    ApproveDataSCMService.approveDataSCM = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("button-approve")[0]);
      fireEvent.click(screen.getByText("confirmApproveOneRow"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
      expect(screen.getByText("confirmApproveOneRow")).toBeInTheDocument();
    });
  });

  //List test check action Delete

  test("should show confirm popup when click delete row button", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("button-delete")[0]);
    });
    await waitFor(() => {
      expect(screen.getByText("titleDeleteOneRow")).toBeInTheDocument();
      expect(screen.getByText("descriptionDeleteOneRow")).toBeInTheDocument();
      expect(screen.getByText("confirmDeleteOneRow")).toBeInTheDocument();
      expect(screen.getByText("cancel")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and delete bulk one row success", async () => {
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["71a769d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("checkbox-select")[0]);
      fireEvent.click(screen.getByTestId("bulk-action-menu"));
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteOneRow"));
    });
    await waitFor(() => {
      expect(screen.getByText("allSuccessDeleteOneRow")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and delete bulk these row success", async () => {
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      const checkboxSelect = screen.getAllByTestId("checkbox-select");
      fireEvent.click(checkboxSelect[0]);
      fireEvent.click(checkboxSelect[1]);
      fireEvent.click(screen.getByTestId("bulk-action-menu"));
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteTheseRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("allSuccessDeleteTheseRows")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click checkbox all delete bulk with status succeed and failLotsAlreadyFinalised", async () => {
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0"],
        failLotsAlreadyFinalised: ["71a719d0", "71a7f960", "75a7f9d0"],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      expect(bulkActionBtn).not.toBeDisabled();
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteAllRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("successDeleteTheseRows")).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessDeleteTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(3);
    });
  });

  test("should show confirm popup when click checkbox all delete bulk with only status succeed", async () => {
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0", "71a719d0", "71a7f960", "75a7f9d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
    fireEvent.click(screen.getByTestId("bulk-action-menu"));
    fireEvent.click(screen.getByTestId("button-delete-all"));
    fireEvent.click(screen.getByText("confirmDeleteAllRows"));
    await waitFor(() => {
      expect(screen.getByText("allSuccessDeleteTheseRows")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select delete all bulk with error failLotsNotFound and failLotsAlreadyFinalised", async () => {
    const history = createMemoryHistory();
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        succeed: [],
        failLotsAlreadyFinalised: ["7147f9d0"],
        failLotsNotFound: ["71a7f5d0", "71a719d0", "71a7f960", "75a7f9d0"],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteAllRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("unSuccessDeleteTheseRows")).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessDeleteTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(5);
    });
  });

  test("should show message call function click button delete all row with only error failLotsAlreadyFinalised", async () => {
    const history = createMemoryHistory();
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        succeed: [],
        failLotsAlreadyFinalised: [
          "7147f9d0",
          "71a7f5d0",
          "71a719d0",
          "71a7f960",
          "75a7f9d0",
        ],
        failLotsNotFound: [],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allUnSuccessDeleteTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show message call function click button delete with error not found api", async () => {
    const history = createMemoryHistory();
    DeleteDataSCMService.deleteDataSCM = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-delete-all"));
      fireEvent.click(screen.getByText("confirmDeleteAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
      expect(screen.getByText("confirmDeleteAllRows")).toBeInTheDocument();
    });
  });

  //List test check action Finalise

  test("should show confirm popup when click finalise row button", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("button-approve")[0]);
    });
    await waitFor(() => {
      expect(screen.getByText("titlefinaliseOneRow")).toBeInTheDocument();
      expect(screen.getByText("descriptionFinaliseOneRow")).toBeInTheDocument();
      expect(screen.getByText("confirmFinaliseOneRow")).toBeInTheDocument();
      expect(screen.getByText("cancel")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and finalise bulk one row success", async () => {
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("checkbox-select")[0]);
      fireEvent.click(screen.getByTestId("bulk-action-menu"));
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseOneRow"));
    });
    await waitFor(() => {
      expect(screen.getByText("allSuccessFinaliseOneRow")).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select checkbox and finalise bulk these row success", async () => {
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      const checkboxSelect = screen.getAllByTestId("checkbox-select");
      fireEvent.click(checkboxSelect[0]);
      fireEvent.click(checkboxSelect[1]);
      fireEvent.click(screen.getByTestId("bulk-action-menu"));
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseTheseRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click checkbox all finalise bulk with status succeed and failLotsAlreadyFinalised", async () => {
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0"],
        failLotsAlreadyFinalised: ["71a719d0", "71a7f960", "75a7f9d0"],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      expect(bulkActionBtn).not.toBeDisabled();
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseAllRows"));
    });
    await waitFor(() => {
      expect(screen.getByText("successFinaliseTheseRows")).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(3);
    });
  });

  test("should show confirm popup when click checkbox all finalise bulk with only status succeed", async () => {
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockResolvedValue({
      status: 200,
      data: {
        succeed: ["7147f9d0", "71a7f5d0", "71a719d0", "71a7f960", "75a7f9d0"],
        failLotsAlreadyFinalised: [],
        failLotsNotFound: [],
      },
    });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      fireEvent.click(screen.getByTestId("bulk-action-menu"));
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show confirm popup when click select finalise all bulk with error failLotsNotFound and failLotsAlreadyFinalised", async () => {
    const history = createMemoryHistory();
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        succeed: [],
        failLotsAlreadyFinalised: ["7147f9d0"],
        failLotsNotFound: ["71a7f5d0", "71a719d0", "71a7f960", "75a7f9d0"],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("unSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
      expect(
        screen.getByText("quantityUnSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
      const contentError = screen.getByTestId("error-container");
      expect(contentError).toBeInTheDocument();
      const dataError = screen.getAllByTestId("content-detail");
      expect(dataError.length).toEqual(5);
    });
  });

  test("should show message call function click button finalise all row with only error failLotsAlreadyFinalised", async () => {
    const history = createMemoryHistory();
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockRejectedValue({
      status: 409,
      data: {
        succeed: [],
        failLotsAlreadyFinalised: [
          "7147f9d0",
          "71a7f5d0",
          "71a719d0",
          "71a7f960",
          "75a7f9d0",
        ],
        failLotsNotFound: [],
      },
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("allUnSuccessFinaliseTheseRows")
      ).toBeInTheDocument();
    });
  });

  test("should show message call function click button finalise all row with error not found api", async () => {
    const history = createMemoryHistory();
    FinaliseDataSCMService.finaliseDataSCM = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getByTestId("checkbox-all").children[0]);
      const bulkActionBtn = screen.getByTestId("bulk-action-menu");
      fireEvent.click(bulkActionBtn);
      fireEvent.click(screen.getByTestId("button-finalise-all"));
      fireEvent.click(screen.getByText("confirmFinaliseAllRows"));
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
      expect(screen.getByText("confirmFinaliseAllRows")).toBeInTheDocument();
    });
  });

  test("handle test select and unselect checkbox", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.PROVIDER });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    await waitFor(() => {
      fireEvent.click(screen.getAllByTestId("checkbox-select")[0]);
    });
    fireEvent.click(screen.getByTestId("bulk-action-menu"));
    expect(screen.getByTestId("button-finalise-all")).toBeInTheDocument();
    expect(screen.getByTestId("button-delete-all")).toBeInTheDocument();
    expect(
      document.getElementsByClassName("box-bulk selected")[0]
    ).toBeDefined();
  });

  test("handle test change winery", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    await act(async () => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData1[0].lotNumber));
    });
    const selectWinery = screen.getByTestId("select-winery");
    expect(selectWinery).toBeInTheDocument();
    fireEvent.mouseDown(selectWinery.children[0]);
    const optionWinery2 = screen.getByText("1235 - Winery 2");
    expect(optionWinery2).toBeInTheDocument();
    fireEvent.click(optionWinery2);
    await waitFor(() => {
      expect(screen.getByText("1235 - Winery 2")).toBeInTheDocument();
      const getDataSCMService = GetDataSCMService.getDataTable as jest.Mock;
      expect(getDataSCMService).toBeCalled();
    });
  });

  test("handle test sort SCM data and check data render table", async () => {
    GetDataSCMService.getDataTable = jest
      .fn()
      .mockResolvedValue({ data: tableData2 });
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText(tableData2[0].lotNumber));
    });
    await waitFor(() => {
      expect(screen.getAllByTestId("table-scm-row").length).toEqual(
        tableData2.length
      );
    });
    expect(screen.getAllByTestId("icon-check").length).toEqual(
      tableData2.length - 1
    );
    expect(screen.getAllByText("approved").length).toEqual(1);
    const origin = screen.getByText("origin");
    fireEvent.click(origin);
    // check sort Origin with rule alphabetically asc
    expect(screen.getAllByTestId("table-scm-row")[0].textContent).toContain(
      tableData2[0].origin
    );
    fireEvent.click(origin);
    // check sort Origin with rule alphabetically desc
    expect(screen.getAllByTestId("table-scm-row")[0].textContent).toContain(
      tableData2[5].origin
    );
  });

  test("should render the modal when isOpen is true", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    fireEvent.click(screen.getByText("uploadANewCSVFile"));
    fireEvent.click(screen.getByText("uploadScmCsvFile"));
    // @TODO - We need to check the title here to differentiate SCM from bottle mapping
    fireEvent.click(screen.getByText("browseFiles"));
    fireEvent.click(screen.getByText("done"));
  });

  test("check handle error get scm data", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    const responseError = {
      data: {
        meta: {
          message: "authorize",
        },
      },
    };
    GetDataSCMService.getDataTable = jest.fn().mockRejectedValue(responseError);
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(screen.getByText("authorize")).toBeInTheDocument();
    });
  });

  test("handle test get scm data with error not found api", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    GetDataSCMService.getDataTable = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
    });
  });

  test("handle test get list winery with error not found api", async () => {
    const history = createMemoryHistory();
    WineryService.getUserWinery = jest.fn().mockRejectedValue({
      status: 404,
      data: {},
    });
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    await waitFor(() => {
      expect(
        screen.getByText("somethingWentWrongPleaseTryAgain")
      ).toBeInTheDocument();
    });
  });

  test("should render upload scm file when click upload a bottle new CSV file", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    fireEvent.click(screen.getByText("uploadANewCSVFile"));
    expect(screen.getByText("titleUpload")).toBeInTheDocument();
    expect(screen.getByText("descriptionUpload")).toBeInTheDocument();
    const buttonUploadSCM = screen.getByText("uploadScmCsvFile");
    expect(buttonUploadSCM).toBeInTheDocument();
    fireEvent.click(buttonUploadSCM);
    const titleUploadSCM = screen.getByText("scmDataTitle");
    expect(titleUploadSCM).toBeInTheDocument();
    expect(screen.getByText("browseFiles")).toBeInTheDocument();
    expect(screen.getByText("done")).toBeInTheDocument();
  });

  test("should render upload bottles mapping when click upload a bottle mapping CSV file", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    fireEvent.click(screen.getByText("uploadANewCSVFile"));
    expect(screen.getByText("titleUpload")).toBeInTheDocument();
    expect(screen.getByText("descriptionUpload")).toBeInTheDocument();
    const buttonUploadBottleMappingFile = screen.getByText(
      "uploadBottleMappingCsvFile"
    );
    expect(buttonUploadBottleMappingFile).toBeInTheDocument();
    fireEvent.click(buttonUploadBottleMappingFile);
    const titleUploadBottleMappingFile = screen.getByText("bottleMappingTitle");
    expect(titleUploadBottleMappingFile).toBeInTheDocument();
    expect(screen.getByText("browseFiles")).toBeInTheDocument();
    expect(screen.getByText("done")).toBeInTheDocument();
  });

  test("should render poper language when click avatar", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.WINERY });
    await act(async () => {
      render(
        <Router history={history}>
          <AuthProvider>
            <Home />
          </AuthProvider>
        </Router>
      );
    });
    const avatarName = screen.getByText("W1");
    expect(avatarName).toBeInTheDocument();
    const buttonAvatar = screen.getByTestId("avatar");
    fireEvent.click(buttonAvatar);
    await waitFor(() => {
      const userName = screen.getByTestId("content-user");
      expect(userName).toBeInTheDocument();
      const selectLanguage = screen.getByTestId("select-language");
      expect(selectLanguage).toBeInTheDocument();
      const buttonSignOut = screen.getByTestId("btn-logout");
      expect(buttonSignOut).toBeInTheDocument();
    });
  });

  test("should render page view bottle mapping", async () => {
    const history = createMemoryHistory();
    history.push("/", { role: ROLE_SYSTEM.ADMIN });
    act(() => {
      render(
        <Router history={history}>
          <Home />
        </Router>
      );
    });
    fireEvent.click(screen.getByTestId("view-bottle-mapping"));
    expect(openMock).toHaveBeenCalledTimes(1);
    expect(openMock).toHaveBeenCalledWith(
      expect.stringContaining("/bottles/"),
      "_blank"
    );
  });
});

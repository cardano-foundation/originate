import { act, render, screen, waitFor } from "@testing-library/react";
import { ViewBottleMappingDataSCMService } from "../../../services";
import { ViewBottleMappingFile } from "./ViewBottleMapping";

jest.mock("react-router-dom", () => ({
  useParams: jest.fn(() => ({ id: "1234" })),
}));

jest.mock("../../../services", () => ({
  ViewBottleMappingDataSCMService: {
    viewBottleMapping: jest.fn().mockResolvedValue({
      status: 200,
      data: {
        scheduled: [],
        success: [
          {
            id: "ukhissuEipDm",
            lotId: "90005678901",
            sequentialNumber: 1,
            reelNumber: 1,
            certificateId: null,
          },
        ],
        error: [],
      },
    }),
  },
}));

describe("ViewBottleMapping test", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test("should show data when click function view bottle mapping file success", async () => {
    const id = "1234";
    const responseSuccess = {
      status: 200,
      data: {
        success: [
          {
            id: "ukhissuEipDm",
            lotId: "90005678901",
            sequentialNumber: 1,
            reelNumber: 1,
            certificateId: null,
          },
        ],
        scheduled: [],
        error: [],
      },
    };

    ViewBottleMappingDataSCMService.viewBottleMapping = jest
      .fn()
      .mockResolvedValue(responseSuccess);
    act(() => {
      render(<ViewBottleMappingFile />);
    });
    await waitFor(() => {
      expect(ViewBottleMappingDataSCMService.viewBottleMapping).toBeCalledWith(
        id
      );
      const expectedText = `success: ${JSON.stringify(
        responseSuccess.data.success,
        null,
        5
      )}`;
      const scheduledElement = screen.getByTestId("content-success");
      expect(scheduledElement).toContainHTML(expectedText);
    });
  });

  test("should show data when click function view bottle mapping file type scheduled", async () => {
    const id = "1234";
    const responseSuccess = {
      status: 200,
      data: {
        scheduled: [
          {
            id: "ukhissuEipDm",
            lotId: "90005678901",
            sequentialNumber: 1,
            reelNumber: 1,
            certificateId: null,
          },
        ],
        success: [],
        error: [],
      },
    };
    ViewBottleMappingDataSCMService.viewBottleMapping = jest
      .fn()
      .mockResolvedValue(responseSuccess);
    act(() => {
      render(<ViewBottleMappingFile />);
    });
    await waitFor(() => {
      expect(ViewBottleMappingDataSCMService.viewBottleMapping).toBeCalledWith(
        id
      );
      const expectedText = `scheduled: ${JSON.stringify(
        responseSuccess.data.scheduled,
        null,
        5
      )}`;
      const scheduledElement = screen.getByTestId("content-scheduled");
      expect(scheduledElement).toContainHTML(expectedText);
    });
  });

  test("should show data when click function view bottle mapping file type error", async () => {
    const id = "1234";
    const responseSuccess = {
      status: 200,
      data: {
        scheduled: [],
        success: [],
        error: [
          {
            id: "ukhissuEipDm",
            lotId: "90005678901",
            sequentialNumber: 1,
            reelNumber: 1,
            certificateId: null,
          },
        ],
      },
    };
    ViewBottleMappingDataSCMService.viewBottleMapping = jest
      .fn()
      .mockResolvedValue(responseSuccess);
    act(() => {
      render(<ViewBottleMappingFile />);
    });
    await waitFor(() => {
      expect(ViewBottleMappingDataSCMService.viewBottleMapping).toBeCalledWith(
        id
      );
      const expectedText = `error: ${JSON.stringify(
        responseSuccess.data.error,
        null,
        5
      )}`;
      const scheduledElement = screen.getByTestId("content-error");
      expect(scheduledElement).toContainHTML(expectedText);
    });
  });

  test("should show data when click function view bottle mapping file data error", async () => {
    const id = "1234";
    const responseError = {
      data: {
        meta: {
          code: "401",
          message:
            "Invalid or expired token. Please obtain a new token by the keycloak login api",
        },
      },
    };
    ViewBottleMappingDataSCMService.viewBottleMapping = jest
      .fn()
      .mockRejectedValue(responseError);
    act(() => {
      render(<ViewBottleMappingFile />);
    });
    await waitFor(() => {
      expect(ViewBottleMappingDataSCMService.viewBottleMapping).toBeCalledWith(
        id
      );
      const errorAlert = screen.getByText(
        `${JSON.stringify(responseError.data)}`
      );
      expect(errorAlert).toBeInTheDocument();
    });
  });
});

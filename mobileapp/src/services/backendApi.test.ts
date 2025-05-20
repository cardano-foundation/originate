import { waitFor } from "@testing-library/react";
import { BackendAPI } from "./backendApi";
import { axiosClient } from "./axiosClient";

jest.mock("./axiosClient", () => ({
  axiosClient: {
    get: jest.fn(),
    put: jest.fn(),
  },
}));

describe("Backend API helpers", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test("should call axiosClient.get with the correct URL", async () => {
    await BackendAPI.getCert("1234");

    expect(axiosClient.get).toHaveBeenCalledWith("certs/winery/1234");
  });

  test("should return the response from axiosClient.get", async () => {
    const mockResponse = {
      data: "mocked response",
      status: 200,
      statusText: "OK",
      headers: {},
      config: {},
    };
    jest.spyOn(axiosClient, "get").mockResolvedValue(mockResponse);

    const response = await BackendAPI.getCert("1234");
    await waitFor(() => {
      expect(response).toEqual(mockResponse);
    });
  });

  test("should call axiosClient.get with the correct URL for getBottleByLot", async () => {
    await BackendAPI.getBottleByLot("12345678912", "1234");

    expect(axiosClient.get).toHaveBeenCalledWith(
      "bottles/1234/lots/12345678912"
    );
  });

  test("should return the response from axiosClient.get for getBottleByLot", async () => {
    const mockResponse = {
      data: [{ id: "bottle1", lotId: "lot123", certificateId: null }],
      status: 200,
      statusText: "OK",
      headers: {},
      config: {},
    };
    jest.spyOn(axiosClient, "get").mockResolvedValue(mockResponse);
    const response = await BackendAPI.getBottleByLot("lot123", "winery456");
    await waitFor(() => {
      expect(response).toEqual(mockResponse);
    });
  });

  test("should call axiosClient.put with the correct URL for saveAndContinue", async () => {
    const data = { add: [], remove: [], finalise: false };
    await BackendAPI.saveAndContinue("winery456", "lot123", "cert456", data);

    expect(axiosClient.put).toHaveBeenCalledWith(
      "bottles/winery456/certs/cert456/lot123",
      data
    );
  });

  test("should call axiosClient.get with the correct URL for getWinery", async () => {
    await BackendAPI.getWinery();

    expect(axiosClient.get).toHaveBeenCalledWith("user/winery");
  });

  test("should call axiosClient.get with the correct URL for getInfoBottle", async () => {
    await BackendAPI.getInfoBottle("winery456", "bottle123");

    expect(axiosClient.get).toHaveBeenCalledWith(
      "bottles/winery456/bottle/bottle123"
    );
  });
});

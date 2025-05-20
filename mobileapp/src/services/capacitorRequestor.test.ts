import { CapacitorHttp, HttpResponse } from "@capacitor/core";
import { Requestor } from "@openid/appauth";
import { XhrSettings } from "ionic-appauth/lib/cordova";
import { CapacitorRequestor } from "./capacitorRequestor";

jest.mock("@capacitor/core", () => ({
  CapacitorHttp: {
    request: jest.fn(),
  },
}));
describe("CapacitorRequestor", () => {
  let requestor: Requestor;
  const xhrSettings: XhrSettings = {
    method: "GET",
    url: "http://example.com",
    headers: {},
    data: {},
  };

  beforeEach(() => {
    requestor = new CapacitorRequestor();
    (CapacitorHttp.request as jest.MockedFn<any>).mockClear();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("should make a request with the provided settings", async () => {
    const responseData = { foo: "bar" };
    const mockResponse: HttpResponse = {
      data: responseData,
      headers: {},
      status: 200,
      url: xhrSettings.url,
    };
    (CapacitorHttp.request as jest.MockedFn<any>).mockResolvedValue(
      mockResponse
    );

    const result = await requestor.xhr<object>(xhrSettings);

    expect(CapacitorHttp.request).toHaveBeenCalledWith({
      method: xhrSettings.method,
      url: xhrSettings.url,
      headers: xhrSettings.headers,
      data: xhrSettings.data,
    });
    expect(result).toEqual(responseData);
  });

  test("should default to GET method if not provided in settings", async () => {
    const responseData = { foo: "bar" };
    const mockResponse: HttpResponse = {
      data: responseData,
      headers: {},
      status: 200,
      url: xhrSettings.url,
    };
    (CapacitorHttp.request as jest.MockedFn<any>).mockResolvedValue(
      mockResponse
    );
    delete xhrSettings.method; // Simulating missing method in the settings

    await requestor.xhr<object>(xhrSettings);

    expect(CapacitorHttp.request).toHaveBeenCalledWith({
      method: "GET",
      url: xhrSettings.url,
      headers: xhrSettings.headers,
      data: xhrSettings.data,
    });
  });
});

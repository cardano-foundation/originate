import { Requestor } from "@openid/appauth";
import { XhrSettings } from "ionic-appauth/lib/cordova";
import { CapacitorHttp } from "@capacitor/core";

class CapacitorRequestor implements Requestor {
  async xhr<T>(settings: XhrSettings): Promise<T> {
    if (!settings.method) {
      settings.method = "GET";
    }

    const response = await CapacitorHttp.request({
      method: settings.method,
      url: settings.url,
      headers: settings.headers,
      data: settings.data,
    });
    return response.data as T;
  }
}

export { CapacitorRequestor };

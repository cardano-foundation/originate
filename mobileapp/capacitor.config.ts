import { CapacitorConfig } from "@capacitor/cli";
import * as dotenv from "dotenv";

/**
 * Get the env file content.
 */
dotenv.config({ path: "./.env" });

/**
 * Building capacitor config.
 */
const config: CapacitorConfig = {
  appId: "cf.proofoforigin.mobileapp",
  appName: "Bolnisi Scanning App",
  webDir: "build",
  bundledWebRuntime: false,
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      launchAutoHide: true,
      launchFadeOutDuration: 1000,
      backgroundColor: "#1D439B",
      androidScaleType: "CENTER",
      showSpinner: false,
      splashFullScreen: true,
    },
  },
  server: {
    // Specify hostname to change the origin of the webview.
    hostname: process.env.MOBILE_APP_HOST_NAME  
  }
};

export default config;

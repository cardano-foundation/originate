import { Preferences } from "@capacitor/preferences";
import { render, waitFor } from "@testing-library/react";
import { i18n } from "../../../i18n";
import { Landing } from "./Landing";

jest.mock("@capacitor/preferences", () => ({
  Preferences: {
    set: jest.fn(),
    get: jest.fn().mockImplementation(async ({ key }) => {
      if (key === "language") {
        return { value: null };
      } else if (key === "idToken") {
        return {
          value:
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2MjkyMjI1NDUsImxvY2FsZSI6ImthIiwibG9jYXRpb24iOiJrYSIsImVtYWlsIjoiam9obi5kb2VAZW1haWwuY29tIiwibG9jYXRpb25zIjpbImthIiwibGEiXSwiaWF0IjoxNjI5MjIyNTQ1LCJleHAiOjE2MzAwMjY1NDV9.46IJqC5hJy_z1Yr0vO1LIPc53aI9RT8lB_0NvlWn7I8",
        };
      }
    }),
  },
}));

describe("parseJwt", () => {
  test("should parse JWT and set language correctly", async () => {
    i18n.changeLanguage = jest.fn();

    render(<Landing />);

    await waitFor(() => {
      expect(i18n.changeLanguage).toHaveBeenCalledWith("ka");
      expect(Preferences.set).toHaveBeenCalledWith({
        key: "language",
        value: "ka",
      });
    });
  });
});

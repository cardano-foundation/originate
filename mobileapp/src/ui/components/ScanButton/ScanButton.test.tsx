import { fireEvent, render, waitFor } from "@testing-library/react";

import { ScanButton } from "./ScanButton";
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
describe("ScanButton", () => {
  test("should render the popup when isOpen is true", async () => {
    const handleClick = jest.fn();
    const { getByTestId } = render(<ScanButton onClick={handleClick} />);
    await waitFor(() => {
      const boxElement = getByTestId("box-btn-scan");

      expect(boxElement).toBeInTheDocument();

      const btnScan = getByTestId("button-scan");
      fireEvent.click(btnScan);

      expect(handleClick).toBeCalled();
    });
  });
});

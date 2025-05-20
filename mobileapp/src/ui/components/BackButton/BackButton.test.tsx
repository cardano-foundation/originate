import { render, fireEvent, waitFor } from "@testing-library/react";
import { BackButton } from "./BackButton";

describe("BackButton", () => {
  test("calls click back() when clicked", async () => {
    const handleBack = jest.fn();
    const { getByTestId } = render(<BackButton handleBack={handleBack} />);
    await waitFor(() => {
      fireEvent.click(getByTestId("back-btn"));
      expect(handleBack).toHaveBeenCalled();
    });
  });
});

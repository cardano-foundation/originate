import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { SelectComponent } from "./SelectComponent";

const handleChange = jest.fn();

describe("SelectComponent test", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  test("check show content and select options winery", () => {
    render(
      <SelectComponent
        value={"1"}
        handleChange={handleChange}
        options={[
          {
            value: "1",
            label: "Winery 1",
          },
          {
            value: "2",
            label: "Winery 2",
          },
          {
            value: "3",
            label: "Winery 3",
          },
        ]}
      />
    );
    const selectWinery = screen.getByTestId("select-winery");
    expect(selectWinery).toBeInTheDocument();
    const select = screen.getByRole("button");
    fireEvent.mouseDown(select);
    const optionWinery2 = screen.getByText("Winery 2");
    expect(optionWinery2).toBeInTheDocument();
    fireEvent.mouseDown(optionWinery2);
    fireEvent.click(optionWinery2);
    expect(screen.getByText("Winery 2")).toBeInTheDocument();
  });
});

import React from "react";
import { render, screen } from "@testing-library/react";
import { Page404 } from "./Page404";

test("renders App page", () => {
  render(<Page404 />);
  const linkElement = screen.getByText(/Page404/i);
  expect(linkElement).toBeInTheDocument();
});

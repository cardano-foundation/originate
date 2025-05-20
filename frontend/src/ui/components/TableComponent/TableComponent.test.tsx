import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import {
  TableBodyComponent,
  TableComponent,
  TableHeaderComponent,
} from "./TableComponent";
import { tableData1, tableData2 } from "../../../__mocks__/tableMock";
import { headCellsAdmin, headCellsWinery } from "../../utils/HeaderCellsData";
import { ROLE_SYSTEM } from "../../constants/auth";

const handleCheckAll = jest.fn();
const handleSortHandler = jest.fn();
const handleCheck = jest.fn();
const handleDeleteOne = jest.fn();
const handleFinaliseOne = jest.fn();
const handleApproveOne = jest.fn();

describe("TableComponent test", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  test("check show content and select options winery", () => {
    render(
      <TableComponent>
        <TableHeaderComponent
          numSelected={1}
          rowCount={tableData1.length}
          order={"asc"}
          orderBy={""}
          onSelectAllClick={handleCheckAll}
          onSortHandler={handleSortHandler}
          headCells={headCellsAdmin}
        />
        <TableBodyComponent
          selected={["1"]}
          data={tableData1}
          role={ROLE_SYSTEM.ADMIN}
          handleClickCheck={handleCheck}
          onClickOneDelete={handleDeleteOne}
          onClickOneFinalise={handleFinaliseOne}
          onClickOneApprove={handleApproveOne}
        />
      </TableComponent>
    );
    const listTableCell = screen.getAllByTestId("table-scm-row");
    expect(listTableCell.length).toEqual(tableData1.length);
    expect(screen.getByText("lotNumber")).toBeInTheDocument();
    fireEvent.click(screen.getByText("lotNumber"));
    expect(handleSortHandler).toHaveBeenCalled();
    expect(screen.getByTestId("lotNumber").children[1]).toBeInTheDocument();
    expect(screen.getByText("finalise/delete").children).toHaveLength(0);
    expect(screen.getAllByTestId("icon-check").length).toEqual(
      tableData1.length - 2
    );
    expect(screen.getAllByTestId("icon-delete").length).toEqual(
      tableData1.length - 2
    );
    expect(screen.getAllByText("finalised").length).toEqual(1);
    expect(screen.getAllByText("approved").length).toEqual(1);
    const checkboxDisable = screen.getAllByTestId("checkbox-not-select");
    fireEvent.click(checkboxDisable[0]);
    expect(screen.queryAllByTestId("icon-checked").length).toEqual(0);
    fireEvent.click(screen.getAllByTestId("icon-delete")[0]);
    expect(handleDeleteOne).toHaveBeenCalled();
    fireEvent.click(screen.getAllByTestId("button-approve")[0]);
    expect(handleFinaliseOne).toHaveBeenCalled();
  });

  test("check show content render action with role Winery", () => {
    render(
      <TableComponent>
        <TableHeaderComponent
          numSelected={1}
          rowCount={tableData2.length}
          order={"asc"}
          orderBy={"lotNumber"}
          onSelectAllClick={handleCheckAll}
          onSortHandler={handleSortHandler}
          headCells={headCellsWinery}
        />
        <TableBodyComponent
          selected={["1"]}
          data={tableData2}
          role={ROLE_SYSTEM.WINERY}
          handleClickCheck={handleCheck}
          onClickOneDelete={handleDeleteOne}
          onClickOneFinalise={handleFinaliseOne}
          onClickOneApprove={handleApproveOne}
        />
      </TableComponent>
    );
    const listTableCell = screen.getAllByTestId("table-scm-row");
    expect(listTableCell.length).toEqual(tableData2.length);
    expect(screen.getByText("approved").children).toHaveLength(0);
    expect(screen.getAllByTestId("icon-check").length).toEqual(
      tableData2.length - 1
    );
    expect(screen.queryAllByTestId("icon-delete").length).toBe(0);
    expect(screen.getAllByText("approved").length).toEqual(1);
    fireEvent.click(screen.getAllByTestId("button-approve")[0]);
    expect(handleApproveOne).toHaveBeenCalled();
    const checkboxAll = screen.getByTestId("checkbox-all").children[0];
    fireEvent.click(checkboxAll);
    expect(screen.getAllByTestId("checkbox-select").length).toEqual(
      tableData2.length - 1
    );
    expect(handleCheckAll).toHaveBeenCalled();
  });

  test("check content when select all checkbox", () => {
    render(
      <TableComponent>
        <TableHeaderComponent
          numSelected={tableData1.length}
          rowCount={tableData1.length}
          order={"asc"}
          orderBy={""}
          onSelectAllClick={handleCheckAll}
          onSortHandler={handleSortHandler}
          headCells={headCellsAdmin}
        />
      </TableComponent>
    );
    expect(screen.getByTestId("icon-checkbox-all")).toBeInTheDocument();
  });
});

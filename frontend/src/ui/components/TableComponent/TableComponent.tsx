/* eslint-disable no-unused-vars */
import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import { Box, Button, Checkbox, TableSortLabel, Tooltip } from "@mui/material";
import { useTranslation } from "react-i18next";

import "./style.scss";
import { Order } from "../../utils/SortData";
import { ROLE_SYSTEM } from "../../constants/auth";
import { TableEntry } from "./TableComponent.types";
import { IconCheck } from "../../assets/icons/IconCheck";
import { IconDelete } from "../../assets/icons/IconDelete";
import {
  IconCheckedBox,
  IconIndeterminate,
  IconUnCheckBox,
} from "../../assets/icons/IconCheckBox";
import { LOT_STATUS_VALUE } from "../../constants";
import { calculateHeightTable } from "../../utils/calculateHeightTable";
import useWindowSize from "../../hooks/useWindowSize";

interface IRenderAction {
  role: string;
  onClickOneFinalise: () => void;
  onClickOneDelete: () => void;
  onClickOneApprove: () => void;
}

const RenderAction = ({
  role,
  onClickOneFinalise,
  onClickOneDelete,
  onClickOneApprove,
}: IRenderAction) => {
  return (
    <div className="btn-group">
      <Button
        data-testid="button-approve"
        variant="contained"
        className="btn-finalise"
        onClick={
          role === ROLE_SYSTEM.ADMIN || role === ROLE_SYSTEM.PROVIDER
            ? onClickOneFinalise
            : onClickOneApprove
        }
      >
        <IconCheck />
      </Button>
      {(role === ROLE_SYSTEM.ADMIN || role === ROLE_SYSTEM.PROVIDER) && (
        <Button
          data-testid="button-delete"
          className="btn-delete"
          variant="contained"
          onClick={onClickOneDelete}
        >
          <IconDelete />
        </Button>
      )}
    </div>
  );
};
interface HeadCellsInterface {
  id: string;
  numeric: boolean;
  label: string;
  minWidth: string;
  sort: boolean;
}

interface ITableHeaderComponent {
  numSelected: number;
  rowCount: number;
  order: Order;
  orderBy?: string;
  headCells: HeadCellsInterface[];
  onSelectAllClick: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onSortHandler: (id: string) => void;
}

const TableHeaderComponent = ({
  numSelected,
  rowCount,
  order,
  orderBy,
  headCells,
  onSelectAllClick,
  onSortHandler,
}: ITableHeaderComponent) => {
  const { t } = useTranslation();

  return (
    <TableHead sx={{ background: "#DDE3F0" }}>
      <TableRow
        sx={{
          th: {
            padding: "15px 15px",
          },
        }}
        className="header"
      >
        <TableCell
          padding="checkbox"
          className="item-header"
        >
          <Checkbox
            data-testid="checkbox-all"
            color="primary"
            indeterminate={numSelected > 0 && numSelected < rowCount}
            checked={rowCount > 0 && numSelected === rowCount}
            onChange={(e) => onSelectAllClick(e)}
            checkedIcon={
              <div data-testid="icon-checkbox-all">
                <IconIndeterminate />
              </div>
            }
            icon={<IconUnCheckBox />}
            indeterminateIcon={<IconIndeterminate />}
            sx={{
              color: "#ccc",
              padding: "0px 0px 4px",
              "&.Mui-checked": {},
            }}
            inputProps={{
              "aria-label": "select all desserts",
            }}
          />
        </TableCell>
        {headCells.map((header, index) => (
          <TableCell
            key={header.id}
            sx={{
              minWidth: header.minWidth,
              fontSize: "16px",
              fontWeight: 600,
              color: "#030321 ",
            }}
            align={index === headCells.length - 1 ? "center" : "left"}
            className={["item-header", !header.sort ? "fix-header" : ""].join(
              " "
            )}
          >
            {header.sort ? (
              <TableSortLabel
                active={orderBy === header.id}
                direction={orderBy === header.id ? order : "asc"}
                onClick={() => onSortHandler(header.id)}
                data-testid={header.id}
              >
                <Tooltip
                  placement="bottom-start"
                  title={t(`${header.label}`)}
                >
                  <span className="header-names">{t(`${header.label}`)}</span>
                </Tooltip>
              </TableSortLabel>
            ) : (
              t(`${header.label}`)
            )}
          </TableCell>
        ))}
      </TableRow>
    </TableHead>
  );
};

interface ITableBodyComponent {
  selected: string[];
  data: TableEntry[];
  role: string;
  handleClickCheck: (id: string) => void;
  onClickOneDelete: (id: string) => void;
  onClickOneFinalise: (id: string) => void;
  onClickOneApprove: (id: string) => void;
}

const TableBodyComponent = ({
  selected,
  data,
  role,
  handleClickCheck,
  onClickOneDelete,
  onClickOneFinalise,
  onClickOneApprove,
}: ITableBodyComponent) => {
  const { t } = useTranslation();
  const isSelected = (name: string) => selected.indexOf(name) !== -1;
  return (
    <TableBody>
      {data.map((row) => {
        const isItemSelected = isSelected(row.lotNumber);
        const isDisabled: boolean =
          role === ROLE_SYSTEM.WINERY
            ? row.status === LOT_STATUS_VALUE.APPROVED
            : row.status === LOT_STATUS_VALUE.APPROVED ||
              row.status === LOT_STATUS_VALUE.FINALISED;
        return (
          <TableRow
            key={row.lotNumber}
            sx={{
              "&:last-child td, &:last-child th": { border: 0 },
              "td,th": {
                fontSize: "14px",
                fontWeight: 500,
                color: "#030321 ",
                padding: "5px 15px",
              },
            }}
            className={`${isDisabled ? "disable-box" : ""}`}
            data-testid="table-scm-row"
          >
            <TableCell padding="checkbox">
              <Checkbox
                checked={isItemSelected}
                onClick={() => handleClickCheck(row.lotNumber)}
                checkedIcon={<IconCheckedBox />}
                icon={<IconUnCheckBox />}
                sx={{
                  color: "#ccc",
                  padding: "0px 0px 4px",

                  "&.Mui-checked": {},
                }}
                className={`${isDisabled ? "disable-checkbox" : ""}`}
                disabled={isDisabled}
                inputProps={
                  {
                    // "aria-labelledby": labelId,
                  }
                }
                data-testid={`${
                  isDisabled ? "checkbox-not-select" : "checkbox-select"
                }`}
              />
            </TableCell>
            <TableCell
              component="th"
              scope="row"
              align="left"
            >
              {row.lotNumber}
            </TableCell>
            <TableCell align="left">{row.wineName}</TableCell>
            <TableCell align="left">{row.origin}</TableCell>
            <TableCell align="left">{row.countryOfOrigin}</TableCell>
            <TableCell align="left">{row.producedBy}</TableCell>
            <TableCell align="left">{row.producerAddress}</TableCell>
            <TableCell align="left">{row.producerLatitude}</TableCell>
            <TableCell align="left">{row.producerLongitude}</TableCell>
            <TableCell align="left">{row.varietalName}</TableCell>
            <TableCell align="left">{row.vintageYear}</TableCell>
            <TableCell align="left">{row.wineType}</TableCell>
            <TableCell align="left">{row.wineColor}</TableCell>
            <TableCell align="left">{row.harvestDate}</TableCell>
            <TableCell align="left">{row.harvestLocation}</TableCell>
            <TableCell align="left">{row.pressingDate}</TableCell>
            <TableCell align="left">{row.processingLocation}</TableCell>
            <TableCell align="left">{row.fermentationVessel}</TableCell>
            <TableCell align="left">{row.fermentationDuration}</TableCell>
            <TableCell align="left">{row.agingRecipient}</TableCell>
            <TableCell align="left">{row.agingTime}</TableCell>
            <TableCell align="left">{row.storageVessel}</TableCell>
            <TableCell align="left">{row.bottlingDate}</TableCell>
            <TableCell align="left">{row.bottlingLocation}</TableCell>
            <TableCell align="left">{row.numberOfBottles}</TableCell>
            <TableCell
              align="center"
              style={{
                position: "sticky",
                right: 0,
                background: `${isDisabled ? "#D6EEE8" : "white"}`,
                borderLeft: "1px solid #CCCCCC",
              }}
            >
              {isDisabled ? (
                <Box
                  className="text-finalised"
                  id="finalised"
                >
                  <p>
                    {row.status === LOT_STATUS_VALUE.APPROVED
                      ? t("approved")
                      : t("finalised")}
                  </p>
                </Box>
              ) : (
                <RenderAction
                  role={role}
                  onClickOneDelete={() => onClickOneDelete(row.lotNumber)}
                  onClickOneFinalise={() => onClickOneFinalise(row.lotNumber)}
                  onClickOneApprove={() => onClickOneApprove(row.lotNumber)}
                />
              )}
            </TableCell>
          </TableRow>
        );
      })}
    </TableBody>
  );
};

const TableComponent = ({ children }: { children: any }) => {
  const windowSize = useWindowSize();
  const elementTable = React.useRef<HTMLDivElement | null>(null);
  const maxHeight = calculateHeightTable(
    windowSize.width,
    windowSize.height,
    elementTable.current?.offsetTop || 0
  );

  return (
    <Paper
      sx={{ width: "100%", overflow: "hidden" }}
      ref={elementTable}
    >
      <TableContainer sx={{ maxHeight: maxHeight }}>
        <Table
          stickyHeader
          aria-label="sticky table"
        >
          {children}
        </Table>
      </TableContainer>
    </Paper>
  );
};

export { TableComponent, TableHeaderComponent, TableBodyComponent };

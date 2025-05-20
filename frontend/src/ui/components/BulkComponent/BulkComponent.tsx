import { Menu, MenuItem } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { IconDropDown } from "../../assets/icons/IconDropDown";
import { IconCheck } from "../../assets/icons/IconCheck";
import { IconDelete } from "../../assets/icons/IconDelete";
import { ROLE_SYSTEM } from "../../constants/auth";
import "./style.scss";

interface IBulkComponent {
  selected: string[];
  onFinalise: () => void;
  onDelete: () => void;
  onApprove: () => void;
  role: string;
}

const BulkComponent = ({
  selected,
  onFinalise,
  onDelete,
  onApprove,
  role,
}: IBulkComponent) => {
  const { t } = useTranslation();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    if (selected.length <= 0) return;
    setAnchorEl(event.currentTarget);
  };

  const handleApprove = () => {
    if (selected.length <= 0) return;
    onApprove();
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleFinalise = () => {
    onFinalise();
    setAnchorEl(null);
  };

  const handleDelete = () => {
    onDelete();
    setAnchorEl(null);
  };
  return (
    <>
      {role === ROLE_SYSTEM.ADMIN || role === ROLE_SYSTEM.PROVIDER ? (
        <>
          <div
            data-testid="bulk-action-menu"
            className={["box-bulk", selected.length > 0 ? "selected" : ""].join(
              " "
            )}
            onClick={(e) => handleClick(e)}
          >
            <p className="text">{t("bulkAction")}</p>
            {selected.length > 0 && (
              <>
                <p className="count">{`(${selected.length})`}</p>
                <IconDropDown />
              </>
            )}
          </div>
          <Menu
            open={open}
            anchorEl={anchorEl}
            onClose={handleClose}
            id="basic-menu"
            MenuListProps={{
              "aria-labelledby": "basic-button",
            }}
            sx={{
              "& .MuiPaper-root": {
                minWidth: "350px",
                marginTop: "10px",
                ul: {
                  padding: 0,
                  border: "1px solid #ccc",
                  borderRadius: "4px",
                },
              },
            }}
          >
            <MenuItem
              data-testid="button-finalise-all"
              className="menu-item"
              sx={{
                paddingInline: "20px",
                borderBottom: "1px solid #ccc",
              }}
              onClick={handleFinalise}
            >
              <IconCheck
                color="#20C198"
                width="22"
              />
              <p>{t("finaliseAll", { selected: selected.length })}</p>
            </MenuItem>
            <MenuItem
              data-testid="button-delete-all"
              className="menu-item"
              sx={{
                paddingInline: "20px",
              }}
              onClick={handleDelete}
            >
              <IconDelete />
              <p>{t("deleteAll", { selected: selected.length })}</p>
            </MenuItem>
          </Menu>
        </>
      ) : (
        <div
          className={[
            "box-bulk-winery",
            selected.length > 0 ? "selected" : "",
          ].join(" ")}
          data-testid="button-approve-all"
          onClick={handleApprove}
        >
          {selected.length > 0 && <IconCheck color="#FFFFFF" />}
          <p
            className="text"
            style={{ marginLeft: selected.length > 0 ? "10px" : "0px" }}
         >
            {t("approve")}
          </p>
          {selected.length > 0 && (
            <p className="count">{`(${selected.length})`}</p>
          )}
        </div>
      )}
    </>
  );
};

export { BulkComponent };

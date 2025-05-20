import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import "./style.scss";
import { IconExpand } from "../../assets/icons/iconExpandDown";

const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: 243,
    },
  },
};

interface IProps {
  value: string;
  // eslint-disable-next-line no-unused-vars
  handleChange: (e: SelectChangeEvent<string>) => void;
  options: { label: string; value: string | number }[];
}

const SelectComponent = ({ value, handleChange, options }: IProps) => {
  const { t } = useTranslation();

  return (
    <div>
      <FormControl
        className="select-container"
        sx={{ minWidth: 350 }}
      >
        <InputLabel id="lable-select-winery">{t("chooseAWinery")}</InputLabel>
        <Select
          value={value}
          onChange={(e: SelectChangeEvent<string>) => handleChange(e)}
          label="Choose a winery"
          sx={{
            height: "52px",
          }}
          IconComponent={(_props) => {
            const rotate = _props.className.toString().includes("iconOpen");
            return (
              <div
                style={{
                  position: "absolute",
                  cursor: "pointer",
                  pointerEvents: "none",
                  right: rotate ? 10 : 10,
                  top: rotate ? 10 : 15,
                  transform: rotate ? "rotate(180deg)" : "none",
                }}
              >
                <IconExpand />
              </div>
            );
          }}
          data-testid="select-winery"
          MenuProps={MenuProps}
        >
          {options &&
            options.length > 0 &&
            options.map((el) => (
              <MenuItem
                key={el.value}
                value={el.value}
                sx={{
                  fontWeight: value === el.value ? "700" : "500",
                }}
              >
                <Box className="label-select">{el.label}</Box>
              </MenuItem>
            ))}
        </Select>
      </FormControl>
    </div>
  );
};

export { SelectComponent };

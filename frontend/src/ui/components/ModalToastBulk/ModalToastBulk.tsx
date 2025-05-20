import { useTranslation } from "react-i18next";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Button,
  Modal,
  Typography,
} from "@mui/material";
import { IconClose } from "../../assets/icons/IconClose";
import "./style.scss";
import { IconExpand } from "../../assets/icons/iconExpandDown";
import { IconCloseToastBulk } from "../../assets/icons/IconCloseToastBulk";
import { IDataModalToastBulk } from "../../pages/Home";

interface IModalToastBulk {
  isModal: boolean;
  data: IDataModalToastBulk;
  onClose: () => void;
}

export const ModalToastBulk = (props: IModalToastBulk) => {
  const { t } = useTranslation();

  return (
    <Modal
      open={props.isModal}
      onClose={props.onClose}
    >
      <div className="modal-toast-bulk">
        <div
          className="btn-close"
          onClick={props.onClose}
        >
          <IconClose />
        </div>
        <Box
          className={
            props.data.isError ? "text-header-error" : "text-header-success"
          }
        >
          {props.data.textHeader}
        </Box>
        <Box
          className="error-container"
          data-testid="error-container"
        >
          <Accordion className="error-content">
            <AccordionSummary
              expandIcon={<IconExpand />}
              className="container-header-error"
            >
              <Box className="content-header-error">
                <IconCloseToastBulk />
                <Typography>{props.data.textError}</Typography>
              </Box>
            </AccordionSummary>
            <Box
              className="error-content-body"
              sx={{
                marginBottom: props.data.dataError.length > 1 ? "10px" : "0px",
              }}
            >
              {props.data.dataError.map((item, index) => (
                <AccordionDetails
                  key={item.id}
                  className={
                    index === 0
                      ? "first-content"
                      : index === props.data.dataError.length - 1
                      ? "last-content"
                      : ""
                  }
                  sx={{
                    borderBottom:
                      props.data.dataError.length === 1
                        ? "unset"
                        : "1px solid #CCCCCC",
                  }}
                  data-testid="content-detail"
                >
                  <Box className="content-item">
                    <Typography>
                      {t("lotNumber")}: {item.id}
                    </Typography>
                    <Typography>{t(item.status)}</Typography>
                  </Box>
                </AccordionDetails>
              ))}
            </Box>
          </Accordion>
        </Box>
        <Button
          variant="contained"
          fullWidth
          className="confirm"
          onClick={props.onClose}
          data-testid="btn-close"
        >
          {t("ok")}
        </Button>
      </div>
    </Modal>
  );
};

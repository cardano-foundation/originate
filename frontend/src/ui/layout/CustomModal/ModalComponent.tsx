import { useTranslation } from "react-i18next";
import { Button, Modal } from "@mui/material";
import { IconClose } from "../../assets/icons/IconClose";
import "./style.scss";

interface IModalComponent {
  title: string;
  description: string;
  textConfirm: string;
  textCancel?: string;
  isModal: boolean;
  type: string;
  onConfirm: () => void;
  onCancel: () => void;
  onClose: () => void;
}

export const ModalComponent = (props: IModalComponent) => {
  const { t } = useTranslation();

  return (
    <Modal
      open={props.isModal}
      onClose={props.onClose}
    >
      <div className="modal-box">
        <div
          className="btn-close"
          onClick={props.onClose}
        >
          <IconClose />
        </div>
        <p className="title">{props.title}</p>
        <p className="des">{props.description}</p>
        <Button
          variant="contained"
          fullWidth
          className={["confirm", props.type === "DELETE" && "delete"].join(" ")}
          onClick={props.onConfirm}
        >
          {props.textConfirm}
        </Button>
        <Button
          variant="outlined"
          fullWidth
          className="cancel"
          onClick={props.onCancel}
        >
          {props.textCancel ?? t("cancel")}
        </Button>
      </div>
    </Modal>
  );
};

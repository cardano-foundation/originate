/* eslint-disable no-unused-vars */
import React, { useCallback, useRef, useState } from "react";
import { Box, Button, Modal } from "@mui/material";
import { useTranslation } from "react-i18next";
import { IconClose } from "../../assets/icons/IconClose";
import { IconUploadFile } from "../../assets/icons/IconUploadFile";
import { CSV_FILE_TYPE } from "../../constants";
import { UploadService } from "../../../services";
import { ALERT_TYPE } from "../../layout/CustomAlert/types";
import { convertErrorMessage } from "../../utils/MessageUtils";
import "./style.scss";

interface IModalUploadProps {
  isOpen: boolean;
  title?: string;
  onCancel?: () => void;
  wineryId: string;
  onOk?: () => void;
  onShowMessage?: (type: string, message: string) => void;
}

const ACCESS_FILE = ".csv";

export const ModalUpload = ({
  isOpen,
  onCancel,
  title = "Upload a SCM CSV file",
  onOk,
  onShowMessage,
  wineryId,
}: IModalUploadProps) => {
  const { t } = useTranslation();
  const inputRef = useRef<HTMLInputElement>(null);
  const [disable, setDisable] = useState<boolean>(false);
  const [disableDone, setDisableDone] = useState<boolean>(true);
  const [file, setFile] = useState<File | null>(null);

  const colorDisable = disable ? "#DEDEDE" : undefined;

  const handleUploadFile = useCallback(
    async (file: File) => {
      try {
        setDisable(true);
        setFile(file);
        let res;
        if (title === t("scmDataTitle")) {
          res = await UploadService.uploadWinerySCM(wineryId, file);
        } else {
          res = await UploadService.uploadBottleMapping(wineryId, file);
        }
        if (res) {
          setDisableDone(false);
          setDisable(true);
          onShowMessage &&
            onShowMessage(ALERT_TYPE.success, t("uploadSuccess"));
        }
      } catch (err: any) {
        setDisable(false);
        setFile(null);
        if (err?.data?.meta?.code) {
          onShowMessage &&
            onShowMessage(
              ALERT_TYPE.error,
              t(convertErrorMessage(err.data.meta.code))
            );
        } else {
          onShowMessage &&
            onShowMessage(
              ALERT_TYPE.error,
              t("somethingWentWrongPleaseTryAgain")
            );
        }
      } finally {
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      }
    },
    [onShowMessage, wineryId, title]
  );

  const handleChangeFile = useCallback(
    async (event: React.ChangeEvent<HTMLInputElement>) => {
      event.preventDefault();
      if (event.target instanceof HTMLInputElement) {
        if (!event.target.files) return;
        handleUploadFile(event.target.files[0]);
      }
    },
    [handleUploadFile]
  );

  const handleDrag = useCallback(
    (e: React.DragEvent<HTMLDivElement | HTMLFormElement>) => {
      e.preventDefault();
      e.stopPropagation();
    },
    []
  );

  const handleDrop = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      e.preventDefault();
      e.stopPropagation();
      if (disable) return;
      if (!CSV_FILE_TYPE.includes(e.dataTransfer?.files[0].type)) {
        onShowMessage && onShowMessage(ALERT_TYPE.error, t("wrongFormat"));
        return;
      }
      if (e.dataTransfer?.files && e.dataTransfer.files.length > 1) {
        onShowMessage &&
          onShowMessage(ALERT_TYPE.error, t("uploadMultipleFile"));
        return;
      }
      if (
        e.dataTransfer?.files &&
        e.dataTransfer?.files[0] &&
        CSV_FILE_TYPE.includes(e.dataTransfer?.files[0].type)
      ) {
        handleUploadFile(e.dataTransfer?.files[0]);
      }
    },
    [disable, handleUploadFile, onShowMessage]
  );

  return (
    <Modal
      open={isOpen}
      onClose={onCancel}
      slotProps={{
        backdrop: !disable
          ? {}
          : {
              onClick: (event) => {
                event.stopPropagation();
              },
            },
      }}
    >
      <form
        className="modal-box"
        onDragEnter={handleDrag}
      >
        <input
          data-testid="input-upload-file"
          accept={ACCESS_FILE}
          className="input-upload"
          ref={inputRef}
          type={"file"}
          onChange={(e) => handleChangeFile(e)}
          multiple={false}
        />
        {!disable && (
          <div
            className="btn-close"
            onClick={onCancel}
          >
            <IconClose />
          </div>
        )}
        <p className="title">{title}</p>
        <div
          className="drag-drop-container"
          onDrop={handleDrop}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          data-testid="drop-area"
        >
          <IconUploadFile
            width="141"
            height="104"
            color={colorDisable}
          />
          <Box
            sx={{
              fontWeight: "bold",
              fontSize: 20,
              mt: 4,
              color: colorDisable,
            }}
          >
            {t("dragAndDropAFileHere")}
          </Box>
          <Box sx={{ color: colorDisable, my: 2, fontSize: "18px" }}>
            {t("or")}
          </Box>
          <Button
            sx={{
              backgroundColor: "#1d439b",
              color: "#fff",
              py: 2,
              px: 3,
              borderRadius: 30,
              height: 52,
              textTransform: "unset",
            }}
            disabled={disable}
            variant="contained"
            onClick={() => inputRef.current?.click()}
          >
            {t("browseFiles")}
          </Button>
        </div>
        <Box
          data-testid="file-name"
          sx={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            my: 2,
            fontWeight: 500,
            color: "#030321",
          }}
        >
          {file?.name}
        </Box>
        <Button
          disabled={disableDone}
          variant="contained"
          fullWidth
          className="confirm"
          onClick={onOk}
          sx={{
            color: disableDone ? "#363C4A !important" : "#F6F6F6",
          }}
        >
          {t("done")}
        </Button>
      </form>
    </Modal>
  );
};

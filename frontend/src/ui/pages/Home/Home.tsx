import React, { useCallback, useEffect, useState } from "react";
import Container from "@mui/material/Container";
import {
  Avatar,
  Box,
  Button,
  CircularProgress,
  SelectChangeEvent,
} from "@mui/material";
import { useHistory, useLocation } from "react-router-dom";
import { AxiosResponse } from "axios";
import { useTranslation } from "react-i18next";
import {
  BulkComponent,
  ModalUpload,
  SelectComponent,
  TableBodyComponent,
  TableComponent,
  TableHeaderComponent,
} from "../../components";
import { getComparator, Order, stableSort } from "../../utils/SortData";
import { LOT_STATUS_VALUE, ROLE_SYSTEM, TYPE_MODAL } from "../../constants";
import { ModalComponent } from "../../layout/CustomModal";
import { CustomAlert } from "../../layout/CustomAlert";
import "./style.scss";
import { ALERT_TYPE } from "../../layout/CustomAlert/types";
import { useAuth } from "../../../contexts/AuthContext";
import { headCellsAdmin, headCellsWinery } from "../../utils/HeaderCellsData";
import { IconPlus } from "../../assets/icons/IconPlus";
import {
  DeleteDataSCMService,
  FinaliseDataSCMService,
  ApproveDataSCMService,
} from "../../../services";
import { GetDataSCMService } from "../../../services/GetDataSCMService";
import { WineryService } from "../../../services/WineryService";
import { TableEntry } from "../../components/TableComponent/TableComponent.types";
import { PopUpLanguage } from "../../components/PopUpLanguage";
import { ModalToastBulk } from "../../components/ModalToastBulk";
import { convertMessageAlert } from "../../utils/MessageAlertSCMData";
import { WINDOW_SIZE_WIDTH } from "../../constants/windowSize";
import { IconDocument } from "../../assets/icons/IconDocument";
import { convertName } from "../../utils/convertName";
import useWindowSize from "../../hooks/useWindowSize";

/* global process */
interface stateType {
  role: string;
}

interface IDataModal {
  title: string;
  description: string;
  textConfirm: string;
  type: string;
  onConfirm?: () => void;
  onCancel?: () => void;
  textCancel?: string;
}

export interface IDataModalToastBulk {
  textHeader: string;
  textError: string;
  dataError: Array<{
    id: string;
    status: string;
  }>;
  isError: boolean;
  onClose?: () => void;
}

interface IDropdownResponse {
  label: string;
  value: string;
}

interface IWineryResponse {
  wineryId: string;
  wineryName: string;
}
export interface IAlertMessage {
  type: string;
  message: string;
}

interface IModalUpload {
  isOpenUploadModal: boolean;
  title: string;
}

function Home() {
  const { t } = useTranslation();
  const { state } = useLocation<stateType>();
  const windowSize = useWindowSize();
  const isTablet = windowSize.width <= WINDOW_SIZE_WIDTH.TABLET;
  const isMobile = windowSize.width <= WINDOW_SIZE_WIDTH.MOBILE;
  const history = useHistory();
  const { validToken, userNameInfo, setUserNameInfo } = useAuth();
  const ROLE = state?.role ?? validToken?.role;

  const [order, setOrder] = useState<Order>("asc");
  const [orderBy, setOrderBy] = useState<keyof TableEntry>();
  const [selected, setSelected] = useState<string[]>([]);
  const [winery, setWinery] = useState<string>("");
  const [isModal, setIsModal] = useState<boolean>(false);
  const [wineryList, setWineryList] = useState<IDropdownResponse[]>([]);
  const [modalUpload, setModalUpload] = useState<IModalUpload>({
    isOpenUploadModal: false,
    title: t("scmDataTitle"),
  });
  const [dataTableNotFinalized, setDataTableNotFinalized] = useState<
    TableEntry[]
  >([]);
  const [dataTableFinalized, setDataTableFinalized] = useState<TableEntry[]>(
    []
  );
  const [isloading, setIsLoading] = useState<boolean>(false);
  const [dataModal, setDataModal] = useState<IDataModal>({
    title: "",
    description: "",
    textConfirm: "",
    type: "",
  });
  const [messageAlert, setMessageAlert] = useState<IAlertMessage>({
    type: ALERT_TYPE.success,
    message: "",
  });

  const [isOpenLanguageModal, setIsOpenLanguageModal] =
    useState<boolean>(false);

  const [modalToastBulk, setModalToastBulk] = useState<IDataModalToastBulk>({
    textHeader: "",
    textError: "",
    dataError: [],
    isError: false,
  });
  const [anchorEl, setAnchorEl] = useState<Element | null>(null);

  useEffect(() => {
    if (state) {
      setMessageAlert({
        type: ALERT_TYPE.success,
        message: t("successLogin"),
      });
    }
  }, [history, state]);

  useEffect(() => {
    const handlePageReload = (event: { preventDefault: () => void }) => {
      event.preventDefault();
      if (state) {
        history.replace({ ...history.location, state: undefined });
      }
    };

    window.addEventListener("beforeunload", handlePageReload);

    return () => {
      window.removeEventListener("beforeunload", handlePageReload);
    };
  }, [history, state]);

  useEffect(() => {
    if (winery) getDataTables();
  }, [winery]);

  useEffect(() => {
    getWineryList();
  }, []);

  const getWineryList = async () => {
    try {
      const response: AxiosResponse = await WineryService.getUserWinery();
      if (response?.data) {
        const dataDropdown: IDropdownResponse[] = [];
        response?.data.forEach((value: IWineryResponse) => {
          const data = {
            value: value.wineryId,
            label: value.wineryId + " - " + value.wineryName,
          };
          dataDropdown.push(data);
        });
        setWineryList(dataDropdown);
        if (ROLE === ROLE_SYSTEM.WINERY) {
          setUserNameInfo({
            userName: response.data[0]?.wineryName,
            isEmail: false,
          });
        }
        if (response.data.length > 0) {
          setWinery(response.data[0].wineryId);
        }
      }
    } catch (error: any) {
      if (error?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: error.data.meta.message,
        });
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };
  const getDataTables = async () => {
    setIsLoading(true);
    try {
      const response: AxiosResponse = await GetDataSCMService.getDataTable(
        winery
      );
      if (response?.data) {
        const arrFinalized: TableEntry[] = [];
        const arrNotfinalized: TableEntry[] = [];
        for (let i = 0; i < response?.data.length; i++) {
          if (
            response?.data[i].status === LOT_STATUS_VALUE.NOT_FINALISED &&
            (ROLE === ROLE_SYSTEM.ADMIN || ROLE === ROLE_SYSTEM.PROVIDER)
          ) {
            arrNotfinalized.push(response?.data[i]);
          } else if (
            response?.data[i].status === LOT_STATUS_VALUE.FINALISED &&
            ROLE === ROLE_SYSTEM.WINERY
          ) {
            arrNotfinalized.push(response?.data[i]);
          } else {
            arrFinalized.push(response?.data[i]);
          }
        }
        setDataTableFinalized(arrFinalized);
        setDataTableNotFinalized(arrNotfinalized);
        setIsLoading(false);
      }
    } catch (error: any) {
      setIsLoading(false);
      setDataTableFinalized([]);
      setDataTableNotFinalized([]);
      if (error?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: error?.data?.meta?.message || t("serverErr"),
        });
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };

  const handleDeleteLotIds = async (id?: string) => {
    const params: string[] = id ? [id] : selected;
    try {
      const response: AxiosResponse = await DeleteDataSCMService.deleteDataSCM(
        winery,
        params
      );
      if (response.status === 200) {
        const { isModalToastBulk, quantitySuccess, quantityError, dataErrors } =
          convertMessageAlert(response);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantitySuccess && quantitySuccess > 1
                ? t("successDeleteTheseRows", { quantity: quantitySuccess })
                : t("successDeleteOneRow", { quantity: quantitySuccess }),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessDeleteTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessDeleteOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: false,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.success,
            message:
              quantitySuccess && quantitySuccess > 1
                ? t("allSuccessDeleteTheseRows")
                : t("allSuccessDeleteOneRow"),
          });
        }
      }
      setSelected([]);
      setIsModal(false);
      getDataTables();
    } catch (err: any) {
      if (err.status === 409) {
        const { isModalToastBulk, quantityError, dataErrors, reason } =
          convertMessageAlert(err);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantityError && quantityError > 1
                ? t("unSuccessDeleteTheseRows")
                : t("unSuccessDeleteOneRow"),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessDeleteTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessDeleteOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: true,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.error,
            message:
              quantityError && quantityError > 1
                ? t("allUnSuccessDeleteTheseRows", { reason: t(`${reason}`) })
                : t("allUnSuccessDeleteOneRow", { reason: t(`${reason}`) }),
          });
        }
        setIsModal(false);
      } else if (err?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: err?.data?.meta?.message,
        });
        setIsModal(false);
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };

  const handleFinaliseLotIds = async (id?: string) => {
    const params: string[] = id ? [id] : selected;
    try {
      const response: AxiosResponse =
        await FinaliseDataSCMService.finaliseDataSCM(winery, params);
      if (response.status === 200) {
        const { isModalToastBulk, quantitySuccess, quantityError, dataErrors } =
          convertMessageAlert(response);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantitySuccess && quantitySuccess > 1
                ? t("successFinaliseTheseRows", { quantity: quantitySuccess })
                : t("successFinaliseOneRow", { quantity: quantitySuccess }),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessFinaliseTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessFinaliseOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: false,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.success,
            message:
              quantitySuccess && quantitySuccess > 1
                ? t("allSuccessFinaliseTheseRows")
                : t("allSuccessFinaliseOneRow"),
          });
        }
      }
      setSelected([]);
      setIsModal(false);
      getDataTables();
    } catch (err: any) {
      if (err.status === 409) {
        const { isModalToastBulk, quantityError, dataErrors, reason } =
          convertMessageAlert(err);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantityError && quantityError > 1
                ? t("unSuccessFinaliseTheseRows")
                : t("unSuccessFinaliseOneRow"),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessFinaliseTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessFinaliseOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: true,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.error,
            message:
              quantityError && quantityError > 1
                ? t("allUnSuccessFinaliseTheseRows", { reason: t(`${reason}`) })
                : t("allUnSuccessFinaliseOneRow", { reason: t(`${reason}`) }),
          });
        }
        setIsModal(false);
      } else if (err?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: err?.data?.meta?.message,
        });
        setIsModal(false);
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };

  const handleAprroveLotIds = async (id?: string) => {
    const params: string[] = id ? [id] : selected;
    try {
      const response: AxiosResponse =
        await ApproveDataSCMService.approveDataSCM(winery, params);
      if (response.status === 200) {
        const { isModalToastBulk, quantitySuccess, quantityError, dataErrors } =
          convertMessageAlert(response);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantitySuccess && quantitySuccess > 1
                ? t("successApprovedTheseRows", { quantity: quantitySuccess })
                : t("successApprovedOneRow", { quantity: quantitySuccess }),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessApprovedTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessApprovedOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: false,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.success,
            message:
              quantitySuccess && quantitySuccess > 1
                ? t("allSuccessApprovedTheseRows")
                : t("allSuccessApprovedOneRow"),
          });
        }
      }
      setSelected([]);
      setIsModal(false);
      getDataTables();
    } catch (err: any) {
      if (err.status === 409) {
        const { isModalToastBulk, quantityError, dataErrors, reason } =
          convertMessageAlert(err);
        if (isModalToastBulk) {
          setModalToastBulk({
            textHeader:
              quantityError && quantityError > 1
                ? t("unSuccessApprovedTheseRows")
                : t("unSuccessApprovedOneRow"),
            textError:
              quantityError && quantityError > 1
                ? t("quantityUnSuccessApprovedTheseRows", {
                    quantity: quantityError,
                  })
                : t("quantityUnSuccessApprovedOneRow", {
                    quantity: quantityError,
                  }),
            dataError: dataErrors || [],
            isError: true,
          });
        } else {
          setMessageAlert({
            type: ALERT_TYPE.error,
            message:
              quantityError && quantityError > 1
                ? t("allUnSuccessApprovedTheseRows", { reason: t(`${reason}`) })
                : t("allUnSuccessApprovedOneRow", { reason: t(`${reason}`) }),
          });
        }
        setIsModal(false);
      } else if (err?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: err?.data?.meta?.message,
        });
        setIsModal(false);
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };
  const handleOkUpload = () => {
    setModalUpload({ ...modalUpload, isOpenUploadModal: false });
    if (modalUpload.title === t("scmDataTitle")) {
      getDataTables();
    }
  };

  const handleCheck = (id: string) => {
    const selectedIndex = selected.indexOf(id);
    let newSelected: string[] = [];
    if (selectedIndex === -1) {
      newSelected = newSelected.concat(selected, id);
    } else {
      selected.splice(selectedIndex, 1);
      newSelected = [...selected];
    }
    setSelected(newSelected);
  };

  const handleCheckAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      const newSelected = dataTableNotFinalized.map((n) => n.lotNumber);
      setSelected(newSelected);
      return;
    }
    setSelected([]);
  };

  const handleSortHandler = (id: string) => {
    const isAsc = orderBy === id && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(id as keyof TableEntry);
  };

  const visibleRows = React.useMemo(() => {
    return stableSort<TableEntry>(
      dataTableNotFinalized,
      getComparator(order, orderBy)
    ).concat(dataTableFinalized);
  }, [dataTableFinalized, dataTableNotFinalized, order, orderBy]);

  const handleDeleteBulk = () => {
    if (selected.length === 1) {
      setDataModal({
        title: t("titleDeleteOneRow"),
        description: t("descriptionDeleteOneRow"),
        textConfirm: t("confirmDeleteOneRow"),
        type: TYPE_MODAL.DELETE,
        onConfirm: () => {
          handleDeleteLotIds();
        },
      });
    } else if (
      selected.length ===
      visibleRows.length - dataTableFinalized.length
    ) {
      setDataModal({
        title: t("titleDeleteAllRows"),
        description: t("descriptionDeleteAllRows"),
        textConfirm: t("confirmDeleteAllRows"),
        type: TYPE_MODAL.DELETE,
        onConfirm: () => {
          handleDeleteLotIds();
        },
      });
    } else {
      setDataModal({
        title: t("titleDeleteTheseRows"),
        description: t("descriptionDeleteTheseRows"),
        textConfirm: t("confirmDeleteTheseRows"),
        type: TYPE_MODAL.DELETE,
        onConfirm: () => {
          handleDeleteLotIds();
        },
      });
    }
    setIsModal(true);
  };

  const handleFinaliseBulk = () => {
    if (selected.length === 1) {
      setDataModal({
        title: t("titlefinaliseOneRow"),
        description: t("descriptionFinaliseOneRow"),
        textConfirm: t("confirmFinaliseOneRow"),
        type: TYPE_MODAL.APPROVE,
        onConfirm: () => {
          handleFinaliseLotIds();
        },
      });
    } else if (
      selected.length ===
      visibleRows.length - dataTableFinalized.length
    ) {
      setDataModal({
        title: t("titlefinaliseAllRows"),
        description: t("descriptionFinaliseAllRows"),
        textConfirm: t("confirmFinaliseAllRows"),
        type: TYPE_MODAL.APPROVE,
        onConfirm: () => {
          handleFinaliseLotIds();
        },
      });
    } else {
      setDataModal({
        title: t("titlefinaliseTheseRows"),
        description: t("descriptionFinaliseTheseRows"),
        textConfirm: t("confirmFinaliseTheseRows"),
        type: TYPE_MODAL.APPROVE,
        onConfirm: () => {
          handleFinaliseLotIds();
        },
      });
    }
    setIsModal(true);
  };

  const handleApproveBulk = () => {
    if (selected.length === 1) {
      setDataModal({
        title: t("titleApproveOneRow"),
        description: t("descriptionApproveOneRow"),
        textConfirm: t("confirmApproveOneRow"),
        type: TYPE_MODAL.APPROVE,
        onConfirm() {
          handleAprroveLotIds();
        },
      });
    } else if (
      selected.length ===
      visibleRows.length - dataTableFinalized.length
    ) {
      setDataModal({
        title: t("titleApproveAllRows"),
        description: t("descriptionApproveAllRows"),
        textConfirm: t("confirmApproveAllRows"),
        type: TYPE_MODAL.APPROVE,
        onConfirm() {
          handleAprroveLotIds();
        },
      });
    } else {
      setDataModal({
        title: t("titleApproveTheseRows"),
        description: t("descriptionApproveTheseRows"),
        textConfirm: t("confirmApproveTheseRows"),
        type: TYPE_MODAL.APPROVE,
        onConfirm() {
          handleAprroveLotIds();
        },
      });
    }
    setIsModal(true);
  };

  const handleDeleteOne = (id: string) => {
    setDataModal({
      title: t("titleDeleteOneRow"),
      description: t("descriptionDeleteOneRow"),
      textConfirm: t("confirmDeleteOneRow"),
      type: TYPE_MODAL.DELETE,
      onConfirm: () => {
        handleDeleteLotIds(id);
      },
    });
    setIsModal(true);
  };

  const handleFinaliseOne = (id: string) => {
    setDataModal({
      title: t("titlefinaliseOneRow"),
      description: t("descriptionFinaliseOneRow"),
      textConfirm: t("confirmFinaliseOneRow"),
      type: TYPE_MODAL.APPROVE,
      onConfirm: () => {
        handleFinaliseLotIds(id);
      },
    });
    setIsModal(true);
  };

  const handleApproveOne = (id: string) => {
    setDataModal({
      title: t("titleApproveOneRow"),
      description: t("descriptionApproveOneRow"),
      textConfirm: t("confirmApproveOneRow"),
      type: TYPE_MODAL.APPROVE,
      onConfirm() {
        handleAprroveLotIds(id);
      },
    });
    setIsModal(true);
  };

  const handleSelectWinery = (e: SelectChangeEvent<string>) => {
    setWinery(e.target.value);
    setSelected([]);
  };

  const handleClickViewBottle = () => {
    window.open(
      `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}/bottles/${winery}`,
      "_blank"
    );
  };

  const handleOpenUploadConfirm = () => {
    setIsModal(true);
    setDataModal({
      title: t("titleUpload"),
      type: TYPE_MODAL.APPROVE,
      description: t("descriptionUpload"),
      textConfirm: t("uploadScmCsvFile"),
      textCancel: t("uploadBottleMappingCsvFile"),
      onConfirm: () => {
        setIsModal(false);
        setModalUpload({
          title: t("scmDataTitle"),
          isOpenUploadModal: true,
        });
      },
      onCancel: () => {
        setIsModal(false);
        setModalUpload({
          title: t("bottleMappingTitle"),
          isOpenUploadModal: true,
        });
      },
    });
  };

  const handleShowMessageAlert = useCallback(
    (type: string, message: string) => {
      setMessageAlert({
        type: type,
        message: message,
      });
    },
    []
  );

  const handleClearMsg = useCallback(() => {
    setMessageAlert((prev) => ({
      ...prev,
      message: "",
    }));
  }, []);

  const handleClickAvatar = (event: {
    currentTarget: React.SetStateAction<Element | null>;
  }) => {
    setIsOpenLanguageModal(true);
    setAnchorEl(event.currentTarget);
  };

  return (
    <Container
      maxWidth={false}
      sx={{
        padding: "50px 0",
      }}
    >
      {ROLE === ROLE_SYSTEM.WINERY ? (
        isTablet || isMobile ? (
          <Box className="winery-action-bulk-container">
            <div className="winery-content-avatar">
              <Avatar
                className="box-avatar"
                onClick={handleClickAvatar}
                data-testid="avatar"
              >
                {convertName(userNameInfo?.userName, userNameInfo?.isEmail)}
              </Avatar>
            </div>

            <BulkComponent
              selected={selected}
              role={ROLE}
              onDelete={handleDeleteBulk}
              onFinalise={handleFinaliseBulk}
              onApprove={handleApproveBulk}
            />
          </Box>
        ) : (
          <Box>
            <div className="winery-content-avatar">
              <Avatar
                className="box-avatar"
                onClick={handleClickAvatar}
                data-testid="avatar"
              >
                {convertName(userNameInfo?.userName, userNameInfo?.isEmail)}
              </Avatar>
            </div>

            <BulkComponent
              selected={selected}
              role={ROLE}
              onDelete={handleDeleteBulk}
              onFinalise={handleFinaliseBulk}
              onApprove={handleApproveBulk}
            />
          </Box>
        )
      ) : isTablet || isMobile ? (
        <Box>
          <div className="box-right">
            <Box sx={{ display: "inherit", gap: isMobile ? "20px" : "25px" }}>
              <Button
                variant="outlined"
                className={
                  isMobile ? "btn-view-bottle-mobile" : "btn-view-bottle"
                }
                onClick={handleClickViewBottle}
                endIcon={isMobile ? <IconDocument /> : null}
                data-testid="view-bottle-mapping"
              >
                {isMobile ? null : t("viewTheBottleMappingFile")}
              </Button>
              <Button
                variant="contained"
                className={
                  isMobile ? "btn-upload-csv-mobile" : "btn-upload-csv"
                }
                endIcon={<IconPlus />}
                onClick={handleOpenUploadConfirm}
              >
                {isMobile ? null : t("uploadANewCSVFile")}
              </Button>
            </Box>
            <Avatar
              className="box-avatar"
              onClick={handleClickAvatar}
              data-testid="avatar"
            >
              {convertName(userNameInfo?.userName, userNameInfo?.isEmail)}
            </Avatar>
          </div>
          <Box className="box-select-acion">
            <SelectComponent
              handleChange={handleSelectWinery}
              value={winery}
              options={wineryList}
            />
            <BulkComponent
              selected={selected}
              role={ROLE}
              onDelete={handleDeleteBulk}
              onFinalise={handleFinaliseBulk}
              onApprove={handleApproveBulk}
            />
          </Box>
        </Box>
      ) : (
        <>
          <div className="box-header">
            {!isTablet && !isMobile && (
              <SelectComponent
                handleChange={handleSelectWinery}
                value={winery}
                options={wineryList}
              />
            )}

            <div className="box-right">
              <>
                <Button
                  variant="outlined"
                  className="btn-view-bottle"
                  onClick={handleClickViewBottle}
                  data-testid="view-bottle-mapping"
                >
                  {t("viewTheBottleMappingFile")}
                </Button>
                <Button
                  variant="contained"
                  className="btn-upload-csv"
                  endIcon={<IconPlus />}
                  onClick={handleOpenUploadConfirm}
                >
                  {t("uploadANewCSVFile")}
                </Button>
              </>
              <Avatar
                className="box-avatar"
                onClick={handleClickAvatar}
                data-testid="avatar"
              >
                {convertName(userNameInfo?.userName, userNameInfo?.isEmail)}
              </Avatar>
            </div>
          </div>

          <BulkComponent
            selected={selected}
            role={ROLE}
            onDelete={handleDeleteBulk}
            onFinalise={handleFinaliseBulk}
            onApprove={handleApproveBulk}
          />
        </>
      )}
      {isloading ? (
        <Box
          sx={{
            position: "fixed",
            left: "50%",
            top: "50%",
            transform: "translate(-50%, -50%)",
          }}
        >
          <CircularProgress />
        </Box>
      ) : (
        <Box style={{ marginTop: "20px" }}>
          <TableComponent>
            <TableHeaderComponent
              numSelected={selected.length}
              rowCount={visibleRows.length - dataTableFinalized.length}
              order={order}
              orderBy={orderBy}
              onSelectAllClick={handleCheckAll}
              onSortHandler={handleSortHandler}
              headCells={
                ROLE === ROLE_SYSTEM.ADMIN || ROLE === ROLE_SYSTEM.PROVIDER
                  ? headCellsAdmin
                  : headCellsWinery
              }
            />
            <TableBodyComponent
              selected={selected}
              data={visibleRows}
              role={ROLE}
              handleClickCheck={handleCheck}
              onClickOneDelete={handleDeleteOne}
              onClickOneFinalise={handleFinaliseOne}
              onClickOneApprove={handleApproveOne}
            />
          </TableComponent>
        </Box>
      )}
      {modalUpload.isOpenUploadModal && (
        <ModalUpload
          wineryId={winery}
          title={modalUpload.title}
          isOpen={modalUpload.isOpenUploadModal}
          onShowMessage={handleShowMessageAlert}
          onOk={handleOkUpload}
          onCancel={() =>
            setModalUpload({ ...modalUpload, isOpenUploadModal: false })
          }
        />
      )}
      {isModal && (
        <ModalComponent
          isModal={isModal}
          title={dataModal.title}
          description={dataModal.description}
          textConfirm={dataModal.textConfirm}
          type={dataModal.type}
          textCancel={dataModal.textCancel}
          onClose={() => {
            setIsModal(false);
          }}
          onConfirm={dataModal.onConfirm ?? (() => setIsModal(false))}
          onCancel={dataModal.onCancel ?? (() => setIsModal(false))}
        />
      )}
      {!!modalToastBulk.dataError.length && (
        <ModalToastBulk
          isModal={!!modalToastBulk.dataError.length}
          data={modalToastBulk}
          onClose={() => {
            setModalToastBulk({
              textHeader: "",
              textError: "",
              dataError: [],
              isError: false,
            });
          }}
        />
      )}
      {isOpenLanguageModal && (
        <PopUpLanguage
          isModal={isOpenLanguageModal}
          anchorEl={anchorEl}
          onCancel={() => setIsOpenLanguageModal(false)}
          setMessageAlert={() =>
            setMessageAlert({
              type: ALERT_TYPE.error,
              message: t("somethingWentWrongPleaseTryAgain"),
            })
          }
        />
      )}
      {!!messageAlert.message && (
        <CustomAlert
          isOpen={!!messageAlert.message}
          severity={
            messageAlert.type === ALERT_TYPE.success ? "success" : "error"
          }
          message={messageAlert.message}
          onClearMessage={handleClearMsg}
        />
      )}
    </Container>
  );
}

export { Home };

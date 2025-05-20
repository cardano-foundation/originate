import Container from "@mui/material/Container";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { AxiosResponse } from "axios";
import { useTranslation } from "react-i18next";
import { ViewBottleMappingDataSCMService } from "../../../services/";
import "./style.scss";

import {
  ResponseViewBottole,
  ResponseViewBottoleError,
} from "./ViewBottle.types";

function ViewBottleMappingFile() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const [dataViewBottle, setDataViewBottle] = useState<ResponseViewBottole>();
  const [dataViewBottleError, setDataViewBottleError] =
    useState<ResponseViewBottoleError>();

  const handleClickViewBottle = async () => {
    try {
      const response: AxiosResponse =
        await ViewBottleMappingDataSCMService.viewBottleMapping(id);

      setDataViewBottle(response?.data);
      window.dispatchEvent(new Event("getViewBottleComplete"));
    } catch (error: any) {
      setDataViewBottleError(error?.data);
      window.dispatchEvent(new Event("getViewBottleComplete"));
    }
  };

  useEffect(() => {
    handleClickViewBottle();
  }, []);

  return (
    <Container
      maxWidth={false}
      className="content-viewBottles"
      data-testid="view-bottles"
    >
      {dataViewBottle && (
        <>
          <p data-testid="content-scheduled">
            {t("scheduled")}:{" "}
            {JSON.stringify(dataViewBottle.scheduled, null, 5)}
          </p>
          <p data-testid="content-success">
            {t("success")}: {JSON.stringify(dataViewBottle.success, null, 5)}
          </p>
          <p data-testid="content-error">
            {t("error")}: {JSON.stringify(dataViewBottle.error, null, 5)}
          </p>
        </>
      )}
      {dataViewBottleError && <p>{JSON.stringify(dataViewBottleError)}</p>}
    </Container>
  );
}
export { ViewBottleMappingFile };

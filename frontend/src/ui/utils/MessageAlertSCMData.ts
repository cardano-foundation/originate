import { AxiosResponse } from "axios";

interface IDataError {
  id: string;
  status: string;
}
interface IConvertMessageAlert {
  isModalToastBulk: boolean;
  quantitySuccess?: number;
  quantityError?: number;
  dataErrors?: Array<IDataError>;
  reason?: string;
}

export const convertMessageAlert = (
  response: AxiosResponse
): IConvertMessageAlert => {
  let succeed: string[] = [];
  let error: Array<IDataError> = [];
  let reason: string[] = [];
  if (
    response.data?.failLotsAlreadyApproved &&
    response.data.failLotsAlreadyApproved.length
  ) {
    reason = ["failLotsAlreadyApproved"];
    error = response.data.failLotsAlreadyApproved.map((id: string) => ({
      id,
      status: "failLotsAlreadyApproved",
    }));
  }
  if (
    response.data?.failLotsNotFinalised &&
    response.data.failLotsNotFinalised.length
  ) {
    reason.push("failLotsNotFinalised");
    error = [
      ...error,
      ...response.data.failLotsNotFinalised.map((id: string) => ({
        id,
        status: "failLotsNotFinalised",
      })),
    ];
  }
  if (
    response.data?.failLotsNotFound &&
    response.data.failLotsNotFound.length
  ) {
    reason.push("failLotsNotFound");
    error = [
      ...error,
      ...response.data.failLotsNotFound.map((id: string) => ({
        id,
        status: "failLotsNotFound",
      })),
    ];
  }
  if (
    response.data?.failLotsAlreadyFinalised &&
    response.data.failLotsAlreadyFinalised.length
  ) {
    reason.push("failLotsAlreadyFinalised");
    error = [
      ...error,
      ...response.data.failLotsAlreadyFinalised.map((id: string) => ({
        id,
        status: "failLotsAlreadyFinalised",
      })),
    ];
  }

  if (response.status === 200) {
    if (response.data?.failJobsNotScheduled) {
      succeed = response.data.succeed.concat(
        response.data.failJobsNotScheduled
      );
    } else {
      succeed = response.data.succeed;
    }
    if (error.length) {
      return {
        isModalToastBulk: true,
        quantitySuccess: succeed.length,
        quantityError: error.length,
        dataErrors: error,
      };
    }
    return {
      isModalToastBulk: false,
      quantitySuccess: succeed.length,
    };
  } else {
    if (reason.length > 1) {
      return {
        isModalToastBulk: true,
        quantityError: error.length,
        dataErrors: error,
      };
    }
    return {
      isModalToastBulk: false,
      quantityError: error.length,
      reason: reason[0],
    };
  }
};

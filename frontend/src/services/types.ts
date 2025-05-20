export interface IErrorResponse {
  meta: Error;
}

interface Error {
  code: string;
  message: string;
  internal_message: string;
}

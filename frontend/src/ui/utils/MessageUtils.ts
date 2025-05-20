export const convertErrorMessage = (code: string) => {
  switch (code) {
    case "500":
      return "serverErr";
    case "401":
      return "authorize";
    case "2":
      return "wrongFormat";
    case "3":
      return "dataInvalid";
    default:
      return "other";
  }
};

import { ROLE_SYSTEM } from "../../constants";

export const filterUnknownRoles = (arrayRoles: string[]) => {
  return arrayRoles.filter((role) => {
    return Object.values(ROLE_SYSTEM).includes(role);
  });
};

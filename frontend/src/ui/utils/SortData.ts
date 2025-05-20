/* eslint-disable no-unused-vars */
export type Order = "asc" | "desc";

export function stableSort<T>(
  array: readonly T[],
  comparator: (
    c: T | { [x: string]: number | string },
    d: T | { [x: string]: number | string }
  ) => number
) {
  const stabilizedThis = array.map((el, index) => [el, index] as [T, number]);
  stabilizedThis.sort((a, b) => {
    const order = comparator(a[0], b[0]);
    if (order !== 0) {
      return order;
    }
    return a[1] - b[1];
  });
  return stabilizedThis.map((el) => el[0]);
}

export function descendingComparator<T>(a: T, b: T, orderBy?: keyof T) {
  if (orderBy) {
    if (b[orderBy] < a[orderBy]) {
      return -1;
    }
    if (b[orderBy] > a[orderBy]) {
      return 1;
    }
  }
  return 0;
}

export function getComparator<T>(
  order: Order,
  orderBy?: keyof T
): (a: T, b: T) => number {
  return order === "desc"
    ? (a, b) => {
        return descendingComparator<T>(a, b, orderBy);
      }
    : (a, b) => -descendingComparator(a, b, orderBy);
}

import {
  BREAKPOINT_FOOTER,
  HEIGHT_FOOTER,
  WINDOW_SIZE_WIDTH,
} from "../constants/windowSize";

export const calculateHeightTable = (
  width: number,
  height: number,
  positionTable: number
) => {
  if (width <= WINDOW_SIZE_WIDTH.SMALL_MOBILE)
    return `${
      height - positionTable - HEIGHT_FOOTER.RESPONSIVE_SMALL_MOBILE
    }px`;
  if (width <= WINDOW_SIZE_WIDTH.TABLET && width <= BREAKPOINT_FOOTER)
    return `${height - positionTable - HEIGHT_FOOTER.RESPONSIVE}px`;
  return `${height - positionTable - HEIGHT_FOOTER.NORMAL}px`;
};

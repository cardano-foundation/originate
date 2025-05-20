interface IIcon {
  width?: string;
  height?: string;
  color?: string;
}

export const IconCheck = ({ width, height, color }: IIcon) => {
  return (
    <svg
      width={width || "20"}
      height={height || "16"}
      viewBox="0 0 20 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      data-testid="icon-check"
    >
      <path
        d="M7.02467 15.2333L0.0830078 8.29158L1.77467 6.62909L7.02467 11.8791L18.1955 0.708252L19.8872 2.37075L7.02467 15.2333Z"
        fill={color || "#030321"}
      />
    </svg>
  );
};

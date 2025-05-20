interface IIcon {
  width?: string;
  height?: string;
  color?: string;
  style?: any;
}
export const IconDropDown = (props: IIcon) => {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 22 22"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      style={props.style}
    >
      <path
        d="M11 14.3916L5.1792 8.57081L6.48545 7.28748L11 11.825L15.5146 7.31039L16.8209 8.59373L11 14.3916Z"
        fill="#F6F6F6"
      />
    </svg>
  );
};

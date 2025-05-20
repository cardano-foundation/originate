export const IconUnCheckBox = () => {
  return (
    <div
      style={{
        background: "#FFFFFF",
        border: "2px solid #CCCCCC",
        borderRadius: "5px",
        width: "28px",
        height: "28px",
      }}
    ></div>
  );
};

export const IconCheckedBox = () => {
  return (
    <div
      style={{
        background: "#DDE3F0",
        border: "2px solid #1D439B",
        borderRadius: "5px",
        width: "28px",
        height: "28px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
      data-testid="icon-checked"
    >
      <svg
        width="20"
        height="14"
        viewBox="0 0 20 14"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M7.23749 13.7165L0.791656 7.27067L2.36249 5.72692L7.23749 10.6019L17.6104 0.229004L19.1812 1.77275L7.23749 13.7165Z"
          fill="#1D439B"
        />
      </svg>
    </div>
  );
};

export const IconIndeterminate = () => {
  return (
    <div
      style={{
        background: "#DDE3F0",
        border: "2px solid #1D439B",
        borderRadius: "5px",
        width: "28px",
        height: "28px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <svg
        width="16"
        height="4"
        viewBox="0 0 16 4"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M0.156433 3.07273V0.927246H15.8436V3.07273H0.156433Z"
          fill="#1D439B"
        />
      </svg>
    </div>
  );
};

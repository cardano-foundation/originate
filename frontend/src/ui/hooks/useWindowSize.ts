import { useState, useEffect } from "react";

function useWindowSize() {
  const [windowSize, setWindowSize] = useState<
    Record<"width" | "height", number>
  >({
    width: 0,
    height: 0,
  });

  useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    }

    if (window) {
      window.addEventListener("load", handleResize);
      window.addEventListener("resize", handleResize);
      window.addEventListener("orientationchange", handleResize);
      window.addEventListener("getViewBottleComplete", handleResize);
      handleResize();
      return () => {
        window.removeEventListener("load", handleResize);
        window.removeEventListener("resize", handleResize);
        window.addEventListener("orientationchange", handleResize);
        window.addEventListener("getViewBottleComplete", handleResize);
      };
    }
  }, []);

  return windowSize;
}

export default useWindowSize;

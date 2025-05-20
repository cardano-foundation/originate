import { IonToast } from "@ionic/react";
import { createContext, ReactNode, useCallback, useState } from "react";
import { ToastMessageType } from "../../ui/common/types";
import "./style.scss";

// Create the context
interface ToastMessageProps {
  showToast: (message: string, type: ToastMessageType) => void;
}

export const ToastMessageContext = createContext<ToastMessageProps>({
  showToast: () => {},
});

// Create a component that will wrap your routes
export const ToastMessageProvider = ({ children }: { children: ReactNode }) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [message, setMessage] = useState<string>("");
  const [type, setType] = useState<ToastMessageType>(ToastMessageType.SUCCESS);

  const handleShowToast = useCallback((msg: string, type: ToastMessageType) => {
    setIsOpen(true);
    setMessage(msg);
    setType(type);
  }, []);

  return (
    <ToastMessageContext.Provider value={{ showToast: handleShowToast }}>
      {children}
      <IonToast
        position="top"
        isOpen={isOpen}
        duration={3000}
        message={message}
        cssClass={`default-toast ${
          type === ToastMessageType.SUCCESS ? "success-toast" : "error-toast"
        }`}
        data-testid="toast-message"
        onDidDismiss={() => setIsOpen(false)}
      />
    </ToastMessageContext.Provider>
  );
};

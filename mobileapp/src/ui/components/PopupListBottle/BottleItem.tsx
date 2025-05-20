import { IonIcon, IonLabel } from "@ionic/react";
import { useTranslation } from "react-i18next";
import { trash } from "../../assets/icons";
import "./style.scss";

interface BottleItemProps {
  bottleId: string;
  sequentialNumber: number;
  onDelete?: (id: string) => void;
}

export const BottleItem = ({
  bottleId,
  sequentialNumber,
  onDelete,
}: BottleItemProps) => {
  const { t } = useTranslation();
  return (
    <div
      className="bottle-item"
      data-testid="bottle-item"
    >
      <IonLabel className="bottle-label">
        <span
          className="bottle-name"
          data-testid="bottle-item-name"
        >
          {`${t("bottleItem", { sequentialNumber })}`}{" "}
        </span>
      </IonLabel>
      <IonIcon
        className="icon-delete"
        icon={trash}
        onClick={() => onDelete && onDelete(bottleId)}
        slot="end"
        data-testid="icon-delete-bottle"
      />
    </div>
  );
};

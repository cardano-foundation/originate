import { Avatar, Box, Button } from "@mui/material";
import Popover from "@mui/material/Popover";
import { useTranslation } from "react-i18next";
import { SelectLanguage } from "../SelectLanguage";
import { useAuth } from "../../../contexts/AuthContext";
import "./style.scss";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import { convertName } from "../../utils/convertName";

/* global process */

interface IModalLanguage {
  isModal: boolean;
  anchorEl?: Element | null;
  onCancel: () => void;
  setMessageAlert: () => void;
}

export const PopUpLanguage = (props: IModalLanguage) => {
  const { t } = useTranslation();
  const { userNameInfo } = useAuth();

  const handleClose = () => {
    props.onCancel();
  };

  const handleLogout = () => {
    try {
      keyCloakClient.logout({
        redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}`,
      });
    } catch (error) {
      props.setMessageAlert();
    }
  };

  return (
    <Popover
      open={props.isModal}
      anchorEl={props.anchorEl}
      onClose={handleClose}
      anchorOrigin={{
        vertical: "bottom",
        horizontal: "right",
      }}
      transformOrigin={{
        vertical: "bottom",
        horizontal: "right",
      }}
      PaperProps={{ className: "popover-language" }}
    >
      <div className="popup-language">
        {userNameInfo?.userName && (
          <Box
            className="user-content"
            data-testid="content-user"
          >
            <Avatar className="avatar-name">
              {convertName(userNameInfo.userName, userNameInfo.isEmail)}
            </Avatar>
            <p>{userNameInfo.userName}</p>
          </Box>
        )}
        <Box sx={{ marginTop: userNameInfo?.userName ? "unset" : "80px" }}>
          <SelectLanguage />
          <Button
            variant="contained"
            fullWidth
            className="confirm"
            onClick={handleLogout}
            data-testid="btn-logout"
          >
            {t("signOut")}
          </Button>
        </Box>
      </div>
    </Popover>
  );
};

import { Button, Checkbox, Link, Modal } from "@mui/material";
import { Trans, useTranslation } from "react-i18next";
import "./style.scss";
import { useState, useCallback } from "react";
import { Link as RouterLink } from "react-router-dom";
import { SelectLanguage } from "../SelectLanguage";
import {
  IconCheckedBox,
  IconUnCheckBox,
} from "../../assets/icons/IconCheckBox";
import { keyCloakClient } from "../../../services/Instances/KeyCloakServices";
import { RefreshTokenService, TermsAcceptService } from "../../../services";
import { useAuth } from "../../../contexts/AuthContext";
import { CustomAlert } from "../../layout/CustomAlert";
import { IAlertMessage } from "../../pages/Home";
import { ALERT_TYPE } from "../../layout/CustomAlert/types";
import { privacyURL } from "../../constants";

/* global process */

interface IModalComponent {
  isModal: boolean;
}
const ModalTermsOfServices = (props: IModalComponent) => {
  const { t } = useTranslation();
  const { validToken, setIsShowTermsConditions, setValidToken } = useAuth();
  const [isChecked, setIsChecked] = useState(false);

  const [messageAlert, setMessageAlert] = useState<IAlertMessage>({
    type: ALERT_TYPE.success,
    message: "",
  });

  const handleClearMsg = useCallback(() => {
    setMessageAlert((prev) => ({
      ...prev,
      message: "",
    }));
  }, []);

  const handleCheckboxChange = (event: { target: { checked: boolean } }) => {
    setIsChecked(event.target.checked);
  };

  const handleLogout = () => {
    try {
      keyCloakClient.logout({
        redirectUri: `${process.env.FRONTEND_DOMAIN_PUBLIC_URL}${process.env.FRONTEND_LOGIN_PATH}`,
      });
    } catch (error: any) {
      setMessageAlert({
        type: ALERT_TYPE.error,
        message: t("somethingWentWrongPleaseTryAgain"),
      });
    }
  };

  const handleContinue = async () => {
    try {
      if (isChecked) {
        const result = await TermsAcceptService.termsAccept();
        if (result.status === 204) {
          if (validToken?.refreshToken) {
            const { data } = await RefreshTokenService.refreshToken(
              validToken.refreshToken
            );
            setIsShowTermsConditions(true);
            setValidToken({
              accessToken: data?.access_token || "",
              role: validToken?.role || "",
              refreshToken: data?.refresh_token || "",
            });
          }
        }
      }
    } catch (error: any) {
      if (error?.data?.meta?.message) {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: error?.data?.meta?.message,
        });
      } else {
        setMessageAlert({
          type: ALERT_TYPE.error,
          message: t("somethingWentWrongPleaseTryAgain"),
        });
      }
    }
  };

  return (
    <>
      <Modal open={props.isModal}>
        <div className="modal-box">
          <div className="box-header">
            <div
              data-testid="titleHeaderModalTerm"
              className="title-terms"
            >
              {t("termsTitle")}
            </div>
            <SelectLanguage isOnlySelect={true} />
          </div>

          <div className="box-container">
            <div className="box-content">
              <div className="content-terms">
                <p className="header-text-terms">
                  <strong>{t("termsTopText")}</strong>
                </p>

                <p className="title-desc">1. {t("terms1")}</p>
                <ol type="a">
                  <li>{<Trans i18nKey="terms1.1" />}</li>
                  <li>{<Trans i18nKey="terms1.2" />}</li>
                  <li>{<Trans i18nKey="terms1.3" />}</li>
                  <li>{<Trans i18nKey="terms1.4" />}</li>
                  <li>
                    {
                      <Trans
                        i18nKey="terms1.5"
                        components={[
                          <RouterLink
                            target="_blank"
                            to={privacyURL}
                          />,
                        ]}
                      />
                    }
                  </li>
                  <li>{<Trans i18nKey="terms1.6" />}</li>
                  <li>{<Trans i18nKey="terms1.7" />}</li>
                </ol>

                <p className="title-desc">2. {t("terms2")}</p>
                <ol type="a">
                  <li>{<Trans i18nKey="terms2.1" />}</li>
                  <li>{<Trans i18nKey="terms2.2" />}</li>
                  <li>{<Trans i18nKey="terms2.3" />}</li>
                  <li>{<Trans i18nKey="terms2.4" />}</li>
                  <ul>
                    <li>{t("terms2.4.1")}</li>
                    <li>{t("terms2.4.2")}</li>
                    <li>{t("terms2.4.3")}</li>
                  </ul>
                  <li>{<Trans i18nKey="terms2.5" />}</li>
                </ol>

                <p className="title-desc">3. {t("terms3")}</p>
                <ol type="a">
                  <li>{<Trans i18nKey="terms3.1" />}</li>
                  <li>{<Trans i18nKey="terms3.2" />}</li>
                  <li>{<Trans i18nKey="terms3.3" />}</li>
                  <li>{<Trans i18nKey="terms3.4" />}</li>
                </ol>

                <p className="title-desc">4. {t("terms4")}</p>
                <p>{t("terms4Header")}</p>
                <ol type="a">
                  <li>{<Trans i18nKey="terms4.1" />}</li>
                  <li>{<Trans i18nKey="terms4.2" />}</li>
                  <li>{<Trans i18nKey="terms4.3" />}</li>
                  <li>{<Trans i18nKey="terms4.4" />}</li>
                </ol>

                <p className="title-desc">5. {t("terms5")}</p>
                <p>{t("terms5.1")}</p>
                <p>{t("terms5.2")}</p>

                <p className="title-desc">6. {t("terms6")}</p>
                <ol type="a">
                  <li>{<Trans i18nKey="terms6.1" />}</li>
                  <li>{<Trans i18nKey="terms6.2" />}</li>
                  <li>{<Trans i18nKey="terms6.3" />}</li>
                  <li>{<Trans i18nKey="terms6.4" />}</li>
                  <li>{<Trans i18nKey="terms6.5" />}</li>
                  <li>{<Trans i18nKey="terms6.6" />}</li>
                  <li>{<Trans i18nKey="terms6.7" />}</li>
                  <li>{<Trans i18nKey="terms6.8" />}</li>
                  <li>
                    {<Trans i18nKey="terms6.9" />}
                    <br />
                    <br />
                    Cardano Foundation,
                    <br />
                    Dammstrasse 16,
                    <br />
                    6300, Zug,
                    <br />
                    Switzerland
                    <br />
                    Attn: Legal
                  </li>
                </ol>
              </div>
            </div>
          </div>
          <div className="box-checkbox">
            <Checkbox
              checkedIcon={<IconCheckedBox />}
              icon={<IconUnCheckBox />}
              sx={{
                color: "#ccc",
                padding: "0px 0px 4px",
              }}
              data-testid="icon-check"
              checked={isChecked}
              onChange={handleCheckboxChange}
            />
            <label data-testid="label-check">{t("agreeTermsOfUse")}</label>
          </div>
          <Button
            variant="contained"
            fullWidth
            data-testid={
              !isChecked ? "button-continue-disable" : "button-continue"
            }
            className={`${!isChecked ? "confirm-disable" : "confirm"}`}
            disabled={!isChecked}
            onClick={handleContinue}
          >
            {t("continue")}
          </Button>
          <Link
            data-testid="btn-logout"
            onClick={handleLogout}
            className="title-sigOut"
          >
            {t("signOut")}
          </Link>
        </div>
      </Modal>

      <CustomAlert
        isOpen={!!messageAlert.message}
        severity={
          messageAlert.type === ALERT_TYPE.success ? "success" : "error"
        }
        message={messageAlert.message}
        onClearMessage={handleClearMsg}
      />
    </>
  );
};
export { ModalTermsOfServices };

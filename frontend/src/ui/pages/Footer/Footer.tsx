import { Container, Link } from "@mui/material";
import "./style.scss";
import { useTranslation } from "react-i18next";
import ROUTES from "../../../routes/contants/Routes";
import { useAuth } from "../../../contexts/AuthContext";
import useWindowSize from "../../hooks/useWindowSize";

function Footer() {
  const windowSize = useWindowSize();
  const { isShowTermsConditions, isAuthenticated } = useAuth();
  const { t } = useTranslation();
  const handleClickViewTermsConditions = () => {
    window.open(`${ROUTES.VIEW_TERMS_CONDITIONS_FRONTEND}`, "_blank");
  };
  const handleClickViewPrivacyPolicy = () => {
    window.open(`${ROUTES.VIEW_PRIVACY_POLICY}`, "_blank");
  };

  if (
    !isShowTermsConditions &&
    isAuthenticated &&
    window.location.pathname === ROUTES.HOME
  ) {
    return null;
  }
  const isCheckRenderLinkText =
    window.location.pathname === ROUTES.VIEW_TERMS_CONDITIONS_FRONTEND ||
    window.location.pathname === ROUTES.VIEW_TERMS_CONDITIONS_MOBILE ||
    window.location.pathname === ROUTES.VIEW_TERMS_CONDITIONS_API ||
    window.location.pathname === ROUTES.VIEW_PRIVACY_POLICY;

  return (
    <Container
      maxWidth={false}
      sx={{ backgroundColor: "#F6F6F6" }}
    >
      <div
        className={isCheckRenderLinkText ? "box-footer-not-link" : "box-footer"}
      >
        <p className="title-footer">{t("footerCopyright")}</p>
        {isCheckRenderLinkText ? (
          <></>
        ) : (
          <div className="box-link">
            <Link
              data-testid="viewTerms"
              onClick={handleClickViewTermsConditions}
            >
              {t("terms")}
            </Link>
            <Link
              data-testid="viewPrivacy"
              onClick={handleClickViewPrivacyPolicy}
            >
              {t("privacy")}
            </Link>
          </div>
        )}
      </div>
    </Container>
  );
}
export { Footer };

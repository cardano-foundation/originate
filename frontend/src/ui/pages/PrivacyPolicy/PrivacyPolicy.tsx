import Container from "@mui/material/Container";
import "./style.scss";
import { Trans, useTranslation } from "react-i18next";
import { SelectLanguage } from "../../components/SelectLanguage";

function PrivacyPolicy() {
  const { t } = useTranslation();

  return (
    <Container
      maxWidth={false}
      data-testid="viewPagePrivacy"
    >
      <div className="box-header-privacy">
        <div
          data-testid="headerPrivacy"
          className="title"
        >
          {t("privacy")}
        </div>
        <SelectLanguage isOnlySelect={true} />
      </div>
      <div className="box-content-privacy">
        <p>{<Trans i18nKey="privacyTopText1" />}</p>
        <p>{t("privacyTopText2")}</p>

        <p className="title-desc">1. {t("privacy1")}</p>
        <p>
          {t("privacy1.1")}
          <br />
          <br />
          Cardano Foundation,
          <br />
          Dammstrasse 16,
          <br />
          6300, Zug,
          <br />
          gdpr@cardanofoundation.org
        </p>

        <p className="title-desc">2. {t("privacy2")}</p>
        <p>{<Trans i18nKey="privacy2Header" />}</p>
        <ol type="a">
          <li>{<Trans i18nKey="privacy2.1" />}</li>
          <li>{<Trans i18nKey="privacy2.2" />}</li>
          <li>{<Trans i18nKey="privacy2.3" />}</li>
          <li>{<Trans i18nKey="privacy2.4" />}</li>
          <li>{<Trans i18nKey="privacy2.5" />}</li>
          <li>{<Trans i18nKey="privacy2.6" />}</li>
        </ol>

        <p className="title-desc">3. {t("privacy3")}</p>
        <p>{<Trans i18nKey="privacy3Header" />}</p>
        <ol type="a">
          <li>{<Trans i18nKey="privacy3.1" />}</li>
          <li>{<Trans i18nKey="privacy3.2" />}</li>
          <li>{<Trans i18nKey="privacy3.3" />}</li>
          <li>{<Trans i18nKey="privacy3.4" />}</li>
          <li>{<Trans i18nKey="privacy3.5" />}</li>
        </ol>

        <p className="title-desc">4. {t("privacy4")}</p>
        <p>{<Trans i18nKey="privacy4Header" />}</p>
        <ol type="a">
          <li>{<Trans i18nKey="privacy4.1" />}</li>
          <li>{<Trans i18nKey="privacy4.2" />}</li>
          <li>{<Trans i18nKey="privacy4.3" />}</li>
          <li>{<Trans i18nKey="privacy4.4" />}</li>
          <li>
            {<Trans i18nKey="privacy4.5" />}
            <br />
            <br />
            {<Trans i18nKey="privacy4.5.1" />}
          </li>
        </ol>

        <p className="title-desc">5. {t("privacy5")}</p>
        <p>{<Trans i18nKey="privacy5.1" />}</p>
        <p>{<Trans i18nKey="privacy5.2" />}</p>
        <p>{<Trans i18nKey="privacy5.3" />}</p>

        <p className="title-desc">6. {t("privacy6")}</p>
        <p>{<Trans i18nKey="privacy6.1" />}</p>
        <p>{<Trans i18nKey="privacy6.2" />}</p>

        <p className="title-desc">7. {t("privacy7")}</p>
        <p>{<Trans i18nKey="privacy7.1" />}</p>
        <p>{<Trans i18nKey="privacy7.2" />}</p>
        <ol type="a">
          <li>{<Trans i18nKey="privacy7.2.1" />}</li>
          <li>{<Trans i18nKey="privacy7.2.2" />}</li>
          <li>{<Trans i18nKey="privacy7.2.3" />}</li>
          <li>{<Trans i18nKey="privacy7.2.4" />}</li>
        </ol>
        <p>{<Trans i18nKey="privacy7.3" />}</p>
        <p>{<Trans i18nKey="privacy7.4" />}</p>
        <p>{<Trans i18nKey="privacy7.5" />}</p>

        <p className="title-desc">8. {t("privacy8")}</p>
        <p>{<Trans i18nKey="privacy8.1" />}</p>
        <p>{<Trans i18nKey="privacy8.2" />}</p>
        <ol type="a">
          <li>{<Trans i18nKey="privacy8.2.1" />}</li>
          <li>{<Trans i18nKey="privacy8.2.2" />}</li>
          <li>{<Trans i18nKey="privacy8.2.3" />}</li>
          <li>{<Trans i18nKey="privacy8.2.4" />}</li>
          <li>{<Trans i18nKey="privacy8.2.5" />}</li>
          <li>{<Trans i18nKey="privacy8.2.6" />}</li>
        </ol>
        <p>{<Trans i18nKey="privacy8.3" />}</p>

        <p className="title-desc">9. {t("privacy9")}</p>
        <p>{<Trans i18nKey="privacy9.1" />}</p>
        <p>{<Trans i18nKey="privacy9.2" />}</p>

        <p className="title-desc">10. {t("privacy10")}</p>
        <p>{<Trans i18nKey="privacy10.1" />}</p>
        <p>{<Trans i18nKey="privacy10.2" />}</p>

        <p className="title-desc">11. {t("privacy11")}</p>
        <p>{<Trans i18nKey="privacy11.1" />}</p>
      </div>
    </Container>
  );
}
export { PrivacyPolicy };
